package com.meili.moon.sdk.app.util

import android.support.annotation.MainThread
import com.meili.moon.sdk.app.callback.SimpleKTypedCallback
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.common.UEHttpHolder
import com.meili.moon.sdk.base.util.OnNormalCallback
import com.meili.moon.sdk.base.util.toT
import com.meili.moon.sdk.common.*
import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.util.throwOnDebug
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect

private val tasksMap = mutableMapOf<IDestroable, MutableList<Cancelable>>()

/**error的lambda表达式, 返回值为是否使用默认交互，true是，false否*/
typealias LambdaErrorWithMsg = ((errorMessage: String?, exception: BaseException) -> Unit)?

/**链式调用中当前节点的结果对应类型*/
enum class HttpLinkageTypeEnum {
    ON_ERROR,
    ON_SUCCESS,
    ON_FINISH
}

/**去掉掉指定IDestroable的tasks，只能取消[httpGet]、[httpPost]等方式调用的task*/
@MainThread
fun IDestroable.cancelHttpTasks() {
    val httpTasks = getHttpTasks()
    httpTasks?.forEach {
        try {
            it.cancel(true)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
    httpTasks?.clear()
}

/**获取指定IDestroable的tasks，只能获取[httpGet]、[httpPost]等方式调用的task*/
@MainThread
fun IDestroable.getHttpTasks(): MutableList<Cancelable>? {
    return tasksMap[this]
}

/**发起一个带有UE交互的get请求*/
@MainThread
fun IDestroable.httpGet(lambda: HttpExtra.() -> Unit): HttpLinkageNode {
    return httpRequest(lambda, HttpMethod.GET)
}

/**发起一个带有UE交互的post请求*/
@MainThread
fun IDestroable.httpPost(lambda: HttpExtra.() -> Unit): HttpLinkageNode {
    return httpRequest(lambda, HttpMethod.POST)
}

/**http请求的一个扩展封装，支持UI交互和销毁判断*/
class HttpExtra(private val method: HttpMethod, private val destroyable: IDestroable? = null) {

    /**http请求的请求参数，此参数必须赋值，否则报错*/
    lateinit var params: IRequestParams.IHttpRequestParams
    /**当前请求UE的类型，此参数在[UEHttpHolder.showUEErrorMessage]、[UEHttpHolder.showUEProgress]、[UEHttpHolder.dismissUEProgress]*/
    var ueType: Int = 0
    /**如果当前请求需要弹出progress，这里定义progress上的信息*/
    var ueProcessMsg: String? = null
    /**定义链式请求的请求模式，比如上一个请求为success的时候，才执行当前请求。默认为success*/
    var executeType: HttpLinkageTypeEnum = HttpLinkageTypeEnum.ON_SUCCESS
    /**当前请求是否为后台请求，如果是后台请求，则任何环节都没有UE*/
    var isBackgroundWork = false

    /**当前UE的控制对象*/
    private var ueHolder: UEHttpHolder? = destroyable.toT()

    /**链式对象的下一个请求*/
    private var nextRequest: HttpLinkageNode = HttpLinkageNode(destroyable)

    private var mSuccessLambda: SuccessLambda<Any> = null
    private var mErrorLambda: LambdaErrorWithMsg = null
    private var mFinishLambda: FinishLambda = null
    private var currTask: Cancelable? = null

    /**接收成功回调*/
    fun <T> onSuccess(successLambda: SuccessLambda<T>) {
        mSuccessLambda = successLambda as SuccessLambda<Any>
    }

    /**接收失败回调*/
    fun onError(errorLambda: LambdaErrorWithMsg) {
        mErrorLambda = errorLambda
    }

    /**接收完成回调*/
    fun onFinish(finishLambda: FinishLambda) {
        mFinishLambda = finishLambda
    }

    /**一般情况下，外部不会使用本方法，慎用*/
    fun request() {
        val lambda = mSuccessLambda

        var kType: KType? = null
        try {
            kType = if (lambda != null) {
                val reflect = lambda.reflect()
                var result: KType? = null
                if (reflect != null) {
                    result = reflect.parameters[0].type
                }
                result
            }
            else {
                String::class.createType()
            }

        }
        catch (e: Throwable) {
            e.printStackTrace()
            processDelayUE {
                processErrorUE(e.message, BaseException(cause = e))
            }
        }

        if (kType == null) {
            throwOnDebug(BaseException("ktype相关参数解析错误"))
            return
        }

        if (!isBackgroundWork) {
            ueHolder?.showUEProgress(ueProcessMsg, ueType)
        }
        try {
            val request = Sdk.http().request(method, params, object : SimpleKTypedCallback<Any>(kType) {
                override fun onSuccess(result: Any) {
                    super.onSuccess(result)
                    processDelayUE {
                        processUEInternal(HttpLinkageTypeEnum.ON_SUCCESS) {
                            mSuccessLambda?.invoke(result)
                        }
                    }
                }

                override fun onError(exception: BaseException) {
                    super.onError(exception)
                    processDelayUE {
                        processErrorUE(exception.message, exception)
                    }
                }

                override fun onFinished(isSuccess: Boolean) {
                    super.onFinished(isSuccess)
                    processDelayUE {
                        processUEInternal(HttpLinkageTypeEnum.ON_FINISH) {
                            mFinishLambda?.invoke(isSuccess)
                        }
                    }
                    removeHttpTask(destroyable, currTask)
                }
            })

            currTask = request

            addHttpTask(destroyable, request)
        }
        catch (throwable: Throwable) {
            processDelayUE {
                processErrorUE(throwable.message, BaseException(cause = throwable))
            }
            processDelayUE {
                processUEInternal(HttpLinkageTypeEnum.ON_FINISH) {
                    mFinishLambda?.invoke(false)
                }
            }
        }

    }

    @Throws(Throwable::class)
    private fun processDelayUE(lambda: OnNormalCallback) {
        var delay = ueHolder?.getUEDelayMills() ?: 0
        if (delay > 0) {
            delay += 100
            Sdk.task().post(delay) {
                lambda?.invoke()
            }
        }
        else {
            lambda?.invoke()
        }
    }

    @Throws(Throwable::class)
    private fun processUEInternal(type: HttpLinkageTypeEnum, lambda: OnNormalCallback) {
        if (destroyable?.hasDestroyed == true) {
            ueHolder?.apply {
                if (isBackgroundWork) {
                    return
                }
                //如果是success不做对话框消失操作,放到onFinish中操作.
                if (type == HttpLinkageTypeEnum.ON_SUCCESS) {
                    return
                }
                dismissUEProgress(ueType)
            }
            return
        }

        lambda?.invoke()

        val extra = nextRequest.execute(type)

        ueHolder?.apply {
            if (isBackgroundWork) {
                return
            }
            if (extra != null && !extra.isBackgroundWork) {
                return
            }
            //如果是success不做对话框消失操作,放到onFinish中操作.
            if (type == HttpLinkageTypeEnum.ON_SUCCESS) {
                return
            }
            dismissUEProgress(ueType)
        }
    }

    private fun processErrorUE(msg: String?, exception: BaseException) {
        val uiHolder = this@HttpExtra.ueHolder

        if (destroyable?.hasDestroyed == true) {
            uiHolder?.dismissUEProgress(ueType)
            return
        }

        mErrorLambda?.invoke(msg, exception)

        val extra = nextRequest.execute(HttpLinkageTypeEnum.ON_ERROR)

        if (isBackgroundWork || uiHolder == null) {
            return
        }

//        if (extra == null || extra.isBackgroundWork) {
//            uiHolder.dismissUEProgress(ueType)
//        }
        uiHolder.showUEErrorMessage(msg, ueType)
    }

    fun getLinkage(): HttpLinkageNode {
        return nextRequest
    }
}

/**http链式请求的一个node*/
class HttpLinkageNode(private val uiHolder: IDestroable? = null) {

    private var httpExtra: HttpExtra? = null

    /**发起一个get请求*/
    fun get(lambda: HttpExtra.() -> Unit): HttpLinkageNode {
        return request(lambda, HttpMethod.GET)
    }

    /**发起一个post请求*/
    fun post(lambda: HttpExtra.() -> Unit): HttpLinkageNode {
        return request(lambda, HttpMethod.POST)
    }

    /**执行请求，一般用在链式请求里的上一个请求调用，其他情况请慎用，一般你是用不到这个方法的*/
    fun execute(executeType: HttpLinkageTypeEnum): HttpExtra? {
        val extra = httpExtra ?: return null
        if (extra.executeType != executeType) {
            return null
        }

        Sdk.task().post { extra.request() }

        return extra
    }

    private fun request(lambda: HttpExtra.() -> Unit, method: HttpMethod): HttpLinkageNode {
        val httpExtra = HttpExtra(method, uiHolder)
        httpExtra.apply {
            lambda()
        }
        this.httpExtra = httpExtra
        return httpExtra.getLinkage()
    }
}

@MainThread
private fun IDestroable.httpRequest(lambda: HttpExtra.() -> Unit, method: HttpMethod): HttpLinkageNode {
    val httpExtra = HttpExtra(method, this)
    httpExtra.apply {
        lambda()
        request()
    }
    return httpExtra.getLinkage()
}

@MainThread
private fun addHttpTask(destroyable: IDestroable?, cancelable: Cancelable) {
    destroyable ?: return
    var httpTasks = destroyable.getHttpTasks()

    if (httpTasks == null) {
        httpTasks = mutableListOf()
        tasksMap[destroyable] = httpTasks
    }

    httpTasks.add(cancelable)
}

@MainThread
private fun removeHttpTask(destroyable: IDestroable?, cancelable: Cancelable?) {
    destroyable ?: return
    cancelable ?: return
    val httpTasks = destroyable.getHttpTasks() ?: return
    httpTasks.remove(cancelable)
}