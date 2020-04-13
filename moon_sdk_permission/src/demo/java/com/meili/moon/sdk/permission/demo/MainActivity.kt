package com.meili.moon.sdk.permission.demo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.Logcat
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.permission.MoonPermission
import com.meili.moon.sdk.permission.Permission
import com.meili.moon.sdk.permission.R
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logcat.config().traceCount = 3

        MoonPermission.Config.newInstance().apply {
            isDirectDeniedRememberUE = true
        }.commit()

        setContentView(R.layout.activity_main)

        mBtnRequest1.onClick {
            val packageURI = Uri.parse("package:$packageName")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
            startActivityForResult(intent, 1009)
        }

        mBtnRequest2.onClick {
            val config = MoonPermission.Config.newInstance().apply {
                // 自定义配置
            }

            CommonSdk.permission().requestWithFailed(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, config = config) { isAllGranted, granted, denied ->
                isAllGranted.log()
                granted.log()
                denied.log()
            }

//            CommonSdk.permission().request(Manifest.permission.CAMERA, config = config) {
//                // 成功回调
//            }
//
//            //
//            CommonSdk.permission().requestWithConfig(Manifest.permission.CAMERA) {
//                isDeniedUEAvailable = false
//                onSuccess {
//                    // 成功回调
//                }
//            }
//
//            CommonSdk.permission().requestWithConfig(Manifest.permission.CAMERA) {
//                isDeniedUEAvailable = false
//                onResult { isAllGranted, granted, denied ->
//
//                }
//            }
//
//            requestPermission(Manifest.permission.CAMERA) {
//                // 成功回调
//            }
//            requestPermissionWithFailed(Manifest.permission.CAMERA) { isAllGranted, granted, denied ->
//                // 结果回调
//            }
//            requestPermissionWithConfig(Manifest.permission.CAMERA) {
//                // 配置
//                isDeniedUEAvailable = true
//                onSuccess {
//                    // 成功回调
//                }
//            }
//            requestPermissionWithConfig(Manifest.permission.CAMERA) {
//                // 配置
//                isDeniedUEAvailable = true
//                onResult { isAllGranted, granted, denied ->
//                    // 结果回调
//                }
//            }
        }

        mBtnRequestMust.onClick {
            testMust()
        }

        mBtnRequestShould.onClick {
            testMustShould()
        }

        mBtnRequestMustDenied.onClick {
            testMustDenied()
        }

        mBtnRequestShouldDenied.onClick {
            testMustShouldDenied()
        }

        mBtnRequestMustDenied3.onClick {
            testMustShouldDenied3()
        }

        mBtnRequestMustDenied2.onClick {
            testMustShouldDenied2()
        }

        mBtnRequestMustDenied4.onClick {
            testMustShouldDenied4("valueR", 100)
        }
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    private fun testMust() {
        "testMust".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, should = [Manifest.permission.ACCESS_FINE_LOCATION])
    private fun testMustShould() {
        "testMustShould".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, onDeniedMethod = "onDenied0")
    private fun testMustDenied() {
        "testMustDenied".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, should = [Manifest.permission.ACCESS_FINE_LOCATION], onDeniedMethod = "onDenied0")
    private fun testMustShouldDenied() {
        "testMustShouldDenied".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, should = [Manifest.permission.ACCESS_FINE_LOCATION], onDeniedMethod = "onDenied3")
    private fun testMustShouldDenied3() {
        "testMustShouldDenied3".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, should = [Manifest.permission.ACCESS_FINE_LOCATION], onDeniedMethod = "onDenied2")
    private fun testMustShouldDenied2() {
        "testMustShouldDenied2".log()
    }

    @Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, should = [Manifest.permission.ACCESS_FINE_LOCATION], onDeniedMethod = "onDenied4")
    private fun testMustShouldDenied4(value: String, value2: Int) {
        value.log()
//        age.log()
        "testMustDenied".log()
    }

    private fun onDenied0() {
        "onDenied0".log()
    }

    private fun onDenied3(isAllGranted: Boolean, granted: Array<String>, denied: Array<String>) {
        isAllGranted.log()
        granted.log()
        denied.log()
        "onDenied3".log()
    }

    private fun onDenied2(granted: Array<String>, denied: Array<String>) {
        granted.log()
        denied.log()
        "onDenied2".log()
    }

    private fun onDenied4(isAllGranted: Boolean, granted: Array<String>, denied: Array<String>, value1: String, value2: Int) {
        isAllGranted.log()
        granted.log()
        denied.log()
        value1.log()
        value2.log()
        "onDenied4".log()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }
}

