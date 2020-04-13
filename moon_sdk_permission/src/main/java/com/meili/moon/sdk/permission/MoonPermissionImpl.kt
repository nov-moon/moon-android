package com.meili.moon.sdk.permission

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.permission.internal.PermissionActivity
import com.meili.moon.sdk.permission.internal.Util
import com.meili.moon.sdk.util.app
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.throwOnDebug
import com.meili.moon.sdk.util.toT

/**
 * 皓月动态权限库的实现入口类
 *
 * Author imuto
 */
object MoonPermissionImpl : MoonPermission, IComponent {

    var config: MoonPermission.Config = MoonPermission.Config.original()

    /**
     * 全量回调
     */
    private var onResult: OnPermissionCallback? = null
    /**
     * 全部成功回调
     */
    private var onAllSuccess: OnPermissionSuccessCallback? = null

    /**
     * 单词请求的config
     */
    internal var currConfig: MoonPermission.Config? = null

    override fun onAfterInit() {
        super.onAfterInit()

        config.apply {
            isDeniedUEAvailable = true
            isDeniedRememberUEAvailable = true
            onDeniedRememberTitle = "权限申请"
            onDeniedRememberDescription = "在'设置-应用-%s-权限'中开启%s权限，以正常使用%s功能"
            onDeniedRememberCancelButton = "取消"
            onDeniedRememberSettingButton = "去设置"

            onDeniedTitle = "权限申请"
            onDeniedDescription = "请开启%s权限，以正常使用%s功能"
            onDeniedCancelButton = "取消"
            onDeniedButton = "授权"
            config.registerPermissionGroups(
                    R.array.moon_permission_call,
                    R.array.moon_permission_sms,
                    R.array.moon_permission_accounts,
                    R.array.moon_permission_email,
                    R.array.moon_permission_location,
                    R.array.moon_permission_calendar,
                    R.array.moon_permission_sensor,
                    R.array.moon_permission_phone,
                    R.array.moon_permission_camera,
                    R.array.moon_permission_audio,
                    R.array.moon_permission_storage
            )
        }
    }

    init {
        Log.e("MoonPermissionImpl", "MoonPermissionImpl")
    }

    override fun init(env: Environment) {
        Log.e("MoonPermissionImpl", "init")
        ComponentsInstaller.installPermission(this, env)
    }

    override fun check(vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        permissions.forEach {
            if (app.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }

    override fun request(vararg permissions: String, config: MoonPermission.Config?, onAllSuccess: OnPermissionSuccessCallback) {
        requestInner(permissions, config, onAllSuccess, null)
    }

    override fun requestWithFailed(vararg permissions: String, config: MoonPermission.Config?, onResult: OnPermissionCallback) {
        requestInner(permissions, config, null, onResult)
    }

    override fun requestWithConfig(vararg permissions: String, configLambda: MoonPermission.ConfigCallback.() -> Unit) {
        val configCallback = MoonPermission.ConfigCallback()
        configLambda.invoke(configCallback)
        requestInner(permissions, configCallback, configCallback.successCallback, configCallback.resultCallback)
    }

    override fun config(): MoonPermission.Config = config

    override fun config(config: MoonPermission.Config) {
        this.config = config
    }

    private fun requestInner(permissions: Array<out String>? = null, config: MoonPermission.Config?,
                             onAllSuccess: OnPermissionSuccessCallback?, onResult: OnPermissionCallback?) {
        val array: Array<out String> = permissions ?: return
        if (onAllSuccess != null && onResult != null) {
            throwOnDebug("在请求权限的方法回调中，只能设置一个接收方法，" +
                    "请参见com.meili.moon.sdk.permission.MoonPermission.ConfigCallback中的定义")
        }
        if (check(*array)) {
            if (onAllSuccess != null) {
                onAllSuccess()
            } else if (onResult != null) {
                onResult(true, array.toT(), emptyArray())
            }
            return
        }

        synchronized(this) {
            this.onAllSuccess = onAllSuccess
            this.onResult = onResult
            this.currConfig = config?.apply(config()) ?: config()

            val intent = Intent(app, PermissionActivity::class.java)
            intent.putExtra(PermissionActivity.DATA_PERMISSIONS, permissions)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        }
    }

    /**
     * 当权限申请完成后
     */
    internal fun onPermissionEnd(granted: List<String>?, denied: List<String>?) {
        synchronized(this) {
            if (!isEmpty(denied)) {
                onResult?.invoke(false, granted?.toTypedArray(), denied?.toTypedArray())
            } else {
                onAllSuccess?.invoke()
                onResult?.invoke(true, granted?.toTypedArray(), denied?.toTypedArray())
            }
            onAllSuccess = null
            onResult = null
            currConfig = null
        }
    }

    /**
     * 给字节码插装方法提供的权限申请方法
     *
     * [host] 申请权限的主对象
     * [must] 申请权限的必须列表
     * [should] 申请权限的非必须列表
     * [onDeniedMethod] 当有[must]列表中的权限被拒绝时，回调的方法名称，不能做混淆处理
     * [methodName] 调用此方法的方法名称，不能做混淆处理。当must权限都被授权时，将重新调用此方法，并将[methodArgs]传入
     */
    fun checkPermissionForByteCode(host: Any, must: Array<String>, should: Array<String>?, onDeniedMethod: String?,
                                   methodName: String, methodDesc: String, methodArgs: Array<Any?>?): Boolean {
        if (check(*must)) {
            return true
        }

        val permissions = mutableListOf<String>()
        permissions.addAll(must)
        if (should != null) {
            permissions.addAll(should)
        }

        val invokeHost: (Boolean, Array<String>, Array<String>) -> Unit = invokeHost@{ isSuccess, granted, denied ->
            if (isSuccess) {
                val findMethod = Util.findMethod(host, methodName, methodDesc)
                val args = methodArgs ?: emptyArray()
                findMethod?.invoke(host, *args)
                return@invokeHost
            }

            if (onDeniedMethod.isNullOrEmpty()) {
                "没有失败回调的匹配方法".log()
                return@invokeHost
            }
            val subMethodDesc = methodDesc.substring(1 until methodDesc.indexOf(")"))

            var findMethod = Util.findMethod(host, onDeniedMethod,
                    "(Z[Ljava/lang/String;[Ljava/lang/String;$subMethodDesc)V")
            if (findMethod != null) {
                val mutableList = mutableListOf<Any?>()
                mutableList.add(isSuccess)
                mutableList.add(granted)
                mutableList.add(denied)
                mutableList.addAll(methodArgs ?: emptyArray())

                val args = mutableList.toTypedArray()

                findMethod.invoke(host, *args)
                return@invokeHost
            }

            findMethod = Util.findMethod(host, onDeniedMethod,
                    "(Z[Ljava/lang/String;[Ljava/lang/String;)V")
            if (findMethod != null) {
                findMethod.invoke(host, isSuccess, granted, denied)
                return@invokeHost
            }

            findMethod = Util.findMethod(host, onDeniedMethod,
                    "([Ljava/lang/String;[Ljava/lang/String;)V")
            if (findMethod != null) {
                findMethod.invoke(host, granted, denied)
                return@invokeHost
            }

            findMethod = Util.findMethod(host, onDeniedMethod, "()V")
            if (findMethod != null) {
                findMethod.invoke(host)
                return@invokeHost
            }

            "未找到失败回调的匹配方法".log()
        }

        val arrayPermission = permissions.toTypedArray()
        requestWithFailed(*arrayPermission) { isAllGranted, granted, denied ->
            val deniedVal = denied ?: emptyArray()
            if (isAllGranted && granted != null) {
                invokeHost(true, granted, deniedVal)
                return@requestWithFailed
            }

            if (granted == null) {
                invokeHost(false, emptyArray(), deniedVal)
                return@requestWithFailed
            }

            val mustInDenied = must.filter { deniedVal.contains(it) }

            if (mustInDenied.isEmpty()) {
                invokeHost(true, granted, deniedVal)
                return@requestWithFailed
            }

            invokeHost(false, granted, deniedVal)
        }

        return false
    }

}