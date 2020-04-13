package com.meili.moon.sdk.page

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.page.internal.RainbowActivity
import com.meili.moon.sdk.page.internal.utils.PageInterceptInfo

/**PageContainer的action*/
//const val ACTION_PAGES_CONTAINER = "action.com.meili.moon.sdk.page.router.container"

private const val RESULT_CODE = "activity_result_code"
private const val RESULT_RECEIVE_CANCEL = "activity_result_receive_cancel"

typealias OnPageResultCallback<T> = (result: T) -> Unit
typealias ContinueCallback = () -> Unit
typealias ContinueCallbackInner = (last: PageInterceptor) -> Unit

/**
 * 页面跳转管理器，提供页面的打开，结束，获取，检查等操作
 *
 * Created by imuto on 2018/3/30.
 */
interface PageManager {

    /**获取当前的顶部Activity*/
    fun getTopContainer(): PagesContainer?

    /**获取当前的顶部Activity*/
    fun getTopActivity(): Activity

    /**获取当前的顶部Fragment*/
    fun getTopPage(): Page?

    /**结束所有页面*/
    fun finishAll()

    /**结束指定affinity*/
    fun finishAffinity(pageIntent: PageIntent)

    /**
     * 从当前顶部fragment开始，结束指定步数的页面
     */
    fun finish(step: Int, page: Page)

    /**
     * 跳转到匹配intent的页面
     *
     * [canSameWithPre] 是否允许当前打开页面和上一个页面相同，默认不允许
     */
    fun <T : Any> gotoPage(intent: PageIntent, canSameWithPre: Boolean? = null, destroyable: Any? = null, pageCallback: OnPageResultCallback<in T>? = null)

    /**
     * 跳转到匹配intent的页面
     *
     * [canSameWithPre] 是否允许当前打开页面和上一个页面相同，默认不允许
     */
    fun gotoPage(intent: PageIntent, canSameWithPre: Boolean? = null)


    /**
     * 打开activity，如果需要请求结果的情况下，返回结果在intent中。
     *
     * Intent在此起作用的扩展：
     *
     * [Intent.resultCode] 当前结果的结果状态，对应activityResult中的resultCode
     *
     * [Intent.receiveCancelResult] 当前请求是否处理cancel情况，默认不处理，一般情况在用户直接返回时会触发cancel的结果回调
     *
     * 入参解释：
     *
     * [intent] 打开页面的配置参数
     *
     * [activityCallback] 如果此参数不为null，则打开方式为startActivityForResult方式
     *
     * 使用举例：
     *
     * ``` kotlin
     * gotoActivity(FirstActivity::class) {
     *      //设置传参
     *      putExtra("param1", 1)
     *
     *      //是否接收Cancel回调，默认不接收
     *      receiveCancelResult = true
     *
     *      //得到回调
     *      onResult {
     *          //回调结果的resultCode字段，一般对应Activity.RESULT_OK等
     *          it.resultCode
     *      }
     * }
     * ```
     */
    fun gotoActivity(intent: Intent, destroyable: IDestroable? = null, activityCallback: OnPageResultCallback<Intent>?)

    fun gotoActivity(intent: Intent)

    /**
     * 获取页面持有对象
     */
    fun getPagesHolder(): PagesHolder

    /**
     * 添加拦截器
     */
    fun registerInterceptor(interceptor: PageInterceptor)

    /**
     * 添加指定页面的拦截器
     *
     * 这里添加的[interceptor]拦截器只对[pageName]相同的页面有效，其他页面无效
     */
    fun registerInterceptor(pageName: String, interceptor: PageInterceptor)

    /**
     * 删除拦截器
     */
    fun unregisterInterceptor(interceptor: PageInterceptor)

    /**
     * 配置页面跳转相关内容，如果有多个配置，则最后一个配置生效.
     * 如果设置[isMainConfig] = true，则以此config为准，默认为false
     */
    fun config(config: RainbowConfig, isMainConfig: Boolean = false)

    /**
     * 获取当前的pageConfig
     */
    fun getConfig(): RainbowConfig


    /**
     * 注册页面
     * @param pageName 页面名称
     * @param pageClass 页面对应的类
     * @param args 页面入参
     * @param affinity 分组名称
     * @param flags 启动的flag设置，常用flag[android.content.Intent.FLAG_ACTIVITY_NEW_TASK]
     * @param launchMode 启动设置，例如常见的：[android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE]
     */
    fun <T : Page> registerPage(pageName: String, pageClass: Class<T>, args: Bundle? = null,
                                affinity: String? = null, flags: Int = 0, launchMode: Int = 0)

    /**
     * 注册页面处理器，用来处理页面跳转操作
     */
    fun registerProcessor(processor: PageProcessor)

    /**
     * 解注页面处理器
     */
    fun unregisterProcessor(processor: PageProcessor)
}

/**
 * 页面处理器，真正的用来处理页面跳转逻辑的处理对象
 *
 * 可以注册多个页面处理器，分别用来处理不同的页面操作。例如注册普通页面跳转、带schema的页面跳转、h5页面跳转等
 */
interface PageProcessor : Priority {

    /**
     * 当前处理器是否和入参[intent]匹配，如果匹配，则[process]方法会得到调用，否则进行下个处理器匹配
     */
    fun isMatch(intent: PageIntent): Boolean

    /**
     * 处理页面跳转，入参和[PageManager.gotoPage]入参相同
     */
    fun <T : Any> process(intent: PageIntent, canSameWithPre: Boolean, destroyable: Any? = null, pageCallback: OnPageResultCallback<in T>?)
}

/**
 * 优先级接口，定义了优先级变量和一些常用标准优先级值
 */
interface Priority {
    companion object {
        /**默认的低优先级*/
        val PRIORITY_LOW = 0
        /**默认的优先级*/
        val PRIORITY_NORMAL = 100
        /**默认的高优先级*/
        val PRIORITY_HIGH = 1000
        /**默认的最大优先级*/
        val PRIORITY_MAX = Int.MAX_VALUE - 100
    }

    /**
     * 优先级，数值越大，优先级越高，标准优先级请参照：[Priority.PRIORITY_LOW]、[Priority.PRIORITY_NORMAL]、
     * [Priority.PRIORITY_HIGH]、[Priority.PRIORITY_MAX]
     */
    val priority: Int
}

interface PageInterceptor : Priority {

    /**
     * 尝试拦截页面
     *
     * [info]中存放着打开页面的一些原始信息。如果你不拦截页面，直接返回false即可。如果你拦截了页面，则返回true。
     * 如果你拦截了页面并且做完相关操作后，想继续原来的打开操作，可调用[continueCallback]来完成原来的操作。
     *
     */
    fun intercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean

    /**
     * 可以添加白名单的抽象拦截器。
     *
     * 可以通过[addWhiteClass]方法添加指定页面的实现类到白名单，添加到白名单后，子类将不会收到拦截回调。
     * 白名单之外的其他所有页面跳转都会在[onIntercept]方法中得到回调，并根据逻辑需求做出相应操作。
     *
     * 类似的拦截器实现还有[AbsTargetInterceptor]
     */
    abstract class AbsWhiteInterceptor : PageInterceptor {

        /**白名单，不进行拦截的列表*/
        protected val whiteList = mutableListOf<Class<*>>()

        override fun intercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean {
            val clazz = info.targetClazz
            if (clazz != null && whiteList.contains(clazz)) {
                return false
            }
            return onIntercept(info, continueCallback)
        }

        /**
         * 子类的拦截器回调方法，会根据规则排除白名单内容后回调此方法
         */
        abstract fun onIntercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean

        /**
         * 添加白名单
         */
        fun addWhiteClass(clazz: Class<*>) {
            whiteList.add(clazz)
        }

        /**
         * 删除白名单
         */
        fun removeWhiteClass(clazz: Class<*>) {
            whiteList.remove(clazz)
        }
    }

    /**
     * 可以添加目标拦截类的抽象拦截器。
     *
     * 可以通过[addTargetClass]方法添加指定页面的类到目标列表，子类能在[onIntercept]中收到目标列表中类拦截回调
     * 拦截列表之外的其他所有页面跳转都不能得到回调
     *
     * 类似的拦截器实现还有[AbsWhiteInterceptor]
     */
    abstract class AbsTargetInterceptor : PageInterceptor {

        /**目标列表，进行拦截的列表*/
        protected val targetList = mutableListOf<Class<*>>()

        override fun intercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean {
            val clazz = info.targetClazz
            if (clazz != null && targetList.contains(clazz)) {
                return onIntercept(info, continueCallback)
            }
            return false
        }

        /**
         * 子类的拦截器回调方法，会根据规则只接收目标列表中的跳转拦截回调
         */
        abstract fun onIntercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean

        /**
         * 添加目标拦截类
         */
        fun addTargetClass(clazz: Class<*>) {
            targetList.add(clazz)
        }

        /**
         * 移除目标拦截类
         */
        fun removeTargetClass(clazz: Class<*>) {
            targetList.remove(clazz)
        }
    }

}

/**
 * rainbow的配置选项。
 *
 * 配合[PageManager.config]方法使用，可定义一些Rainbow库中的常用配置策略
 */
class RainbowConfig {

    var pageContainer: Class<*> = RainbowActivity::class.java

    /**
     * 是否支持沉浸式状态栏，默认支持
     */
    var isTranslucentStatusBar: Boolean = true

    /**
     * 当前app的自定义schema
     * 一般用来支持url打开页面，例如外部打开app或者内部通过url打开页面，
     * 例如：meili://page/login
     */
    var appSchema: String = "meili"

    /**
     * 通用的H5页面pageName
     *
     * 用来设置通用h5的处理页面，一般一个应用只需要定义一个集成了webview的H5处理页面即可。
     * 如果要打开h5，必须初始化此属性
     */
    var h5PageName: String = ""

    /**
     * 打开H5的方式
     *
     * 本方法和[h5PageName]都是进行H5页面处理的，本方法优先进行判断，如果本方法的入参返回了true，则[h5PageName]不生效。
     * 如果本方法为null，则再判断[h5PageName].
     *
     * [uri]是当前url生成的对象，可通过toString()方法获取url。[intent]是当前打开操作的其他参数内容。
     *
     */
    var h5OpenProcessor: ((uri: Uri, intent: PageIntent, canSameWithPre: Boolean, destroyable: Any?, pageCallback: OnPageResultCallback<Any>?) -> Boolean)? = null

    /**
     * 是否可以连续打开两个相同的页面，默认不可以。因为大多数连续打开相同页面的原因是用户误点击造成的。
     * 另外一个类似的功能设置[canOpenSamePage]。这里的设置优先处理，这里的配置只对两个连续打开有效
     */
    var canOpenSamePageInSuccession = false

    /**
     * 是否可以打开两个相同的页面
     *
     * pageName+nickName相同就是相同页面，默认可以打开相同页面。
     * 如果这里设置了false，当打开页面时，会尝试在堆栈中查找相同页面，如果找到了则不打开新页面，只是返回目标页面
     */
    var canOpenSamePage: Boolean = true

    /**
     * 设置通用的softInputMode
     *
     * 这个值用来管理当前app和软键盘的交互。默认值为：resize。
     * 更多可选参数：[WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN]、
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE]、
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN]、
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED]、
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED]、
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE]、
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING]、
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN]、
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED]、
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE]
     */
    var softInputMode: Int = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
}

/**当前请求返回的结果码*/
var Intent.resultCode: Int
    get() = getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED)
    set(value) {
        putExtra(RESULT_CODE, value)
    }
/**当前请求是否处理cancel情况*/
var Intent.receiveCancelResult: Boolean
    get() = getBooleanExtra(RESULT_RECEIVE_CANCEL, false)
    set(value) {
        putExtra(RESULT_RECEIVE_CANCEL, value)
    }