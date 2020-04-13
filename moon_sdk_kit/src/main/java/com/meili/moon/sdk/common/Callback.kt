package com.meili.moon.sdk.common

import java.lang.reflect.Type
import kotlin.reflect.KType

/**等待回调*/
typealias WaitingLambda = (() -> Unit)?

/**开始回调*/
typealias StartLambda = (() -> Unit)?

/**缓存回调：（缓存内容，一般为json对象）*/
typealias CacheLambda = ((cache: String) -> Boolean)?

/**进度回调：（当前进度，总进度）*/
typealias ProgressLambda = ((progress: Long, total: Long) -> Unit)?

/**成功的结果回调：（结果对象）*/
typealias SuccessLambda<T> = ((result: T) -> Unit)?

/**失败回调：（失败原因）*/
typealias ErrorLambda = ((exception: BaseException) -> Unit)?

/**取消回调：（是否是用户取消）*/
typealias CancelLambda = ((isByUser: Boolean) -> Unit)?

/**完成回调：（是否成功）*/
typealias FinishLambda = ((isSuccess: Boolean) -> Unit)?

/**
 * callback的统一标准类
 * Created by imuto on 17/11/22.
 */
interface Callback {

    /**开始执行的callback*/
    interface StartedCallback : Callback {
        fun onStarted()
    }

    interface WaitingCallback : Callback {
        fun onWaiting()
    }

    interface CacheCallback : Callback {
        /**
         * 是否信任缓存
         * [cacheBody] 缓存内容
         *
         * @return true 信任，false 不信任
         */
        fun isTrustCache(cacheBody: String): Boolean
    }

    /**执行成功的callback*/
    interface SuccessCallback<in ResultType> : Callback {
        fun onSuccess(result: ResultType)
    }

    /**错误的callback*/
    interface ErrorCallback : Callback {
        fun onError(exception: BaseException)
    }

    /**取消的callback*/
    interface CancelCallback : Callback {
        fun onCancelled(byUser: Boolean)
    }

    /**结束callback*/
    interface FinishedCallback : Callback {
        fun onFinished(isSuccess: Boolean)
    }

    /**进度callback*/
    interface ProgressCallback : Callback {
        fun onProgress(curr: Long, total: Long)
    }

    /**可以提供type的callback*/
    interface Typed : Callback {
        var typed: Type?
    }

    /**可以提供type的callback*/
    interface KTyped : Callback {
        var typed: KType
    }

    /**常用的httpCallback，提供成功、失败、结束回调*/
    interface IHttpCallback<in ResultType> : SuccessCallback<ResultType>, ErrorCallback, FinishedCallback

    open class SimpleCallback<in ResultType> : IHttpCallback<ResultType>, StartedCallback, WaitingCallback ,ProgressCallback{
        override fun onProgress(curr: Long, total: Long) {

        }

        override fun onWaiting() {
        }

        override fun onStarted() {
        }

        override fun onSuccess(result: ResultType) {
        }

        override fun onError(exception: BaseException) {
        }

        override fun onFinished(isSuccess: Boolean) {
        }
    }
}