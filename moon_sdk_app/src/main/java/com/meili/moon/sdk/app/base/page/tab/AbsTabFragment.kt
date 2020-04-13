package com.meili.moon.sdk.app.base.page.tab

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import android.view.ViewGroup
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.page.PageFragment
import com.meili.moon.sdk.app.base.page.util.SimpleOnPageChangedListener
import com.meili.moon.sdk.app.util.hideKeyboard
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.page.exception.StartPageException
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.largerSize
import com.meili.moon.sdk.util.throwOnDebug
import kotlinx.android.synthetic.main.moon_sdk_app_tab_page.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2018/5/16.
 */
abstract class AbsTabFragment : PageFragment() {

    private lateinit var mAdapter: MPagerAdapter

    override fun getLayoutResId() = R.layout.moon_sdk_app_tab_page

    private val simpleOnPageChangedListener = object : SimpleOnPageChangedListener() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            for (i in 0 until mAdapter.getFragments().size) {
                val pageFragment = mAdapter.getFragments()[i] as? TabItemListener ?: continue
                if (i == position) {

                    pageFragment.onAlphaChanged(positionOffset)
                } else {

                    pageFragment.onAlphaChanged(1f - positionOffset)
                }
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Sdk.task().post(400) { hideKeyboard() }
            this@AbsTabFragment.onPageSelected(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = MPagerAdapter(childFragmentManager)
        val tabAnnotation = this::class.findAnnotation<TabContent>()
        if (tabAnnotation == null) {
            throwOnDebug(StartPageException(msg = "请使用TabAnnotation对使用类进行注解"))
            return
        }

        mAdapter.setFragmentsClass(tabAnnotation.value.toList())

        val firstSelectPosition = if (savedInstanceState != null) {
            initBySaveInstanceState(savedInstanceState)
        } else tabAnnotation.checkedIndex

        mViewPager.adapter = mAdapter
        mViewPager.offscreenPageLimit = mAdapter.count
        mViewPager.addOnPageChangeListener(simpleOnPageChangedListener)

        Sdk.task().post(200) { select(firstSelectPosition) }
    }

    protected fun select(position: Int) {
        if (isFinishing()) return
        if (position < 0 || position > mAdapter.count) {
            return
        }
        mViewPager?.currentItem = position
    }

    protected fun getCurrentIndex(): Int {
        return mViewPager?.currentItem ?: 0
    }

    /**
     * 如果重写此方法,请调用父类的方法,父类会进行子view的select通知
     */
    open protected fun onPageSelected(position: Int) {
        if (isFinishing()) {
            return
        }
        val fragments = getFragments()
        if (!largerSize(fragments, position)) {
            return
        }
        val pageFragment = fragments[position]
        if (isFinishing()) {
            return
        }
        pageFragment.postResume()

        if (pageFragment is TabItemListener) {
            (pageFragment as TabItemListener).onSelected()
        }
    }

    /** 获取指定类型的fragment，如果之前没有注册或者还未初始化，则返回null  */
    protected fun <T : PageFragment> getFragment(clazz: Class<T>): T? {
        return getFragments().firstOrNull { it::class.java == clazz } as? T?
    }

    protected fun getFragments(): List<PageFragment> {
        return mAdapter.getFragments()
    }

    protected fun onPageInit(fragment: PageFragment, position: Int) {

    }

    protected fun getFirstSelectPosition(): Int {
        return 0
    }

    private inner class MPagerAdapter(internal var mFM: FragmentManager) : FragmentPagerAdapter(mFM) {

        private var fragmentsClass: List<KClass<out PageFragment>> = mutableListOf()

        // 要保持数组下表,小标==viewPager.Position
        private var fragments: MutableList<PageFragment> = mutableListOf()

        var fragmentsPageName: MutableList<String> = ArrayList()

        val currentFragment: PageFragment?
            get() = if (!largerSize(fragments, mViewPager?.currentItem ?: Int.MAX_VALUE)) {
                null
            } else fragments[mViewPager.currentItem]

        fun setFragmentsClass(cls: List<KClass<out PageFragment>>) {
            fragmentsClass = cls
//            fragments.clear()
//            for (i in fragmentsClass.indices) {
//                fragments.add(i, null)
//            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val result = super.instantiateItem(container, position) as PageFragment
            if (!fragments.contains(result)) {
                if (largerSize(fragments, position)) {
                    fragments.removeAt(position)
                    fragments.add(position, result)
                } else {
                    fragments.add(result)
                }
            }
            return result
        }

        override fun getItem(position: Int): Fragment {
            var fragment: PageFragment? = null
            if (largerSize(fragments, position)) {
                fragment = fragments[position]
            }
            if (fragment != null) {
                onPageInit(fragment, position)
                return fragment
            }
            val simpleName = if (largerSize(fragmentsPageName, position)) {
                fragmentsPageName[position]
            } else null

            if (!isEmpty(simpleName)) {
                fragment = mFM.findFragmentByTag(simpleName) as PageFragment
            }
            if (fragment == null) {
                fragment = Fragment.instantiate(pageActivity, fragmentsClass[position].qualifiedName) as PageFragment
            }
            if (largerSize(fragments, position)) {
                fragments.removeAt(position)
                fragments.add(position, fragment)
            } else {
                fragments.add(fragment)
            }
            onPageInit(fragment, position)
            return fragment
        }

        fun getFragments(): List<PageFragment> {
            return fragments
        }

        override fun getCount(): Int {
            return fragmentsClass.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            super.destroyItem(container, position, obj)
            //            fragments.remove(position);
        }

        override fun finishUpdate(container: ViewGroup) {
            super.finishUpdate(container)
        }
    }

    interface TabItemListener {
        fun onAlphaChanged(alpha: Float)
        fun onSelected()
    }

    override fun onPagePause() {
        super.onPagePause()
        mAdapter.currentFragment?.onPause()
    }


    override fun onPageResume() {
        super.onPageResume()
        if (mAdapter.currentFragment == null) {
            Sdk.task().post(mPageResumeRunnable)
            return
        }
        val currentFragment = mAdapter.currentFragment ?: return
        currentFragment.postResume()
    }

    private val mPageResumeRunnable = object : Runnable {
        override fun run() {
            if (mAdapter.currentFragment == null) {
                if (isFinishing()) {
                    return
                }
                Sdk.task().post(this, 50)
                return
            }
            val currentFragment = mAdapter.currentFragment ?: return
            currentFragment.onResume()
        }
    }

    override fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onPageResult(requestCode, resultCode, intent)
        mAdapter.currentFragment?.onPageResult(requestCode, resultCode, intent)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0 until mAdapter.getFragments().size) {
            val pageFragment = mAdapter.getFragments()[i]
            outState.putString("page$i", pageFragment.tag)
        }
        outState.putInt("pageCount", mAdapter.count)
        outState.putInt("currIndex", mViewPager.currentItem)
    }

    private fun initBySaveInstanceState(savedInstanceState: Bundle?): Int {
        return if (savedInstanceState != null) {
            val pageCount = savedInstanceState.getInt("pageCount", 0)
            mAdapter.fragmentsPageName.clear()
            for (i in 0 until pageCount) {
                mAdapter.fragmentsPageName.add(savedInstanceState.getString("page$i"))
            }
            savedInstanceState.getInt("currIndex")
        } else 0
    }
}