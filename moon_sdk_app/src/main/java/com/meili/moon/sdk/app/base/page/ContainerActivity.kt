package com.meili.moon.sdk.app.base.page

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.BaseHostConfigs
import com.meili.moon.sdk.app.util.*
import com.meili.moon.sdk.base.util.*

//import com.uuzuche.lib_zxing.activity.CaptureActivity
//import com.uuzuche.lib_zxing.activity.CodeUtils


/**
 * Created by imuto on 2018/4/9.
 */
class ContainerActivity : BaseActivity() {

    override var containerId: Int = R.id.mLayoutContainer

    override lateinit var container: ViewGroup


    //摇一摇 工具类
    private var mShakeUtils: ShakeUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //关闭网页加载优化，以获取网页的全部内容。
            WebView.enableSlowWholeDocumentDraw()
        }
        setContentView(R.layout.moon_sdk_app_container_activity)

        container = findViewById(containerId)

        initDebugHost()
    }

    private fun initDebugHost() {
        if (!VersionUtils.isDebug()) return

        val initShake = {
            val shake = ShakeUtils(this)
            mShakeUtils = shake
            shake
        }

        val shake = mShakeUtils ?: initShake()

        shake.setOnShakeListener {
            showHostDialog()
        }
    }

    private fun showHostDialog() {

        mShakeUtils?.enable = false

        val host = BaseHostConfigs.getActiveHost("API服务器")

        val items = host.getViewHosts().toMutableList()
        val customType = items.find { it.buildType.isCustom }
        val selectedItem = items.find { it.isSelected }

        if (customType == null) {
            items.add(BaseHostConfigs.Hosts.HostItem("本地环境", null, VersionUtils.BuildType.BUILD_TYPE_CUSTOM, false, true))
        }

        showChoiceDialog("选择服务器地址", items, dismissListener = {
            mShakeUtils?.enable = true
        }) {
            val item = it.toT<BaseHostConfigs.Hosts.HostItem>()!!
            if (item.buildType.isCustom) {
                val value = if ("本地环境" == item.host) "http://172.28.12.0:9000" else item.host
                post(500) {
                    selectedItem?.isSelected = true
                    val itemVar = if (item == selectedItem) null else item
                    showEditDialog(value, itemVar, host)
                }
            } else {
                host.select(item)
            }
            false
        }
    }

    private fun showEditDialog(native: String, item: BaseHostConfigs.Hosts.HostItem?,
                               host: BaseHostConfigs.Hosts) {
        mShakeUtils?.enable = false
        item?.isSelected = false

        val content = inflating(R.layout.moon_sdk_app_dialog_host_config)

        val dialog = showDialog(content) {
            mShakeUtils?.enable = true
        }

        val edit = content.findViewById<EditText>(R.id.mEditContent)
        edit.setText(native)

        content.findViewById<View>(R.id.mTxtCancel).onClick {
            dialog.dismiss()
        }

        content.findViewById<View>(R.id.mTxtSubmit).onClick {

            val str = edit.text.toString()
            if (str.trim().length < 15) {
                ToastUtil.showFailed("无效地址")
                return@onClick
            }

            edit.hideKeyboard()

            host.updateCustom(str, "本地")
            dialog.dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
        mShakeUtils?.onResume()
//        val topPage = Sdk.page().getTopPage() ?: return
//        if (topPage is Fragment) {
//            topPage.onResume()
//        }

    }

    override fun onStop() {
        super.onStop()
        mShakeUtils?.onPause()
//        val topPage = Sdk.page().getTopPage() ?: return
//        if (topPage is Fragment) {
//            topPage.onStop()
//        }
    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * 结束当前页面
     *
     * [isForce] 是否强制结束，如果是fragment，则不会执行onPreFinish()逻辑
     */
    override fun finish(isForce: Boolean) {
        finish()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        /**
//         * 处理二维码扫描结果
//         */
//        if (requestCode == 1234) {
//            //处理扫描结果（在界面上显示）
//            if (null != data) {
//                val bundle = data.getExtras()
//                if (bundle == null) {
//                    return;
//                }
//                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
//                    val result = bundle.getString(CodeUtils.RESULT_STRING);
//                    val pageIntent = PageIntent("common/web")
//                    pageIntent.putExtra("url", result)
//                    Sdk.page().gotoPage(pageIntent)
//                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
//                    Toast.makeText(this, "解析二维码失败", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, intent)
//    }
}