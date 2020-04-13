package com.meili.moon.sdk.page.internal

import android.net.Uri
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.internal.utils.PageManagerUtils
import com.meili.moon.sdk.util.throwOnDebug
import com.meili.moon.sdk.util.toT

/**
 * 定义了一些常用的页面跳转Processor
 *
 * Created by imuto on 2019-08-30.
 */

/**
 * 通过pageName进行页面跳转的Processor
 */
object PageNameProcessor : PageProcessor {
    /**
     * 当前处理器是否和入参[intent]匹配，如果匹配，则[process]方法会得到调用，否则进行下个处理器匹配
     */
    override fun isMatch(intent: PageIntent): Boolean {
        return !intent.pageName.isNullOrEmpty()
    }

    /**
     * 处理页面跳转，入参和[PageManager.gotoPage]入参相同
     */
    override fun <T : Any> process(intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<in T>?) {
        PageManagerUtils.innerGotoPage(intent, canSameWithPre, destroyable, pageCallback.toT())
    }

    /**
     * 优先级，数值越大，优先级越高，标准优先级请参照：[Priority.PRIORITY_LOW]、[Priority.PRIORITY_NORMAL]、
     * [Priority.PRIORITY_HIGH]、[Priority.PRIORITY_MAX]
     */
    override val priority: Int = Priority.PRIORITY_LOW

}

/**
 * 匹配[RainbowConfig.appSchema]作为schema的处理器
 *
 * 一般作为外部打开app某个页面的schema使用
 */
object AppUriProcessor : UriProcessor() {
    /**
     * 经过简单处理的match方法，[intent]为当前页面跳转的数据，[uri]是根据[intent]生成的uri信息。
     * 一般情况下只使用[uri]进行判断就可以了，[uri]已经在父类验证过了schema和host不为空。
     */
    override fun isMatch(intent: PageIntent, uri: Uri): Boolean {
        if (uri.scheme != PageManagerImpl.getConfig().appSchema) {
            return false
        }
        return true
    }

    /**
     * 开始处理跳转逻辑，已经将[uri]上的参数，以String的方式填写到了[intent]中
     */
    override fun process(uri: Uri, intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<*>?) {
        intent.pageName = (uri.host ?: "") + (uri.path ?: "")
        if (uri.host == "page") {
            intent.pageName = uri.path ?: ""
        }
        PageManagerImpl.gotoPage(intent, canSameWithPre, destroyable, pageCallback)
    }

}

/**
 * 匹配[RainbowConfig.h5PageName]作为schema的处理器
 *
 * 作为打开H5页面的处理器，这里会添加下个页面的入参：
 * String类型 'url'名称的页面入参，可以通过arguments.getString("url")获取
 */
object H5UriProcessor : UriProcessor() {

    private const val schemaHttp = "http"
    private const val schemaHttps = "https"
    /**
     * 经过简单处理的match方法，[intent]为当前页面跳转的数据，[uri]是根据[intent]生成的uri信息。
     * 一般情况下只使用[uri]进行判断就可以了，[uri]已经在父类验证过了schema和host不为空。
     */
    override fun isMatch(intent: PageIntent, uri: Uri): Boolean {
        if (uri.scheme != schemaHttp && uri.scheme != schemaHttps) {
            return false
        }
        return true
    }

    /**
     * 开始处理跳转逻辑，已经将[uri]上的参数，以String的方式填写到了[intent]中
     */
    override fun process(uri: Uri, intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<*>?) {
        val config = PageManagerImpl.getConfig()
        val result = config.h5OpenProcessor?.invoke(uri, intent, canSameWithPre, destroyable, pageCallback.toT()) ?: false
        if (result) {
            return
        }
        if (config.h5PageName.isEmpty()) {
            throwOnDebug("请设置打开H5页面的默认pageName，设置方式为：PageSdk.page().getConfig().h5PageName = 你的H5PageName")
            return
        }

        intent.pageName = config.h5PageName
        intent.putExtra("url", uri.toString())
        PageManagerImpl.gotoPage(intent, canSameWithPre, destroyable, pageCallback)
    }
}

/**
 * Uri类型的页面处理器
 *
 * 主要用来处理：h5页面跳转、app内页面跳转等
 */
abstract class UriProcessor : PageProcessor {
    /**
     * 当前处理器是否和入参[intent]匹配，如果匹配，则[process]方法会得到调用，否则进行下个处理器匹配
     */
    override fun isMatch(intent: PageIntent): Boolean {
        try {
            val data = intent.pageName ?: intent.dataString

            val uri = Uri.parse(data)
            if (uri.scheme.isNullOrEmpty() || uri.host.isNullOrEmpty()) {
                return false
            }
            if (!isMatch(intent, uri)) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 经过简单处理的match方法，[intent]为当前页面跳转的数据，[uri]是根据[intent]生成的uri信息。
     * 一般情况下只使用[uri]进行判断就可以了，[uri]已经在父类验证了schema和host不为空。
     */
    abstract fun isMatch(intent: PageIntent, uri: Uri): Boolean

    /**
     * 处理页面跳转，入参和[PageManager.gotoPage]入参相同
     */
    override fun <T : Any> process(intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<in T>?) {
        val data = intent.pageName ?: intent.dataString

        val uri = Uri.parse(data)

        if (uri.scheme.isNullOrEmpty() || uri.host.isNullOrEmpty()) {
            throwOnDebug("打开页面时，没有匹配的Processor，请检查intent是否正确：$intent")
        }

        val queryParameterNames: MutableSet<String>? = uri.queryParameterNames

        queryParameterNames?.forEach {
            if (intent.hasExtra(it)) {
                return@forEach
            }
            intent.putExtra(it, uri.getQueryParameter(it))
        }

        process(uri, intent, canSameWithPre, destroyable, pageCallback)
    }

    /**
     * 开始处理跳转逻辑，已经将[uri]上的参数，以String的方式填写到了[intent]中。
     * [uri]已经在父类验证过了schema和host不为空。
     */
    abstract fun process(uri: Uri, intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<*>?)

    /**
     * 优先级，数值越大，优先级越高，标准优先级请参照：[Priority.PRIORITY_LOW]、[Priority.PRIORITY_NORMAL]、
     * [Priority.PRIORITY_HIGH]、[Priority.PRIORITY_MAX]
     */
    override val priority: Int = Priority.PRIORITY_NORMAL

}