package com.meili.moon.sdk.cache.demo

import android.app.Activity
import android.os.Bundle
import com.meili.moon.sdk.cache.MoonCacheImpl
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cache = MoonCacheImpl.getCache()
        val fileCache = MoonCacheImpl.getFileCache()

        /**存储缓存*/
        mTxtSaveApiCache.setOnClickListener {
            cache.put("custom1","缓存数据1")
        }
        mTxtGetApiCache.setOnClickListener {
            val start = System.currentTimeMillis()
            val cc = cache.getValue("custom1")
//            cache.configGlobal(object : DiskCache.Config {
//                override var cacheMaxCount: Int = 500
//                override var cacheMaxFileSize: Long = 100 * 1024 * 1024L
//            })
////            val cValue = cache.getValue("custom1")
////
//
//
//            Logcat.e(cc)
//            val end = System.currentTimeMillis()
//            Logcat.e(end-start)
////            Logcat.e(cValue)
//            cache.remove("")
            mTxtAddDataMessage.text = cc
        }
        mTxtSaveFileCache.setOnClickListener {
            fileCache.put("custom", File(""));
            cache.clear()
        }
        mTxtGetFileCache.setOnClickListener {
            val cache = fileCache.getValue("")

        }
        mTxtCleanCache.setOnClickListener {
            cache.clear()
        }
    }

}

