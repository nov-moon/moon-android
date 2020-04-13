package com.meili.moon.sdk.permission.internal

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.permission.MoonPermission
import com.meili.moon.sdk.permission.MoonPermissionImpl

/**
 * 申请权限的辅助类
 *
 * 这个类的执行流程是：
 * 1. 申请权限，将结果分为三部分存储：已授权、未标记不再提醒的拒绝授权、标记不再提醒的拒绝授权
 * 2. 如果 未标记不再提醒的未授权 不为空，则提示需要授权：
 *      2.1. 如果用户点取消，则跳转步骤5
 *      2.2. 如果点确定，则使用未授权列表进入步骤1
 * 3. 如果 未标记不再提醒的拒绝授权 为空，则检查 标记不再提醒的拒绝授权 列表，如果它不为空，则提示去设置开启权限：
 *      3.1. 如果用户点取消，则跳转步骤5
 *      3.2. 如果点去设置，则跳转app的设置页面
 *      3.3. 当从设置页面返回后，使用 标记不再提醒的拒绝授权 列表，进入步骤1
 * 4. 如果 未标记不再提醒的拒绝授权 和 标记不再提醒的拒绝授权 列表都为空，则说明所有权限都请求成功
 * 5. 回调原始调用类
 */
class PermissionActivity : AppCompatActivity() {

    private lateinit var mPermissions: Array<String>
    private lateinit var mAppName: CharSequence

    private var mGranted = mutableListOf<String>()
    private var mDenied = mutableListOf<String>()
    private var mDeniedRemember = mutableListOf<String>()

    companion object {
        const val DATA_PERMISSIONS = "data_permissions"

        private const val REQUEST_SETTING = 1000

        private const val REQUEST_CODE_MULTI = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPermissions = intent.getStringArrayExtra(DATA_PERMISSIONS)
        mAppName = applicationInfo.loadLabel(packageManager)

        ActivityCompat.requestPermissions(this, mPermissions, REQUEST_CODE_MULTI)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.isEmpty() && !mPermissions.isEmpty()) {
            // 在测试的时候发现，如果走到去设置流程，直接返回会出两次permissionresult，第一次数组为空，第二次才是正常的
            return
        }
        when (requestCode) {
            REQUEST_CODE_MULTI -> {
                // 1. 将数据拆分为已授权和未授权列表
                permissions.forEachIndexed { index, s ->
                    mGranted.remove(s)
                    mDenied.remove(s)
                    mDeniedRemember.remove(s)

                    if (grantResults[index] != PackageManager.PERMISSION_DENIED) {
                        mGranted.add(s)
                    } else {
                        mDenied.add(s)
                    }
                }
                if (mDenied.isEmpty() && mDeniedRemember.isEmpty()) {
                    finish()
                    return
                }

                if (MoonPermissionImpl.currConfig?.isDirectDeniedRememberUE == true) {
                    showOnDeniedRememberDialog(mDenied)
                    return
                }

                // 2. 将未授权列表拆分为：未授权和不再提醒授权列表
                mDenied = mDenied.filter {
                    if (!shouldShowRequestPermissionRationale(it)) {
                        mDeniedRemember.add(it)
                        return@filter false
                    } else true
                }.toMutableList()

                // 3. 如果未授权列表为空，则代表不再提醒列表不为空，进行不再提醒逻辑，否则进行未授权逻辑
                if (mDenied.isEmpty()) {
                    showOnDeniedRememberDialog(mDeniedRemember)
                } else {
                    showOnDeniedDialog(mDenied)
                }
            }
        }
    }

    /**
     * 显示不再提醒弹窗
     *
     * 优先使用config中的回调方式进行处理，如果没有回调，则使用config中的文案进行弹窗展示
     *
     */
    private fun showOnDeniedRememberDialog(deniedList: MutableList<String>) {
        if (MoonPermissionImpl.currConfig?.isDeniedRememberUEAvailable != true) {
            finish()
            return
        }
        val (permissionName,
                functionName,
                permissionDescs) = processDesc(deniedList)

        val config = MoonPermissionImpl.currConfig ?: MoonPermissionImpl.config
        if (config.onDeniedRememberUECallback != null) {
            config.onDeniedRememberUECallback?.invoke(deniedList.toTypedArray(),
                    permissionDescs.toTypedArray(),
                    { finish() },
                    {
                        try {
                            val packageURI = Uri.parse("package:$packageName")
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                            startActivityForResult(intent, REQUEST_SETTING)
                        } catch (e: Exception) {
                            finish()
                        }
                    })

            return
        }

        val (permissionStr, funStr) = processDescStr(permissionName, functionName)

        val msg = String.format(config.onDeniedRememberDescription
                ?: "", mAppName, permissionStr, funStr)

        showAlertDialog(config.onDeniedRememberTitle ?: "权限申请",
                msg,
                config.onDeniedRememberCancelButton ?: "取消",
                config.onDeniedRememberSettingButton ?: "去设置") {
            try {
                val packageURI = Uri.parse("package:$packageName")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                startActivityForResult(intent, REQUEST_SETTING)
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    /**
     * 显示拒绝权限弹窗
     *
     * 优先使用config中的回调方式进行处理，如果没有回调，则使用config中的文案进行弹窗展示
     *
     */
    private fun showOnDeniedDialog(deniedList: MutableList<String>) {

        if (MoonPermissionImpl.currConfig?.isDeniedUEAvailable != true) {
            finish()
            return
        }

        val (permissionName,
                functionName,
                permissionDescs) = processDesc(deniedList)

        val config = MoonPermissionImpl.currConfig ?: MoonPermissionImpl.config
        if (config.onDeniedUECallback != null) {
            config.onDeniedUECallback?.invoke(
                    deniedList.toTypedArray(),
                    permissionDescs.toTypedArray(),
                    { finish() },
                    {
                        ActivityCompat.requestPermissions(this, deniedList.toTypedArray(), REQUEST_CODE_MULTI)
                    })

            return
        }

        val (permissionStr, funStr) = processDescStr(permissionName, functionName)


        val msg = String.format(config.onDeniedDescription
                ?: "", permissionStr, funStr)

        showAlertDialog(config.onDeniedTitle ?: "权限申请",
                msg,
                config.onDeniedCancelButton ?: "取消",
                config.onDeniedButton ?: "授权") {
            ActivityCompat.requestPermissions(this, deniedList.toTypedArray(), REQUEST_CODE_MULTI)
        }
    }

    private fun showAlertDialog(title: CharSequence, msg: String, cancelTxt: CharSequence, submitTxt: CharSequence,
                                cancelCallback: (() -> Unit)? = null, submitCallback: (() -> Unit)? = null) {
        val alertDialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(cancelTxt) { dialog, _ ->
                    if (cancelCallback != null) {
                        cancelCallback()
                    } else {
                        finish()
                    }
                    dialog.dismiss()
                }
                .setPositiveButton(submitTxt) { dialog, _ ->
                    if (submitCallback != null) {
                        submitCallback()
                    } else {
                        finish()
                    }
                    dialog.dismiss()
                }.create()

        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SETTING) {
            if (MoonPermissionImpl.currConfig?.isDirectDeniedRememberUE == true) {
                ActivityCompat.requestPermissions(this, mDenied.toTypedArray(), REQUEST_CODE_MULTI)
                return
            }
            ActivityCompat.requestPermissions(this, mDeniedRemember.toTypedArray(), REQUEST_CODE_MULTI)
        }
    }

    override fun finish() {
        super.finish()
        postPermissionResult()
    }

    private fun postPermissionResult() {
        mDenied.addAll(mDeniedRemember)
        MoonPermissionImpl.onPermissionEnd(mGranted, mDenied)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        val bundle = outState ?: Bundle()
        bundle.putStringArray("onGrantedPermission", mGranted.toTypedArray())
        bundle.putStringArray("onDeniedPermission", mDenied.toTypedArray())
        bundle.putStringArray("onDeniedRememberPermission", mDeniedRemember.toTypedArray())
        super.onSaveInstanceState(bundle)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState ?: return
        mGranted = savedInstanceState.getStringArray("onGrantedPermission")?.toMutableList()
                ?: mutableListOf()
        mDenied = savedInstanceState.getStringArray("onDeniedPermission")?.toMutableList()
                ?: mutableListOf()
        mDeniedRemember = savedInstanceState.getStringArray("onDeniedRememberPermission")?.toMutableList()
                ?: mutableListOf()
    }

    private fun processDesc(deniedList: MutableList<String>): Triple<MutableList<String>, MutableList<String>, MutableList<MoonPermission.PermissionDesc>> {
        val permissionName = mutableListOf<String>()
        val functionName = mutableListOf<String>()
        val permissionDescs = mutableListOf<MoonPermission.PermissionDesc>()

        deniedList.forEach {
            val permissionGroups = CommonSdk.permission().config().permissionGroups
            val permissionDesc = permissionGroups[it]
            if (permissionDesc != null) {
                permissionName.add(permissionDesc.groupName)
                permissionDescs.add(permissionDesc)
                if (permissionDesc.functionName.isNotEmpty() && permissionDesc.functionName != "-") {
                    functionName.add(permissionDesc.functionName)
                }
            }
        }
        return Triple(permissionName, functionName, permissionDescs)
    }


    private fun processDescStr(permissionName: MutableList<String>, functionName: MutableList<String>): Pair<String, String> {
        val permissionSb = StringBuilder()
        val funSb = StringBuilder()

        try {
            permissionName.forEach {
                permissionSb.append(it)
                permissionSb.append("、")
            }
            permissionSb.deleteCharAt(permissionSb.length - 1)

            functionName.forEach {
                funSb.append(it)
                funSb.append("、")
            }
            funSb.deleteCharAt(funSb.length - 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val funStr = when {
            funSb.isEmpty() -> "App"
            (permissionName.size != functionName.size) -> "${funSb}及其他"
            else -> funSb.toString()
        }

        return Pair(permissionSb.toString(), funStr)
    }
}
