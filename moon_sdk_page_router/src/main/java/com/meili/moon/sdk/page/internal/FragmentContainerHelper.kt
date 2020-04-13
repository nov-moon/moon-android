package com.meili.moon.sdk.page.internal

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.page.internal.utils.isDirty
import com.meili.moon.sdk.util.foreachInverse

/**
 * Created by imuto on 2019-06-14.
 */
internal class FragmentContainerHelper(val container: FragmentContainerImpl) : Runnable {

    private val mOpList = mutableListOf<OpModel>()

    @Synchronized
    fun startFragment(ft: FragmentTransaction) {
//        ft.commitAllowingStateLoss()
        val op = GotoOp(ft)
        if (PageStackManager.INSTANCE.topFragment == null) {
            executeOp(op)
            return
        }
        mOpList.add(op)
    }

    @Synchronized
    fun finishFragment(step: Int, page: SdkFragment?) {
        page ?: return
        mOpList.add(FinishOp(step, page))
    }

    @Synchronized
    fun commit() {
        CommonSdk.task().removeCallbacks(this)
        CommonSdk.task().post(this)
    }

    override fun run() {
        synchronized(mOpList) {
            if (mOpList.isEmpty()) return
            mOpList.sortBy { if (it is GotoOp) 1 else 0 }
            val hasOpen = mOpList[mOpList.lastIndex] is GotoOp
            mOpList.forEach {
                //                if (executeOp(it, hasOpen)) return
                executeOp(it, hasOpen)
            }
            mOpList.clear()
        }
    }

    private fun executeOp(it: OpModel): Boolean {
        return executeOp(it, false)
    }

    private fun executeOp(it: OpModel, hasOpen: Boolean = false): Boolean {
        when (it) {
            is GotoOp -> it.ft.commitAllowingStateLoss()
            is FinishOp -> {
                if (hasOpen) {
                    processDirtyFragment(it)
                    return true
                }

                val fragment = it.page
//                fragment.isForceNoPageAnimation = hasOpen
                val tag = container.getFinishTag(it.step, fragment)
                val fm = fragment.fragmentManager
                if (fm == null || TextUtils.isEmpty(tag)) {
                    return true
                }

                fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
        return false
    }

    private fun processDirtyFragment(op: FinishOp) {
        val fragment = op.page
        val step = op.step
        val index = container.findFragmentIndex(fragment)

        val start = if (index - (step - 1) >= 0) index - (step - 1) else 0

        (start..index).foreachInverse {
            val processPage = container.findFragment(it) ?: return@foreachInverse
            if (processPage.isDirty) {
                return@foreachInverse
            }
//            processPage.onDestroyView()

            processPage.isDirty = true
            processPage.onPagePause()
            processPage.onPageStop()
            processPage.onDestroyView()
            processPage.onDestroy()
            processPage.onPageDetach()
        }

        container.size().foreachInverse {
            val page = container.findFragment(it)
            if (page.isDirty) return@foreachInverse
            val tag = fixPage(page) ?: return@foreachInverse
            page.pageIntent.lastFragmentTag = tag
        }
    }

    private fun fixPage(page: SdkFragment): String? {
        val preFragment = page.getPrePage(false)
                ?: return null

        if (preFragment is SdkFragment) {
            if (preFragment.isDirty) {
                return fixPage(preFragment)
            }
            return preFragment.tag
        }
        return null
    }
}

internal interface OpModel

private data class GotoOp(val ft: FragmentTransaction) : OpModel
private data class FinishOp(val step: Int, val page: SdkFragment) : OpModel
