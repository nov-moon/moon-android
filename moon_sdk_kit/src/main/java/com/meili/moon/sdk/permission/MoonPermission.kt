package com.meili.moon.sdk.permission

import android.support.annotation.ArrayRes
import android.support.annotation.MainThread
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.util.app

typealias OnPermissionCallback = (isAllGranted: Boolean, granted: Array<String>?, denied: Array<String>?) -> Unit
typealias OnPermissionSuccessCallback = () -> Unit
typealias OnPermissionUECallback = ((denied: Array<String>, permissionDesc: Array<MoonPermission.PermissionDesc>,
                                     onCancel: () -> Unit, onSubmit: () -> Unit) -> Unit)

/**
 * 皓月动态权限库的入口。
 *
 * 皓月动态权限库提供简化的动态权限判定，方便开发者权限检查和ue管理。
 *
 * 皓月提供两种权限管理方案：注解的形式进行权限检查和自动回调、自主调用的方式管理权限请求策略
 *
 * 1. 注解的形式进行权限检查和自动回调
 *  皓月库提供Permission注解，用来在方法上注册此方法执行所必须的权限列表。
 *  我们在编译期对方法进行插桩，以此调用权限库判断权限。
 *  如果有权限则继续执行方法
 *  如果没有则执行权限请求流程，并在成功后回调原始方法。如果失败后则执行默认ue交互逻辑
 *
 * 2. 自主调用的方式管理权限请求策略
 *  皓月库提供便捷的api，在使用处，开发者自主调用[request]方法请求权限，如果授权失败，直接使用默认ue交互的话，则不需要关注失败逻辑。
 *  如果关注失败结果的话，则使用带两个参数回调的[request]方法即可
 *
 * Author imuto
 */
interface MoonPermission {

    /**
     * 判断列表[permissions]中的权限是否全部被授予，如果具有全部权限，则返回true，否则返回false。
     *
     * 此方法不具有任何交互，只是检查方法
     */
    fun check(vararg permissions: String): Boolean

    /**
     * 批量权限申请，如果所有权限都已经申请成功，则在[onAllSuccess]中得到回调，否则自动进入默认的失败交互逻辑
     *
     * 注意，回调只有在所有权限都成功的情况下才会触发，如果要关注失败回调，请使用[requestWithFailed]
     *
     * 如果[config]不为空，则尝试使用此config进行单次自定义权限申请，此config可以只设置你单次需要特殊定义的部分，其他不进行定义的部分将尝试复用通用设置
     */
    @MainThread
    fun request(vararg permissions: String, config: Config? = null, onAllSuccess: OnPermissionSuccessCallback)

    /**
     * 批量权限申请，不管成功还是失败，都会得到回调
     *
     * 不论申请成功与否，都会回调到[onResult]中，并将成功权限数组和失败权限数组给到回调方法。
     *
     * 如果[config]不为空，则尝试使用此config进行单次自定义权限申请，此config可以只设置你单次需要特殊定义的部分，其他不进行定义的部分将尝试复用通用设置
     */
    @MainThread
    fun requestWithFailed(vararg permissions: String, config: Config? = null, onResult: OnPermissionCallback)

    /**
     * 批量权限申请
     *
     * 请求的具体结果，将在 [configLambda] 中得到回调
     *
     */
    @MainThread
    fun requestWithConfig(vararg permissions: String, configLambda: ConfigCallback.() -> Unit)

    /**
     * 获取动态权限库的全局配置选项
     */
    fun config(): Config

    /**
     * 配置动态权限库的全局选项
     */
    fun config(config: Config)

    /**
     * 权限请求的配置选项
     *
     * 在这些配置项中，主要分三部分：deniedRemember权限交互、denied权限交互、全局方法
     *
     * deniedRemember权限交互的含义是：当我们请求权限时，用户可能拒绝权限，并且选择不再提示，这时这部分交互会被触发
     *
     * denied权限交互含义：当我们发起权限请求是，用户可能拒绝，但是并没有勾选不再提示，这时候会触发这部分交互
     *
     * 在这两种配置中，如果是局部配置，如果配置项为null，则尝试使用全局配置的config内容
     *
     */
    open class Config {

        companion object {
            /**
             * 使用全局配置，生成一个新config
             */
            fun newInstance(): Config {
                return Config().apply(CommonSdk.permission().config())
            }

            /**
             * 使用全新对象，不使用全局配置
             */
            fun original(): Config {
                return Config()
            }
        }

        /**
         * 是否 'denied权限交互' 可用
         */
        var isDeniedUEAvailable: Boolean? = null

        /**
         * 是否直接使用 'deniedRemember权限交互'，舍弃使用 'denied权限交互'。
         *
         * 默认为false，如果设置为true，则忽略[isDeniedUEAvailable]、[isDeniedRememberUEAvailable]
         */
        var isDirectDeniedRememberUE = false

        /**
         * 是否 'deniedRemember权限交互' 可用
         */
        var isDeniedRememberUEAvailable: Boolean? = null

        /**
         * 默认deniedRemember权限交互：设置被拒绝后的title，如果设置为null，则使用默认提示
         */
        var onDeniedRememberTitle: CharSequence? = null
        /**
         * 默认deniedRemember权限交互：设置被拒绝后的提示，如果设置为null，则使用默认提示
         *
         * 这个参数接收格式为字符串替代，默认为："在设置-应用-%s-权限中开启%s权限，以正常使用%s功能"，你可以定义%s的位置和前后话术
         */
        var onDeniedRememberDescription: String? = null
        /**
         * 默认deniedRemember权限交互：设置被拒绝后的取消按钮文本，如果设置为null，则使用默认提示
         */
        var onDeniedRememberCancelButton: CharSequence? = null
        /**
         * 默认deniedRemember权限交互：设置被拒绝后的去设置按钮文本，在全局设置时不可为空，在局部设置时，如果为空则使用全局的值
         */
        var onDeniedRememberSettingButton: CharSequence? = null

        /**
         * 自定义onDeniedRemember权限交互：设置权限被拒，并且不再提醒后的交互回调，如果此属性设置，则默认deniedRemember权限交互将忽略
         *
         * [denied]被拒的权限列表，[permissionDesc]被拒的权限描述列表，
         * [onCancel]在后续交互中，用户取消的回调，如果执行此回调，会结束权限申请，并返回结果，一般在弹出框的取消按钮上调用，
         * [onSubmit]在后续交互中，用户继续的回调，如果执行此回调，会继续权限申请，一般在弹出框的确定、授权按钮上调用
         *
         */
        var onDeniedRememberUECallback: OnPermissionUECallback? = null


        /**
         * 默认denied权限交互：设置被拒权限后的弹窗title，如果设置为null，则使用默认提示
         */
        var onDeniedTitle: CharSequence? = null
        /**
         * 默认denied权限交互：设置被拒权限后的弹窗的提示，如果设置为null，则使用默认值
         *
         * 这个参数接收格式为字符串替代，默认为："请开启%s权限，以正常使用%s功能"，你可以定义%s的位置和前后话术
         */
        var onDeniedDescription: String? = null
        /**
         * 默认denied权限交互：设置被拒权限后弹窗的取消按钮文本，如果设置为null，则使用默认提示
         */
        var onDeniedCancelButton: CharSequence? = null
        /**
         * 默认denied权限交互：设置被拒权限后弹窗按钮文本，在全局设置时不可为空，在局部设置时，如果为空则使用全局的值
         */
        var onDeniedButton: CharSequence? = null

        /**
         * 自定义denied权限交互：设置被拒权限后的交互回调，如果此属性设置，则默认'denied权限交互'将忽略
         *
         * [denied]被拒的权限列表，[permissionDesc]被拒的权限描述列表，
         * [onCancel]在后续交互中，用户取消的回调，如果执行此回调，会结束权限申请，并返回结果，一般在弹出框的取消按钮上调用，
         * [onSubmit]在后续交互中，用户继续的回调，如果执行此回调，会继续权限申请，一般在弹出框的确定、授权按钮上调用
         *
         */
        var onDeniedUECallback: OnPermissionUECallback? = null

        /**
         * 权限组，用于当用户拒绝时，给用户展示dialog时的权限描述和功能描述
         */
        var permissionGroups: MutableMap<String, PermissionDesc> = mutableMapOf()

        /**
         * 注册权限组，用于当用户拒绝时，给用户展示dialog时的权限描述和功能描述
         */
        fun registerPermissionGroup(permission: String, permissionDesc: PermissionDesc) {
            permissionGroups[permission] = permissionDesc
        }

        /**
         * 使用资源文件的stringArray方式注册权限组，用于当用户拒绝时，给用户展示dialog时的权限描述和功能描述
         *
         * 资源格式必须符合权限组资源格式，如下：
         * index = 0 : 当前权限组名称
         * index = 1 : 当前权限组对应的功能名称
         *     .     : 当前权限组中的权限
         *     .     : 当前权限组中的权限
         *     .     : 当前权限组中的权限
         *     .     : 当前权限组中的权限
         * 其中，权限组功能名称可能为无，当为无时，使用短中划线 '-' 进行标记
         * 例如：
         * <string-array name="moon_permission_call">
         *     <item>电话</item><!-- 被拒绝权限名称 -->
         *     <item>通话</item><!-- 影响的功能名称 -->
         *     <item>Manifest.permission.ACCEPT_HANDOVER</item>
         *     <item>Manifest.permission.ANSWER_PHONE_CALLS</item>
         * </string-array>
         */
        fun registerPermissionGroups(@ArrayRes vararg resIds: Int) {
            parsePermissionGroupFromRes(*resIds)
        }

        /**
         * 注销权限组
         */
        fun unregisterPermissionGroup(permission: String) {
            permissionGroups.remove(permission)
        }

        /**
         * 将入参中的配置信息，补充到当前配置中
         */
        fun apply(config: Config): Config {
            isDeniedUEAvailable = isDeniedUEAvailable ?: config.isDeniedUEAvailable
            isDeniedRememberUEAvailable = isDeniedRememberUEAvailable
                    ?: config.isDeniedRememberUEAvailable
            onDeniedRememberTitle = onDeniedRememberTitle ?: config.onDeniedRememberTitle
            onDeniedRememberDescription = onDeniedRememberDescription
                    ?: config.onDeniedRememberDescription
            onDeniedRememberCancelButton = onDeniedRememberCancelButton
                    ?: config.onDeniedRememberCancelButton
            onDeniedRememberSettingButton = onDeniedRememberSettingButton
                    ?: config.onDeniedRememberSettingButton
            onDeniedRememberUECallback = onDeniedRememberUECallback
                    ?: config.onDeniedRememberUECallback
            onDeniedTitle = onDeniedTitle ?: config.onDeniedTitle
            onDeniedDescription = onDeniedDescription ?: config.onDeniedDescription
            onDeniedCancelButton = onDeniedCancelButton ?: config.onDeniedCancelButton
            onDeniedButton = onDeniedButton ?: config.onDeniedButton
            onDeniedUECallback = onDeniedUECallback ?: config.onDeniedUECallback
            permissionGroups = if (permissionGroups.isEmpty()) {
                config.permissionGroups
            } else permissionGroups

            return this
        }

        /**
         * 将当前config应用到全局
         */
        fun commit() {
            CommonSdk.permission().config(this)
        }

        private fun parsePermissionGroupFromRes(@ArrayRes vararg resIds: Int) {
            resIds.forEach { res ->
                val array: Array<String>? = app.resources.getStringArray(res)
                array ?: return@forEach
                val groupName = array[0]
                val funName = array[1]
                val permissionGroup = PermissionDesc(groupName, funName)
                (2 until array.size).forEach {
                    registerPermissionGroup(array[it], permissionGroup)
                }
            }
        }
    }

    /**
     * 方便对当前请求进行config的扩展类
     *
     * 使用此方式，必须重写[onSuccess]、[onResult]方法中的一个，否则将报错
     */
    class ConfigCallback : Config() {
        var successCallback: OnPermissionSuccessCallback? = null
        var resultCallback: OnPermissionCallback? = null

        /**
         * 只有当所有权限都请求成功时，才会回调到这里，否则忽略结果
         *
         * 此方法和[onResult]互斥，当同时存在两个方法，符合此方法时，此方法优先
         */
        fun onSuccess(onAllSuccess: OnPermissionSuccessCallback) {
            successCallback = onAllSuccess
        }

        /**
         * 不管权限请求成功与否，都会回调到这里
         *
         * 此方法和[onSuccess]互斥，当同时存在两个方法，符合此方法和[onSuccess]方法时，[onSuccess]方法优先
         */
        fun onResult(onResult: OnPermissionCallback) {
            resultCallback = onResult
        }
    }

    /**
     * 权限分组信息
     *
     * [groupName] 分组名称，用在当用户拒绝时，提示需要的权限名称，例如：我们需要 电话 权限
     * [functionName] 功能名称，用在用户拒绝时，提示当前权限时为了什么功能，例如：我们需要 电话 权限，以正常使用 通话 功能
     */
    data class PermissionDesc(val groupName: String, val functionName: String)
}

/**
 * 请求权限，参见[MoonPermission.request]
 */
@MainThread
fun requestPermission(vararg permissions: String, config: MoonPermission.Config? = null, onAllSuccess: OnPermissionSuccessCallback) {
    CommonSdk.permission().request(*permissions, config = config, onAllSuccess = onAllSuccess)
}

/**
 * 请求权限，参见[MoonPermission.requestWithFailed]
 */
@MainThread
fun requestPermissionWithFailed(vararg permissions: String, config: MoonPermission.Config? = null, onResult: OnPermissionCallback) {
    CommonSdk.permission().requestWithFailed(*permissions, config = config, onResult = onResult)
}

/**
 * 请求权限，参见[MoonPermission.requestWithConfig]
 */
@MainThread
fun requestPermissionWithConfig(vararg permissions: String, configLambda: MoonPermission.ConfigCallback.() -> Unit) {
    CommonSdk.permission().requestWithConfig(*permissions, configLambda = configLambda)
}