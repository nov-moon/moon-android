package com.meili.moon.sdk.app.base

import android.content.Context
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.VersionUtils
import com.meili.moon.sdk.common.StartLambda
import com.meili.moon.sdk.log.LogUtil

/**
 * app当前运行的环境类型，用来在开发阶段动态全局配置各个集成环境
 *
 * 只有当前app是debug模式的时候，RuntimeType才会生效。否则将一直返回线上类型
 *
 * Created by imuto on 2019-06-28.
 */
object RuntimeType {

    /**
     * 强制指定当前运行环境，可在代码中随意更改。
     *
     * 他的优先级会高于用户自定义环境类型和开发类型
     */
    private val forceType: VersionUtils.BuildType? = null

    private var _currBuildType: VersionUtils.BuildType

    private val mSp = Sdk.app().getSharedPreferences("run_time_type", Context.MODE_PRIVATE)

    private var mCallbacks: MutableList<StartLambda>? = null

    init {
        val type = mSp.getInt("build_type", -1)
        _currBuildType = if (type >= 0) {
            VersionUtils.BuildType.values().find { type == it.value } ?: VersionUtils.getBuildType()
        } else {
            VersionUtils.getBuildType()
        }
    }

    /**
     * 当前的运行环境类型
     */
    var buildType: VersionUtils.BuildType
        get() {
            if (!VersionUtils.isDebug()) VersionUtils.BuildType.BUILD_TYPE_RELEASE

            LogUtil.d("runtimeType -> get:(forceType = $forceType currBuildType = $_currBuildType)")

            if (forceType != null) return forceType

            return _currBuildType
        }
        set(value) {

            if (forceType != null) return

            if (_currBuildType == value) return

            _currBuildType = value

            LogUtil.d("runtimeType -> set:(forceType = $forceType currBuildType = $_currBuildType value = $value)")

            mSp.edit().putInt("build_type", value.value).apply()

            mCallbacks?.forEach {
                it?.invoke()
            }
        }

    /**
     * 当前是否为开发环境
     */
    val isDebug: Boolean
        get() = buildType == VersionUtils.BuildType.BUILD_TYPE_DEBUG
    /**
     * 当前是否为测试环境
     */
    val isTest: Boolean
        get() = buildType == VersionUtils.BuildType.BUILD_TYPE_DEV
    /**
     * 当前是否为release环境
     */
    val isRelease: Boolean
        get() = buildType == VersionUtils.BuildType.BUILD_TYPE_RELEASE
    /**
     * 当前是否为自定义类型
     */
    val isCustom: Boolean
        get() = buildType == VersionUtils.BuildType.BUILD_TYPE_CUSTOM

    /**
     * 注册buildType的变更事件
     */
    fun registerChangeCallback(callback: StartLambda) {
        if (!VersionUtils.isDebug()) return

        if (mCallbacks == null) {
            mCallbacks = mutableListOf()
        }
        mCallbacks?.add(callback)
    }

    /**
     * 注解buildType的变更事件
     */
    fun unregisterChangeCallback(callback: StartLambda) {
        if (mCallbacks == null || callback == null) return

        mCallbacks?.remove(callback)
    }
}