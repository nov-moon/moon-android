package com.meili.moon.sdk.app.base.role

import android.content.Intent
import android.view.View

/**
 * Author wudaming
 * Created on 2018/9/21
 */
interface LifecycleBridge {
//    fun onCreateView(){}
    fun onViewCreated(view :View){}
    fun onActivityCreated(){}
    fun onStart(){}

    fun onStop(){}
    fun onDestroyView(){}
    fun onDestroy(){}
    fun onDetach(){}

    fun onPreFinish()=false
    fun onPageResume(){}
    fun onPagePause(){}
    fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent){}

}