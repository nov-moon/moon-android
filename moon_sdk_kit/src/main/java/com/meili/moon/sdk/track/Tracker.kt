package com.meili.moon.sdk.track

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.inClipboard
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**参数提供器，返回map类型参数*/
typealias ParamsProvider = () -> Map<String, Any>?

/**
 * Created by imuto on 2019/5/23.
 */
interface TrackerProvider {
    fun addCommonParamsProvider(provider: ParamsProvider?)
    fun removeCommonParamsProvider(provider: ParamsProvider?)

    fun track(eventName: String, properties: JSONObject? = null)
    fun trackBegin(eventName: String)
    fun trackEnd(eventName: String, properties: JSONObject? = null)
}

/**
 * 数据埋点对象，要使用此对象，请先安装[TrackerProvider]
 */
object Tracker {

    fun addCommonParamsProvider(provider: ParamsProvider?) {
        if (!CommonSdk.isTracker()) return
        ComponentsInstaller.mTracker.get().addCommonParamsProvider(provider)
    }

    fun removeCommonParamsProvider(provider: ParamsProvider?) {
        if (!CommonSdk.isTracker()) return
        ComponentsInstaller.mTracker.get().removeCommonParamsProvider(provider)
    }

    /**
     * 添加埋点，在不调用后续put等方法后，会自动提交埋点
     */
    fun track(eventName: String): TrackData {
        return TrackData(EventTrack(eventName), true)
    }

    /**
     * 添加埋点
     *
     * 不会自动提交，需要调用commit方法提交埋点，否则埋点将丢失
     */
    fun trackNoCommit(eventName: String): TrackData {
        return TrackData(EventTrack(eventName), false)
    }

    /**
     * 统计时长的开始时间
     */
    fun trackBegin(eventName: String) {
        if (!CommonSdk.isTracker()) return
        val provider = ComponentsInstaller.mTracker.get()
        provider.trackBegin(eventName)
    }

    /**
     * 统计时长的埋点
     */
    fun trackEnd(eventName: String): TrackData {
        return TrackData(EventTimerTrack(eventName), true)
    }

    fun trackEdit(eventName: String, editText: EditText): TrackDataCache {
        return trackEdit(eventName, editText, null)
    }

    fun trackEdit(eventName: String, editText: EditText, focusChangeListener: View.OnFocusChangeListener?): TrackDataCache {
        val commitable = EventTrack(eventName)
        val trackData = TrackData(commitable, false)
        val trackDataCache = TrackDataCache()
        val records = arrayOf(JSONArray(), JSONArray())

        val filters = editText.filters
        val filterList = ArrayList<InputFilter>()
        if (filters != null) {
            filterList.addAll(Arrays.asList(*filters))
        }
        filterList.add(InputFilter { source, start, end, dest, dstart, dend ->
            LogUtil.e("InputFilter: $source || start:$start end:$end dest:$dest || dstart:$dstart dend:$dend")
            try {
                if (source.inClipboard()) {
                    val copyRecords = records[1]
                    copyRecords.put(source)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            null
        })

        editText.filters = filterList.toTypedArray()

        var lastText = editText.text.toString()
        lastText = lastText.replace(" ".toRegex(), "")
        val lastSB = StringBuilder(lastText)
        val lastTime = LongArray(1)

        val oldOnFocusChangeListener = editText.onFocusChangeListener

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                LogUtil.e("onTextChanged: $s || start:$start before:$before count:$count")
                try {
                    var curr = s.toString()
                    curr = curr.replace(" ".toRegex(), "")
                    if (lastSB.toString() == curr) {
                        return
                    }
                    lastSB.replace(0, lastSB.length, curr)

                    val textRecords = records[0]
                    textRecords.put(s.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })

        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            LogUtil.e("onFocusChange: $hasFocus")
            focusChangeListener?.onFocusChange(v, hasFocus)
                    ?: oldOnFocusChangeListener?.onFocusChange(v, hasFocus)
            try {
                val textRecords = records[0]
                val copyRecords = records[1]
                if (!hasFocus && (textRecords.length() > 0 || copyRecords.length() > 0)) {
                    trackData.commit()
                } else {
                    lastTime[0] = System.currentTimeMillis()

                    records[0] = JSONArray()
                    records[1] = JSONArray()
                    trackData.clearProperty()

                    var currText = editText.text.toString()
                    currText = currText.replace(" ".toRegex(), "")
                    lastSB.replace(0, lastSB.length, currText)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        commitable.registerOnCommitCallback(object : Commitable.OnCommitCallback {
            override fun onCommit() {

                val mProperty = trackDataCache.mProperty
                if (mProperty != null) {
                    val keys = mProperty.keys()
                    while (keys.hasNext()) {
                        val name = keys.next()
                        try {
                            trackData.putInternal(name, mProperty.get(name))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }

                val textRecords = records[0]
                val copyRecords = records[1]
                trackData.put("text_records", textRecords)
                if (copyRecords.length() > 0) {
                    trackData.put("copy_records", copyRecords)
                }

                if (lastTime[0] > 0) {
                    trackData.put("event_duration", System.currentTimeMillis() - lastTime[0])
                }

                records[0] = JSONArray()
                records[1] = JSONArray()
                lastTime[0] = 0
            }
        })
        return trackDataCache
    }
//    /**
//     * 添加用户信息
//     *
//     *
//     * 用户可以在留存分析、分布分析等功能中，使用用户属性作为过滤条件，精确分析特定人群的指标。
//     *
//     *
//     * 更多用户信息api可以直接使用神策api，文档：https://www.sensorsdata.cn/manual/android_sdk.html
//     */
//    fun trackProfile(): TrackData {
//        return TrackData(ProfileTrack(), true)
//    }
//
//    /**
//     * 添加用户信息
//     *
//     *
//     * 用户可以在留存分析、分布分析等功能中，使用用户属性作为过滤条件，精确分析特定人群的指标。
//     *
//     *
//     * 不会自动提交，需要调用commit方法提交埋点，否则埋点将丢失
//     */
//    fun trackProfileNoCommit(): TrackData {
//        return TrackData(ProfileTrack(), false)
//    }
//
//    /**
//     * 当用户登录后，绑定登录关系
//     *
//     *
//     * 注册成功、登录成功、初始化SDK后  调用 login 传入登录 ID
//     *
//     * @param userId 用户要绑定在神策的用户id
//     */
//    fun login(userId: String) {
//        //注册成功、登录成功、初始化SDK后  调用 login 传入登录 ID
//        //服务器暂时不支持profile类型，所以暂时先不调用登录等方法
//        //        SensorsDataAPI.sharedInstance().login(userId);
//    }

}


open class TrackData internal constructor(private var mCommitable: Commitable?, protected var isAutoCommit: Boolean) {
    internal var mProperty: JSONObject? = null

    init {
        if (isAutoCommit) {
            mHandler.postDelayed(mCommitable, DEFAULT_DELAYED)
        }
    }

    /**
     * 添加属性
     *
     *
     * key必须是合法的变量名，即不能以数字开头，且只包含：大小写字母、数字、下划线和 $。
     */
    fun put(key: String, value: String): TrackData {
        putInternal(key, value)
        return this
    }

    /**
     * 添加属性
     *
     *
     * key必须是合法的变量名，即不能以数字开头，且只包含：大小写字母、数字、下划线和 $。
     */
    fun put(key: String, value: Number): TrackData {
        putInternal(key, value)
        return this
    }

    /**
     * 添加属性
     *
     *
     * key必须是合法的变量名，即不能以数字开头，且只包含：大小写字母、数字、下划线和 $。
     *
     *
     * jsonArray中的内容只能是String类型，否则将出错
     */
    fun put(key: String, value: JSONArray): TrackData {
        putInternal(key, value)
        return this
    }

    /**
     * 添加属性
     *
     *
     * key必须是合法的变量名，即不能以数字开头，且只包含：大小写字母、数字、下划线和 $。
     */
    fun put(key: String, value: Boolean?): TrackData {
        putInternal(key, value)
        return this
    }

    /**
     * 添加属性
     *
     *
     * key必须是合法的变量名，即不能以数字开头，且只包含：大小写字母、数字、下划线和 $。
     */
    fun put(key: String, value: Date): TrackData {
        putInternal(key, value)
        return this
    }

    internal open fun putInternal(key: String, value: Any?) {
        if (TextUtils.isEmpty(key) || value == null ||
                value is String && TextUtils.isEmpty(value as String?)) {
            return
        }

        if (mProperty == null) {
            mProperty = JSONObject()
            mCommitable!!.mProperty = mProperty
        }

        try {
            mProperty!!.put(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isAutoCommit) {
            mHandler.removeCallbacks(mCommitable)
            mHandler.postDelayed(mCommitable, DEFAULT_DELAYED)
        }
    }

    /**
     * 提交埋点
     */
    open fun commit() {
        mCommitable!!.commit()
    }

    fun clearProperty() {
        mProperty = null
        if (mCommitable != null) {
            mCommitable!!.active()
        }
    }

    companion object {

        protected var mHandler = Handler(Looper.getMainLooper())
        protected var DEFAULT_DELAYED: Long = 10
    }
}

/**
 * 埋点数据对象，但是此对象只能缓存数据，不能做提交等操作
 */
class TrackDataCache internal constructor() : TrackData(null, false) {

    override fun commit() {

    }

    override fun putInternal(key: String, value: Any?) {
        if (mProperty == null) {
            mProperty = JSONObject()
        }

        try {
            mProperty?.put(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}


private class EventTimerTrack internal constructor(private val eventId: String) : Commitable() {

    override fun commitInternal() {
        if (!CommonSdk.isTracker()) return
        ComponentsInstaller.mTracker.get().trackEnd(eventId, mProperty)
    }
}

private class EventTrack internal constructor(private val eventId: String) : Commitable() {

    override fun commitInternal() {
        if (!CommonSdk.isTracker()) return
        ComponentsInstaller.mTracker.get().track(eventId, mProperty)
    }
}

private class ProfileTrack internal constructor() : Commitable() {

    override fun commitInternal() {
//        if (!CommonSdk.isTracker()) return
//        ComponentsInstaller.mTracker.get().profileSet(eventId, mProperty)
//
//        SensorsDataAPI.sharedInstance().profileSet(mProperty)
    }
}

internal abstract class Commitable : Runnable {

    internal var mProperty: JSONObject? = null
    internal var mTempProperty: Map<String, Any>? = null
    internal var mCallback: OnCommitCallback? = null

    //当前commit是否为脏，如果为脏，则不能提交
    private var isDirty = false

    override fun run() {
        commit()
    }

    /**
     * 提交埋点
     */
    internal fun commit() {
        if (isDirty) {
            return
        }

        isDirty = true
        try {
            if (mCallback != null) {
                mCallback!!.onCommit()
            }
            commitInternal()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun active() {
        isDirty = false
    }

    internal abstract fun commitInternal()

    internal fun registerOnCommitCallback(callback: OnCommitCallback) {
        mCallback = callback
    }

    internal interface OnCommitCallback {
        fun onCommit()
    }
}