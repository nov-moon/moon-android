package com.meili.moon.sdk.page.internal

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.TAG
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.internal.animators.PageAnimator
import com.meili.moon.sdk.page.internal.animators.PageAnimatorWrapper
import com.meili.moon.sdk.page.internal.animators.PageRatioAnimators
import com.meili.moon.sdk.page.internal.utils.*
import com.meili.moon.sdk.page.internal.widget.RainbowPageRootView
import com.meili.moon.sdk.util.getClassDelegate
import java.util.*
import kotlin.reflect.KClass

/**
 * 实现的page页面路由的page基类
 *
 * 主要实现的功能有：
 *
 * 1. 页面之间数据传输的数据保存
 * 2. fragment的生命周期维护
 * 3. fragment的动画执行后，任务队列执行
 * 4. fragment的一些页面状态记录
 *
 * Created by imuto on 2018/4/9.
 */
@Suppress("KDocUnresolvedReference", "DEPRECATION", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
abstract class SdkFragment : Fragment(), Page, PageAnimator by PageAnimatorWrapper(), PageLifeCycle {

    override var hasDestroyed: Boolean = false
        get() = field || isDirty

    /**
     * 持有本页面的activity，请谨慎使用此对象，因为此对象在页面没有attach前是为null
     */
    override lateinit var pageActivity: Activity

    override var requestCode: Int = 0

    override val pageName: String
        get() {
            var result = pageIntent.pageName
            if (TextUtils.isEmpty(result)) {
                result = this.javaClass.name
            }
            return result
        }

    override val nickName: String
        get() = pageIntent.nickName ?: ""

    override val isChildPage: Boolean
        get() {
            return fragmentManager != (pageActivity as FragmentActivity).supportFragmentManager
        }

    @Deprecated(message = "请框架使用者不要直接使用此属性，可能会引起诡异的bug")
    override var pageIntent: PageIntent = PageIntent()

    override var interactive: Boolean = true

    override var pageAnimators: PageAnimators? = null

    override var containerId: Long = 0

    /**拦截页面finish等跳转操作的委托对象，一般用来做子fragment的跳转处理*/
    var pageManagerDelegate: SdkFragment? = null

    /**是否打开滑动关闭，默认打开*/
    var isSlideFinish = true

    /**滑动关闭的最小Y值检测，一般用来设置titleBar高度，titleBar位置不能滑动退出*/
    @Deprecated(message = "已经修复手势滑动bug，应该不用此方法了，如果还必须使用请联系开发")
    var slideFinishMinY = -1
        protected set(value) {
            field = value
        }

    /**当前fragment的containerView*/
    protected var container: ViewGroup? = null

    private var _result: Any? = null

    internal var mView: View? = null

    /**contentView的父view*/
    lateinit var mViewParent: RainbowPageRootView
        private set

    /**记录当前结束状态*/
    private var mFinishing: Boolean = false

    /**记录当前页面是否已经暂停*/
    private var paused = false

    /**当前动画状态  0: idle, 1: start, 2: end*/
    private var pageAnimationState: Byte = 0

    /**记录请求队列*/
    private val postOnPageAnimationRunnable = RunnableList()

    /**动画锁*/
    private val postOnPageAnimationEndLock = Any()

    /**在fragment跳转期间执行的进度动画对象*/
    protected var fragmentAnimator: Animator? = null
        private set

    /**PageResult的拦截器*/
    var onPageResultHolder: ((requestCode: Int, resultCode: Int, intent: Intent?) -> Boolean)? = null

    /**是否打开日志*/
    open val isDebugLog = false

    /*---------------------------------------子类可能重写的方法---------------------------------------*/

    override fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent) {
        if (isDebugLog) {
            "$TAG -> onPageResult($requestCode, $resultCode)".log()
        }
    }

    /**
     * 退出页面之前调用
     *
     * @return true表示已处理back事件不会做任何事情, false
     */
    open fun onPreFinish(): Boolean {
        if (isDebugLog) {
            "$TAG -> onPreFinish".log()
        }
        return false
    }

    init {
    }

    /*---------------------------------------fragment的状态和生命周期维护---------------------------------------*/

    @SuppressLint("ClickableViewAccessibility")
    @Deprecated(message = "请谨慎使用此方法，一般情况下请参考使用getContentView")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.container = container
        if (isDebugLog) {
            "$TAG -> life Circle: onCreateView".log()
        }

        mViewParent = RainbowPageRootView(pageActivity)

//        初始化container部分
        val parentLayoutParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mViewParent.layoutParams = parentLayoutParam

        mViewParent.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        //屏蔽touch事件穿透
        mViewParent.setOnTouchListener { _, _ -> return@setOnTouchListener true }

        val contentView = getContentView(inflater, mViewParent, savedInstanceState)
                .fixContentBackground()

        mViewParent.setPageLeftShadowDrawableResource(R.drawable.rainbow_page_background)
        mViewParent.setMainAnimateView(contentView, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // 获取当前委托类，并将真正的实现委托给委托类，再转发给当前动画对象
        getClassDelegate<PageAnimatorWrapper>(SdkFragment::class)?.realInstance = mViewParent

        return mViewParent
    }

    /**
     * 获取当前页面的内容View
     */
    abstract fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    @Deprecated(message = "请谨慎使用此方法，一般情况下请参考使用onPageCreate")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view

        pageAnimators = PageRatioAnimators(this)
        if (savedInstanceState != null) {
            pageIntent.putExtras(savedInstanceState)
        }

        CommonSdk.event().register(this)
        if (isDebugLog) {
            "$TAG -> life Circle: onViewCreated".log()
        }

        if (!isFinishing()) {
            onPageCreated(view, savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val extras = pageIntent.extras
        if (extras != null) {
            outState.putAll(extras)
        }
        if (isDebugLog) {
            "$TAG -> life Circle: onSaveInstanceState".log()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        pageIntent.putExtras(savedInstanceState)
        if (isDebugLog) {
            "$TAG -> life Circle: onViewStateRestored".log()
        }
    }

    override fun setArguments(args: Bundle?) {
        if (arguments != null || isStateSaved || isAdded) {
            return
        }
        super.setArguments(pageIntent.putExtras(args).extras)
    }

    /**当页面的打开方式是在堆栈中找到的，重新打开，则调用此方法重置入参*/
    fun onArgumentsReset(args: Bundle?) {
    }

    /**当按键抬起时*/
    fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed(1, false)
            return true
        }
        return false
    }

    /**
     * 不要使用此方法
     *
     * 此声明周期不准确
     *
     */
    final override fun onAttach(activity: Context) {
        super.onAttach(activity)
        val lastFragment = getPrePage(true) as? Fragment ?: return
        if (lastFragment is SdkFragment
                && lastFragment.isResumed
                && !lastFragment.isDetached
                && !lastFragment.isFinishing()) {
            lastFragment.onPause()
        }
        if (isDebugLog) {
            "$TAG -> life Circle: onAttach".log()
        }
    }

    /**
     * 不要使用此方法
     *
     * 此声明周期不准确
     *
     */
    final override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.pageActivity = activity
    }

    /**
     * 不要使用此方法
     *
     * 此声明周期不准确
     *
     */
    @Deprecated("")
    final override fun onStart() {
        super.onStart()
        if (isDebugLog) {
            "$TAG -> life Circle: onStart".log()
        }

        if (!isDirty) {
            onPageStart()
        }
    }

    override fun onPageStart() {
        if (isDebugLog) {
            "$TAG -> life Circle: onPagePause".log()
        }
    }

    /**
     * 不要使用此方法
     *
     * Activit 重新回到页面时,如果Fragment栈中过多处理onResume会影响反应速度
     *
     * @see [onPageResume]
     */
    @Deprecated("")
    final override fun onResume() {
        super.onResume()
        if (isDebugLog) {
            "$TAG -> life Circle: onResume".log()
        }
        postResume()
    }

    /**
     * 当前Fragment在 栈顶时调用
     */
    override fun onPageResume() {

        val translationX = view?.translationX ?: 0F
        if (translationX != 0F) {
            view?.translationX = 0F
        }

        paused = false
        if (isDebugLog) {
            "$TAG -> life Circle: onPageResume".log()
        }
    }

    /**
     * 不要使用此方法
     *
     * @see [onPagePause]
     */
    @Deprecated("")
    final override fun onPause() {
        super.onPause()
        if (isDebugLog) {
            "$TAG -> life Circle: onPause".log()
        }

        if (isFinishing()) return

        if (!paused && !isDirty) {
            postPause()
        }
    }

    /**
     * 当前Fragment在 栈顶时调用
     */
    override fun onPagePause() {
        if (isDebugLog) {
            "$TAG -> life Circle: onPagePause".log()
        }
    }

    /**
     * 不要使用此方法
     *
     * 此声明周期不准确
     *
     */
    @Deprecated("不要使用此方法，此方法回调不准确")
    final override fun onStop() {
        super.onStop()
        if (isDebugLog) {
            "$TAG -> life Circle: onStop".log()
        }

        if (!isDirty) {
            onPageStop()
        }
    }

    override fun onPageStop() {
        if (isDebugLog) {
            "$TAG -> life Circle: onPageStop".log()
        }
    }

    final override fun onDetach() {
        super.onDetach()

        mView = null
        val lastFragment = getPrePage(true) as? Fragment ?: return
        if (!pageActivity.isFinishing) {
            lastFragment.onResume()
        }
        if (isDebugLog) {
            "$TAG -> life Circle: onDetach".log()
        }

        if (!isDirty) {
            onPageDetach()
        }
    }


    override fun onPageDetach() {
        if (isDebugLog) {
            "$TAG -> life Circle: onPageDetach".log()
        }
    }

    @Deprecated("不要使用此方法，此方法回调不准确")
    override fun onDestroyView() {
        super.onDestroyView()

        if (isDebugLog) {
            "$TAG -> life Circle: onDestroyView".log()
        }
//        if (isDirty) {
//            container?.removeView(mViewParent)
//        }
        if (!isDirty) {
            onPageDestroyView()
        }
    }

    override fun onPageDestroyView() {

        if (isDebugLog) {
            "$TAG -> life Circle: onPageDestroyView".log()
        }
    }

    final override fun onDestroy() {
        changeToFinishing()
        CommonSdk.event().unregister(this)

//        container?.removeView(mViewParent)

        super.onDestroy()
        if (isDebugLog) {
            "$TAG -> life Circle: onDestroy".log()
        }
        if (!isDirty) {
            onPageDestroy()
        }
    }

    override fun onPageDestroy() {
        hasDestroyed = true
        if (isDebugLog) {
            "$TAG -> life Circle: onPageDestroy".log()
        }
    }

    fun postResume() {
        postRunnable {
            if (isFront() && !isDirty) {
                onPageResume()
            }
            onPostResume()
        }
    }

    fun postPause() {
        if (isFinishing()) return

        paused = true
        onPagePause()
    }

    /*---------------------------------------常用方法及功能---------------------------------------*/

    /**发送一个消息，默认忽略自己本身*/
    fun postEvent(event: Any) {
        CommonSdk.event().post(event, ignored = this)
    }

    override fun isFront(): Boolean {
        return PageManagerImpl.getTopPage() === this
    }

    override fun isFinishing(): Boolean {
        return mFinishing || view == null || hasDestroyed
    }

    private fun changeToFinishing() {
        mFinishing = true
    }

    /** 是否页面已经停止  */
    fun isPaused(): Boolean {
        return paused
    }

    override fun getPrePage(isSameContainer: Boolean): Page? {
        val tag = pageIntent.lastFragmentTag

        if (TextUtils.isEmpty(tag)) return null

        var result: Fragment?
        var fm: FragmentManager? = this.fragmentManager

        if (fm == null) {
            val ac = PageManagerImpl.getContainer(containerId)?.pageActivity
            if (ac != null && ac is FragmentActivity) {
                fm = ac.supportFragmentManager
            }
        }

        if (fm == null) {
            return null
        }

        //从当前Container中找前一个Fragment
        result = fm.findFragmentByTag(tag)

        //如果为null，并且必须是同一个Container，则直接返回null
        if (result == null && isSameContainer) {
            return null
        }

        //如果结果不是一个Page类型，则返回null
        if (result != null && result !is Page) {
            return null
        }

        if (result == null && !isSameContainer) {
            val preContainer = PageManagerImpl.getPreContainer(containerId) ?: return null
            if (preContainer is FragmentActivity) {
                result = preContainer.supportFragmentManager.findFragmentByTag(tag)
            }
        }

        return result as? Page
    }

    /**
     * 是否覆盖在之前的fragment之上
     */
    fun isOverlayFragment(): Boolean {
        var result = pageIntent.isOverlayFragment
        if (!result) {
            val lastFragment = getPrePage(true)
            if (lastFragment == null) {
                result = true
            } else if (lastFragment is SdkFragment) {
                val lastPageIntent = lastFragment.pageIntent
                val lastAnimations = lastPageIntent.animations
                result = !Arrays.equals(pageIntent.animations, lastAnimations)
                        || Arrays.equals(PageAnims.NONE.animations, lastAnimations)
            }
        }
        return result
    }

    fun finishAffinity() {
        PageManagerImpl.finishAffinity(pageIntent)
    }

    override fun finish(isForce: Boolean) {
        val delegate = pageManagerDelegate
        if (delegate != null) {
            delegate.finish(isForce)
        } else {
            onBackPressed(1, isForce)
        }
    }

    fun finish(step: Int, isForce: Boolean = false) {
        val delegate = pageManagerDelegate
        if (delegate != null) {
            delegate.finish(step, isForce)
        } else {
            onBackPressed(step, isForce)
        }
    }

    /*---------------------------------------任务队列维护---------------------------------------*/

    /**
     * 在onResume之后执行
     */
    private fun onPostResume() {
        CommonSdk.task().post(100) {
            // 防止嵌套的Fragment#onCreateAnimation未执行的情况
            if (pageAnimationState < 1) {
                resolvePageAnimationEnd()
            }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return null
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        if (transit == FragmentTransaction.TRANSIT_NONE) return null

        val pageAnimator = pageAnimators?.getPageAnimator(
                transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE, enter, nextAnim)

        if (pageAnimator != null && transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                && enter && pageAnimator.duration > 0) {

            pageAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    resolvePageAnimationEnd()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    pageAnimationState = 1
                }

            })
        }
        //前面没有页面，并且当前页面是进入，并且进入方式是打开，这时候fragment不能有动画执行
        if (getPrePage(false) == null && enter
                && (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN || transit == FragmentTransaction.TRANSIT_NONE)) {
            this.fragmentAnimator = null
            return null
        }
        this.fragmentAnimator = pageAnimator
        return pageAnimator
    }

    private fun resolvePageAnimationEnd() {
        synchronized(postOnPageAnimationEndLock) {
            postRunnable(postOnPageAnimationRunnable)
            pageAnimationState = 2
        }
    }

    /**
     * 在页面动画执行完成之后执行runnable, 没有页面动画会立即执行.
     */
    protected fun postOnPageAnimationEnd(runnable: () -> Unit) {
        synchronized(postOnPageAnimationEndLock) {
            if (pageAnimationState < 2) {
                postOnPageAnimationRunnable.add(runnable)
            } else {
                postRunnable(runnable)
            }
        }
    }

    /**
     * 是否页面动画执行完成
     */
    protected fun isEnterAnimationEnd(): Boolean {
        return pageAnimationState >= 2
    }

    private fun postRunnable(runnable: Runnable) {
        postRunnable { runnable.run() }
    }

    private fun postRunnable(lambda: (() -> Unit)) {
        if (isFinishing()) {
            return
        }
        CommonSdk.task().post {
            if (isFinishing()) {
                return@post
            }
            view?.post(lambda)
        }
    }

    /**
     * rId==0,返回Animation实例,否则Fragment启动会有延迟
     */
    protected open fun getAnimation(rId: Int): Animation {
        return if (rId <= 0) {
            object : Animation() {
                override fun getDuration(): Long {
                    return 0
                }
            }
        } else {
            AnimationUtils.loadAnimation(CommonSdk.environment().app(), rId)
        }
    }

    /**
     * 所有的back操作都会回调这里，这里再回调[.onFinishPre] 方法，根据返回结果，是否结束页面。
     */
    private fun onBackPressed(step: Int, isForce: Boolean) {
        if ((isFinishing() || !isResumed) || (isForce || !onPreFinish())) {
            changeToFinishing()
            PageManagerImpl.finish(step, this)
        }
    }

    override fun setResult(result: Any) {
        _result = result
    }

    override fun clearResult() {
        _result = null
    }

    override fun getResult(): Any? = _result

    private inner class RunnableList : Runnable {

        private val queue = LinkedList<() -> Unit>()

        fun add(item: () -> Unit) {
            queue.addLast(item)
        }

        override fun run() {
            var item: (() -> Unit)? = queue.pollFirst()
            while (item != null) {
                postRunnable(item)
                item = queue.pollFirst()
            }
        }
    }

    /**
     * 跳转到指定页面
     *
     * 使用举例：
     *
     * ``` kotlin
     * val arguments = Bundle()
     * arguments.putIntExtra("id", id)
     *
     * gotoPage("order/detail", bundle)
     *
     * ```
     *
     * [page] 跳转的页面名称，可以通过PageDefine的方式或者注解的方式进行添加和查询
     *
     * [bundle] 给下个页面的参数
     *
     * 这个方法之所以没有放到SdkFragmentExtra_.kt中，是因为已经有很多页面在使用此方法。如果更换位置，需要更换大量引用
     *
     * 更多打开页面方法，请参考：[SdkFragmentExtra_.kt]
     */
    fun gotoPage(page: String, bundle: Bundle? = null) {
        val pageIntent = PageIntent(page)
        if (bundle != null) {
            pageIntent.putExtras(bundle)
        }

        PageManagerImpl.gotoPage(pageIntent)
    }


    //为了扩展PageIntent做的缓存
    private val onResultCache: MutableMap<Intent, OnPageResultCallback<Any>> = mutableMapOf()

    /**
     * 打开指定页面，并可以做参数传递，回调结果等
     *
     * 使用举例：
     *
     * ``` kotlin
     * gotoPage("pageName") {
     *      // 添加参数
     *      putExtra("param1", 1)
     *      putExtra("param2", "value")
     *
     *      // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
     *      onResult<String> {
     *          //处理结果
     *      }
     * }
     * ```
     */
    fun gotoPage(page: String, initCallback: PageIntent.() -> Unit) {
        val pageIntent = PageIntent(page)
        pageIntent.initCallback()

        val onResult = onResultCache[pageIntent]
        onResultCache.remove(pageIntent)

        PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = onResult)
    }

    /**
     * 打开指定页面，并可以做参数传递，回调结果等
     *
     * 如果你已经有参数集，或者完全不需要传递参数，只关心结果，可以使用此方法
     *
     * 使用举例：
     *
     * ``` kotlin
     * // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
     * gotoPage<String>("pageName") {
     *      //结果处理
     * }
     *
     * val bundle = Bundle()
     * bundle.putIntExtra("params", 1)
     *
     * // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
     * gotoPage<String>("pageName", bundle) {
     *      //结果处理
     * }
     * ```
     */
    fun <T : Any> gotoPage(page: String, bundle: Bundle? = null, invoker: OnPageResultCallback<in T>) {
        val pageIntent = PageIntent(page)

        if (bundle != null) {
            pageIntent.putExtras(bundle)
        }

        PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = invoker)
    }

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
    fun gotoActivity(clazz: KClass<out Activity>?, initCallback: (Intent.() -> Unit)? = null) {
        val context = activity ?: return
        context.gotoActivity(clazz, this, initCallback)
    }


    /**
     * 打开activity，如果需要请求结果的情况下，返回结果在intent中。
     *
     * 当已经有参数集，或者完全不用传递参数，并且需要获取结果，则此方法使用更简单一些
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
     * [invoker] 如果此参数不为null，则打开方式为startActivityForResult方式
     *
     * 使用举例：
     *
     * ``` kotlin
     * gotoActivityForResult(FirstActivity::class) {
     *     //回调结果的resultCode字段，一般对应Activity.RESULT_OK等
     *     it.resultCode
     * }
     * ```
     */
    fun gotoActivityForResult(clazz: KClass<out Activity>?, intent: Intent? = null, invoker: OnPageResultCallback<Intent>? = null) {
        val context = activity ?: return
        context.gotoActivityForResult(clazz, intent, this, invoker)
    }
}