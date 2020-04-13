package com.meili.moon.sdk.base.tracker

import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.track.ParamsProvider
import com.meili.moon.sdk.track.TrackerProvider
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import org.json.JSONObject
import java.util.*

/**
 * Created by imuto on 2019/5/24.
 */
object SensorsAnalyticsTrackerProvider : TrackerProvider {

    private val mParamProviders = mutableListOf<ParamsProvider>()

    fun init() {

        ComponentsInstaller.installTrackerProvider(this)

        val api = SensorsDataAPI.sharedInstance(Sdk.app(),
                "http://dataapi-bigdata.mljr.com/mljr_pub_api/json/event/car_crm"
        )

        api.enableLog(Sdk.environment().isDebug())

        try {
            // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
            val eventTypeList = ArrayList<SensorsDataAPI.AutoTrackEventType>()
            // $AppStart
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_START)
            // $AppEnd
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_END)
            // $AppViewScreen
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN)
            // $AppClick
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_CLICK)
            SensorsDataAPI.sharedInstance().enableAutoTrack(eventTypeList)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //初始化通用参数
        SensorsDataAPI.sharedInstance().registerDynamicSuperProperties {
            val result = JSONObject()

            mParamProviders.forEach {
                try {
                    val params: Map<String, Any>? = it.invoke()
                    params?.forEach params@{ entry ->
                        if (entry.key == "gps") {
                            try {
                                val gpsValue = entry.value.toString()
                                val split = gpsValue.split("|")

                                api.setGPSLocation(split[0].toDouble(), split[1].toDouble())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            return@params
                        }
                        result.put(entry.key, entry.value)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            result
        }

        SensorsDataAPI.sharedInstance().trackFragmentAppViewScreen()
    }

    override fun addCommonParamsProvider(provider: ParamsProvider?) {
        provider ?: return
        mParamProviders.add(provider)
    }

    override fun removeCommonParamsProvider(provider: ParamsProvider?) {
        provider ?: return
        mParamProviders.remove(provider)
    }

    override fun track(eventName: String, properties: JSONObject?) {
        SensorsDataAPI.sharedInstance().track(eventName, properties)
    }

    override fun trackBegin(eventName: String) {
        SensorsDataAPI.sharedInstance().trackTimerStart(eventName)

    }

    override fun trackEnd(eventName: String, properties: JSONObject?) {
        SensorsDataAPI.sharedInstance().trackTimerEnd(eventName, properties)
    }
}