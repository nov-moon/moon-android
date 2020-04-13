@file:Suppress("DEPRECATION", "unused", "UNCHECKED_CAST")

package com.meili.moon.sdk.page.internal.utils

import android.content.Intent
import android.content.pm.ActivityInfo
import android.text.TextUtils
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.exception.StartPageException
import com.meili.moon.sdk.page.internal.*
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.post
import com.meili.moon.sdk.util.throwOnDebug
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by imuto on 2019-08-15.
 */

/**页面拦截器*/
private var mProcessors = mutableListOf<ProcessorWrapper>()

internal fun PageManagerImpl.innerFinish(step: Int, page: Page) {
    PageStackManager.INSTANCE.finish(step, page as SdkFragment)
}

/**根据[containerId]获取当前container*/
fun PageManagerImpl.getContainer(containerId: Long): PagesContainer? {
    return PageStackManager.INSTANCE.getContainer(containerId)
}

/**根据[containerId]获取当前container的前一个container*/
fun PageManagerImpl.getPreContainer(containerId: Long): PagesContainer? {
    return PageStackManager.INSTANCE.getPreContainer(containerId)
}

// 序号生成器
private val mSequence = AtomicInteger(0)

/**
 * 尝试使用拦截器对当前intent进行拦截
 *
 * 如果拦截成功，则返回true，没有可拦截对象，则返回false。
 * 如果拦截成功，并且使用了异步拦截，进行了相关操作后，想继续当前页面操作，则会在[continueCallback]中进行回调，
 * 并在其中继续进行页面打开操作。
 */
internal fun Intent.intercept(interceptors: MutableList<InterceptorWrapper>, lastInterceptor: PageInterceptor?, continueCallback: ContinueCallbackInner): Boolean {
    // 这里加了一个post的原因是，在测试的时候，如果拦截的是打开activity，
    // 那么如果做后续操作的时候，activityResult获取不到。加了post，就能正确获取onActivityResult
    val info = if (this is PageIntent && this.pageName.isNotEmpty()) {
        PageInterceptInfo(this, PageManagerImpl.getPagesHolder().findClassByPageName<Page>(this.pageName), pageName)
    } else {
        try {
            PageInterceptInfo(this, Class.forName(this.component.className))
        } catch (e: Exception) {
            PageInterceptInfo(this, null)
        }
    }

    var index = -1
    if (lastInterceptor != null) {
        index = interceptors.indexOfFirst { it.interceptor == lastInterceptor }
    }

    ((index + 1) until interceptors.size).forEach {
        val itemInterceptor = interceptors[it]
        // 拦截器的PageName和info的pageName不同，代表他们有自己指定的拦截器信息，所以不处理，进行下一个循环
        if (itemInterceptor.pageName != null && itemInterceptor.pageName != info.pageName) return@forEach

        val interceptor = itemInterceptor.interceptor
        val result = interceptor.intercept(info) { post { continueCallback(interceptor) } }
        if (result) return true
    }

    return false
}

/**
 * 添加一个页面拦截器
 *
 * 使用拦截器优先级进行排序，如果优先级相同，则使用当前拦截器的添加顺序进行排序。
 *
 */
internal fun PageManagerImpl.addInterceptor(list: MutableList<InterceptorWrapper>,
                                            interceptor: PageInterceptor, pageName: String? = null) {

    val findInterceptor = list.find { it.interceptor == interceptor }
    if (findInterceptor != null) {
        return
    }

    val wrapper = InterceptorWrapper(interceptor, mSequence.getAndIncrement(), pageName)

    list.add(wrapper)
    list.sortDescending()
}


/**
 * 添加一个页面处理器
 *
 * 使用优先级进行排序，如果优先级相同，则使用当前的添加顺序进行排序。
 *
 */
internal fun PageManagerImpl.processor(intent: PageIntent, canSameWithPre: Boolean?,
                                       destroyable: Any?, pageCallback: OnPageResultCallback<*>?) {

    val find = mProcessors.find { it.processor.isMatch(intent) }

    if (find == null) {
        throwOnDebug("打开页面时，没有匹配的Processor，请检查intent是否正确：$intent")
        return
    }
    val canSameWithPreVal = canSameWithPre ?: getConfig().canOpenSamePageInSuccession

    find.processor.process(intent, canSameWithPreVal, destroyable, pageCallback)
}

/**
 * 添加一个页面处理器
 *
 * 使用优先级进行排序，如果优先级相同，则使用当前的添加顺序进行排序。
 *
 */
internal fun PageManagerImpl.addProcessor(processor: PageProcessor) {

    val findInterceptor = mProcessors.find { it.processor == processor }
    if (findInterceptor != null) {
        return
    }

    val wrapper = ProcessorWrapper(processor, mSequence.getAndIncrement())

    mProcessors.add(wrapper)
    mProcessors.sortDescending()
}

/**
 * 移除一个页面处理器
 */
internal fun PageManagerImpl.removeProcessor(processor: PageProcessor) {

    val findInterceptor = mProcessors.find { it.processor == processor }
    if (findInterceptor != null) {
        mProcessors.remove(findInterceptor)
    }
}

internal class InterceptorWrapper(var interceptor: PageInterceptor, private val sequence: Int, val pageName: String? = null) :
        Comparable<InterceptorWrapper> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: InterceptorWrapper): Int {
        return if (other.interceptor.priority == interceptor.priority) {
            sequence - other.sequence
        } else interceptor.priority - other.interceptor.priority
    }

}

internal class ProcessorWrapper(var processor: PageProcessor, private val sequence: Int) :
        Comparable<ProcessorWrapper> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: ProcessorWrapper): Int {
        return if (other.processor.priority == processor.priority) {
            sequence - other.sequence
        } else processor.priority - other.processor.priority
    }

}

/**
 * 页面拦截信息
 *
 * [intent]包含了当前页面跳转的所有信息。
 * 在大多数拦截器处理页面时，可能需要跳转目标页面的信息，[targetClazz]用来提供这一信息，他有可能为null。
 *
 */
class PageInterceptInfo(val intent: Intent, val targetClazz: Class<*>?, val pageName: String? = null)


object PageManagerUtils {

    @JvmStatic
    fun openPageForResult(requestCode: Int, intent: PageIntent, canSameWithPre: Boolean) {
        if (requestCode < 0) {
            innerGotoPage(intent, canSameWithPre, null, null)
            return
        }

        if (intent.avoidOpenSamePage(canSameWithPre)) return

        intent.fix()

        if (intent.isWrongful()) {
            val msg = "page not find, pageName = ${intent.pageName} pageClass = ${intent.pageClass}"
            throwOnDebug(StartPageException(msg = msg))
            return
        }

        val pageClass: Class<*>? = intent.pageClass

        if (pageClass == null || SdkActivity::class.java.isAssignableFrom(pageClass.superclass)) {
            PageStackManager.INSTANCE.startActivity(intent, requestCode)
        } else if (SdkFragment::class.java.isAssignableFrom(pageClass)) {
            try {
                val info = intent.activityInfo
                val single = info.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE
                if (single) {
                    PageStackManager.INSTANCE.gotoFragment(intent, requestCode)
                } else {
                    PageStackManager.INSTANCE.startFragment(intent, requestCode)
                }
            } catch (ex: Exception) {
                throwOnDebug(StartPageException(msg = "open page error: " + intent.pageName, cause = ex))
            }
        } else {
            throwOnDebug(StartPageException(msg = "page open failed, no holder to process"))
        }
    }

    @JvmStatic
    fun innerGotoPage(intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<Any>?) {
        if (intent.avoidOpenSamePage(canSameWithPre)) return

        intent.fix()

        if (intent.isWrongful()) {
            val msg = "page not find, pageName = ${intent.pageName} pageClass = ${intent.pageClass}"
            throwOnDebug(StartPageException(msg = msg))
            return
        }

        if (pageCallback != null) {
            PageCallbackHolder.registerCallback(intent, pageCallback, destroyable)
        }

        val pageClass = intent.pageClass
        /*
            1.如果pageClass为null,或者pageClass是BaseActivity的子类,则尝试启动这个intent的activity
            2.如果pageClass是BaseFragment的子类,则尝试打开这个Fragment
         */
        if (pageClass == null || SdkActivity::class.java.isAssignableFrom(pageClass)) {
            PageStackManager.INSTANCE.gotoActivity(intent, pageClass)
        } else if (SdkFragment::class.java.isAssignableFrom(pageClass)) {
            try {
                PageStackManager.INSTANCE.gotoFragment(intent, -1)
            } catch (ex: Exception) {
                throw StartPageException(msg = "open page error: " + intent.pageName, cause = ex)
            }

        } else {
            val msg = "${pageClass.name} must extends BaseActivity or BaseFragment"
            throwOnDebug(StartPageException(msg = msg))
        }
    }


    /**是否是错误的Intent*/
    internal fun PageIntent.isWrongful(): Boolean {
        val pageClass = pageClass
        // 是不合法的page
        val isNilPage = !TextUtils.isEmpty(pageName) && pageClass == null
        // 是否可以通过普通activity方式处理
        val isProcessByActivity = pageClass == null || SdkActivity::class.java.isAssignableFrom(pageClass)
        // 是否可以通过fragment方式处理
        val isSdkFragment = pageClass != null && SdkFragment::class.java.isAssignableFrom(pageClass)

        return isNilPage || (!isProcessByActivity && !isSdkFragment)
    }

    /**
     * 避免由于用户误按，连续打开相同的页面
     *
     * 通过生成最后的页面id，和两次打开页面的时间间隔做判断
     *
     * @return true 是误按造成的相同页面，否则不是
     */
    internal fun PageIntent.avoidOpenSamePage(canSameWithPre: Boolean): Boolean {

        var pageNameVar = pageName ?: ""
        if (pageNameVar.startsWith("/")) {
            pageNameVar = pageNameVar.substring(1)
        }
        if (pageNameVar.endsWith("/")) {
            pageNameVar = pageNameVar.substring(0, pageNameVar.length - 1)
        }

        pageName = pageNameVar

        val newPageIdentity = "$action,$component,$pageName,$nickName"

        val sdkFragment = PageManagerImpl.getTopPage() as? SdkFragment
        sdkFragment ?: return false

        val prePageIntent = sdkFragment.pageIntent
        val lastPageIdentity = "${prePageIntent.action},${prePageIntent.component}," +
                "${prePageIntent.pageName},${prePageIntent.nickName}"

        return newPageIdentity == lastPageIdentity && !canSameWithPre
    }

    /**完善pageIntent信息*/
    internal fun PageIntent.fix() {
        val pagesHolder = PageManagerImpl.getPagesHolder()

        var pageClass: Class<*>? = null
        if (!isEmpty(pageName)) {
            pageClass = pagesHolder.findClassByPageName<Page>(pageName)
        }

        if (pageClass == null && component != null) {
            try {
                pageClass = Class.forName(component!!.className)
            } catch (ex: ClassNotFoundException) {
                throw StartPageException(msg = "open page error: $pageName", cause = ex)
            }
        }

        this.pageClass = pageClass

        if (pageClass != null && !TextUtils.isEmpty(pageName)) {
            val info = pagesHolder.findInfoByPageName(pageName)
            activityInfo = info
        }

//        if (Rainbow.config.canOpenSamePage && TextUtils.isEmpty(nickName)) {
//            nickName = "${System.currentTimeMillis()}"
//        }

        pagesHolder.tryRedirector(this)
    }
}
