package com.meili.moon.sdk.permission.demo

import android.app.Application
import com.meili.moon.sdk.permission.MoonPermissionImpl

/**
 * Application
 */
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化
        MoonPermissionImpl.init(this)
        // 配置
//        MoonPermission.Config.newInstance().apply {
//            // 是否使用被拒后的dialog交互，默认为true
//            isDeniedUEAvailable = false
//
//            // 是否使用被拒并且不再提示后的dialog交互，默认为true
//            isDeniedRememberUEAvailable = false
//
//            // 设置被拒后的弹窗上的title
//            onDeniedTitle = "权限申请"
//
//            // 设置被拒后的弹窗上的描述信息，此String以拼串的方式使用，接收两个string：
//            // 第一个为被拒权限名称列表例如：定位、电话，
//            // 第二个为被拒权限影响的功能名称，例如：地图、打电话。
//            onDeniedDescription = "请开启%s权限，以正常使用%s功能"
//
//            // 设置被拒并且不再提示后的弹窗上的描述信息，此String以拼串的方式使用，接收三个string：
//            // 第一个为app名称占位符
//            // 第二个为被拒权限名称列表例如：定位、电话，
//            // 第三个为被拒权限影响的功能名称，例如：地图、打电话。
//            onDeniedRememberDescription = "在设置-应用-应用名称-权限中开启%s权限，以正常使用%s功能"
//
//            // 当被拒时，不使用框架默认的交互方式，自定义交互方式
//            // denied 被拒绝的权限列表
//            // permissionDesc 被拒绝的权限列表的描述信息
//            // onCancel 当取消下一步操作时，回调此方法，一般在弹窗的取消按钮上调用
//            // onSubmit 当继续下一步操作时，回调此方法，一般在弹窗的确定按钮上调用
//            onDeniedUECallback = {denied, permissionDesc, onCancel, onSubmit ->
//
//            }
//
//            // 当被拒并且不再提示时，不使用框架默认的交互方式，自定义交互方式
//            // denied 被拒绝的权限列表
//            // permissionDesc 被拒绝的权限列表的描述信息
//            // onCancel 当取消下一步操作时，回调此方法，一般在弹窗的取消按钮上调用
//            // onSubmit 当继续下一步操作时，回调此方法，一般在弹窗的确定按钮上调用
//            onDeniedRememberUECallback = {denied, permissionDesc, onCancel, onSubmit ->
//
//            }
//        }.commit()
    }
}
