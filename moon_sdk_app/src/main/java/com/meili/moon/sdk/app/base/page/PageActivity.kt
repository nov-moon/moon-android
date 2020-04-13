package com.meili.moon.sdk.app.base.page

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import com.meili.moon.sdk.base.Sdk

/**
 * Created by imuto on 2018/5/15.
 */
open class PageActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Sdk.view().inject(this)
    }
}