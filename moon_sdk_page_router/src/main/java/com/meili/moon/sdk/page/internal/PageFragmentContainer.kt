package com.meili.moon.sdk.page.internal

import android.support.v4.app.FragmentManager
import com.meili.moon.sdk.page.PagesContainer

/**
 * Created by imuto on 2018/4/8.
 */
interface PageFragmentContainer : PagesContainer {

    fun getSupportFragmentManager(): FragmentManager

    fun hasSavedState(): Boolean
}