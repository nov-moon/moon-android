package com.meili.moon.sdk.permission

import android.Manifest

/**
 * 方法上的权限注解，这里强制要求使用此注解方法的返回类型必须为void，并且注解方法不能为静态方法
 *
 * 使用方式如下：
 *
 * 1. 普通调用方式
 *
 * @Permission(Manifest.permission.READ_SMS, Manifest.permission.READ_SMS)
 * fun test() {
 *  // 业务内容
 * }
 *
 * 如上，可请求多个权限。如果获取到权限，则会运行test内容，否则将调用通用提示交互
 *
 * 2. 定义次要权限，应该获取，但不是必须权限
 *
 * 如果有部分权限不是必须权限，可以使用如下方式获取：
 * @Permission(Manifest.permission.READ_SMS, should = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
 * fun test() {
 *  // 业务内容
 * }
 * should参数中可存放多个权限，如果should中的权限没有获取成功，也不影响test的调用，其他同1
 *
 * 3. 自定义被拒绝后的处理方式
 *
 * 在1、2中，如果用户拒绝了权限申请，都会直接使用框架默认的交互方式，提示用户授权。如果你关注拒绝后的时机，需要处理一些业务，可如下定义：
 * @Permission(Manifest.permission.READ_SMS, should = [Manifest.permission.WRITE_EXTERNAL_STORAGE], deniedMethod = "onDenied")
 * fun test() {
 *  // 业务内容
 * }
 * 如上，deniedMethod参数指的是如果必要权限被拒绝后，下一步调用的方法名称。
 * 如果用户拒绝了必要权限，框架会尝试调用此方法，使用者可以在此方法中进行处理。
 * 我们暂时统一叫此方法为降级方法。在降级方法中，我们支持如下参数列表的声明：
 *
 * 1. 带有授权、被拒列表和原始方法入参的方法
 * fun onDenied(isAllGranted, granted: Array<String>, denied: Array<String>, 原始方法列表)
 *
 * 2. 带有授权、被拒列表的方法
 * fun onDenied(isAllGranted, granted: Array<String>, denied: Array<String>)
 *
 * 3. 带有授权、被拒列表的方法
 * fun onDenied(granted: Array<String>, denied: Array<String>)
 *
 * 4. 无参方法
 * fun onDenied()
 *
 * 如上所示，我们支持以上参数列表。这些被拒方法，都必须和注解方法在同一个类中声明，并且不能为静态方法
 * 如果有同名方法，我们优先使用符合条件的方法，如果有多个都符合，我们只调用其中一个方法。优先级为上述顺序。
 *
 * 在有参方法中，请避免使用可变长参数的方法定义方式，以避免不可预期的错误。
 *
 * Created on 2019/1/17
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Permission(
        /**
         * 申请的必要权限名称，例如：Manifest.permission.READ_SMS。
         * 可传入多个，这里声明的权限为必要权限，如果其中有一个申请失败，则直接进入失败流程
         */
        vararg val name: String,

        /**
         * 申请的非必要权限名称，以数组方式传入，例如：should = [Manifest.permission.READ_SMS]
         * 这里声明的权限未非必要权限，如果用户拒绝，也会进入成功流程
         */
        val should: Array<String> = [],

        /**
         * 定义用户拒绝时的回调方法名称，例如：onDeniedMethod = "onDenied"
         *
         * 如果用户拒绝了必要权限，会回调此方法。
         * 我们暂时统一叫此方法为降级方法。在降级方法中，我们支持如下参数列表的声明：
         *
         * 1. 带有授权、被拒列表和原始方法入参的方法
         * fun onDenied(isAllGranted, granted: Array<String>, denied: Array<String>, 原始方法列表)
         *
         * 2. 带有授权、被拒列表的方法
         * fun onDenied(isAllGranted, granted: Array<String>, denied: Array<String>)
         *
         * 3. 带有授权、被拒列表的方法
         * fun onDenied(granted: Array<String>, denied: Array<String>)
         *
         * 4. 无参方法
         * fun onDenied()
         *
         * 如上所示，我们支持以上参数列表。这些被拒方法，都必须和注解方法在同一个类中声明，并且不能为静态方法
         * 如果有同名方法，我们优先使用符合条件的方法，如果有多个都符合，我们只调用其中一个方法。优先级为上述顺序。
         */
        val onDeniedMethod: String = ""
)