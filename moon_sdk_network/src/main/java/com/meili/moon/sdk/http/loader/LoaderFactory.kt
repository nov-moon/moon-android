package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.common.Callback
import com.meili.moon.sdk.common.ProgressLambda
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.IRequestTracker
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.ParameterizedTypeUtil
import com.meili.moon.sdk.util.isArrayType
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * loader的工厂类
 * Created by imuto on 17/12/4.
 */
object LoaderFactory {
    fun <T> getLoader(param: IHttpRequestParams, progress: ProgressLambda, canMock: Boolean = true): IResourceLoader<*> {
        val resultClass = param.response.itemKClass
        return if (canMock && param.isSupportMock()) {
            MockDataLoader<T>(param)
        } else if (resultClass != File::class.java) {
            DefHttpLoader<T>(param)
        } else {
            DefDownLoader(param, progress)
        }
    }


    // 解析loadType
    private fun resolveLoadType(clazz: Class<*>): Type {
        return if (clazz is Callback.Typed) {
            (clazz as Callback.Typed).typed!!
        } else {
            ParameterizedTypeUtil.getParameterizedType(clazz, Callback.IHttpCallback::class.java, 0)
        }
    }

    private fun resolveResultClass(loadType: Type): Class<*> {
        return if (loadType.isArrayType()) {
            val parameterizedType = ParameterizedTypeUtil.getParameterizedType(loadType, List::class.java, 0)
            if (parameterizedType is ParameterizedType) {
                parameterizedType.rawType as Class<*>
            } else {
                parameterizedType as Class<*>
            }
        } else {
            loadType as Class<*>
        }
    }

    private var mDefTrackerClass: Class<out IRequestTracker>? = null

    /**
     * 注册默认日志追踪器
     */
    fun registerDefaultTracker(trackerClass: Class<out IRequestTracker>) {
        mDefTrackerClass = trackerClass
    }

    /**
     * 获取默认的网络请求日志追踪器，可以使用registerDefaultTracker方法注册默认追踪器
     */
    fun getDefaultTracker(): IRequestTracker? {
        try {
            if (mDefTrackerClass != null) {
                return mDefTrackerClass!!.newInstance()
            }
        } catch (e: Exception) {
            LogUtil.e("生成默认日志追踪器错误", e)
        }

        return null
    }

}

internal fun IHttpRequestParams.isSupportMock(): Boolean {
    return (this is IRequestParams.MockFeatures
            && (this.getUseMockDataStrategy() == IRequestParams.MockFeatures.MockStrategy.ON
            || this.getUseMockDataStrategy() == IRequestParams.MockFeatures.MockStrategy.ON_FORCE))
}

internal fun IHttpRequestParams.isForceMock(): Boolean {
    return (this is IRequestParams.MockFeatures
            && this.getUseMockDataStrategy() == IRequestParams.MockFeatures.MockStrategy.ON_FORCE)
}