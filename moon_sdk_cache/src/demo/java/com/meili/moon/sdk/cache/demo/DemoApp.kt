package com.meili.moon.sdk.cache.demo

import android.app.Application
import com.meili.moon.sdk.cache.MoonCacheImpl
import com.meili.moon.sdk.db.MoonDB

/**
 * Application
 */
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MoonDB.init(this)
        MoonCacheImpl.init(this)
    }
}
