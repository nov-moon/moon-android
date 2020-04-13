package com.meili.moon.sdk.app.base.prefs

import android.content.Context
import android.content.SharedPreferences
import com.meili.moon.sdk.app.base.role.DefaultRole
import com.meili.moon.sdk.app.base.role.UserRole
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.VersionUtils

/**
 *
 * 账户之间共享的prefs
 *
 * Created by imuto on 2018/5/27.
 */
interface CommonPrefs {

    /**是否已经登录*/
    val isLogin: Boolean

    /**引导页的version，用来作为[isNoReadGuide]的key*/
    var guideVersion: String

    /**prefs对象*/
    val mPreferences: SharedPreferences

    /**
     * 设置最后忽略升级版本号
     */
    var lastIgnoreVersion: String

    /**
     * 设置是否读取过引导页面
     */
    var isNoReadGuide: Boolean

    /**
     * 设置第一次进入  不建议其他地方修改改值
     */
    var isFirstOpenApp: Boolean
    /**
     * 设置第一次查看年度报告
     */
    var firstReportClick: Boolean

    /**
     * 登录记录
     */
    var accountHistory: String

    /**
     * 摇一摇 本地环境 记录
     */
    var historyNativeIP: String

    /**
     * 是否已经清除版本登录信息
     */
    var hasClearVersionLogin: Boolean

    var userRole:UserRole

    /**设置委托对象的holder*/
    fun setHolder(holder: CommonPrefs)

    /**清除记录信息*/
    fun clear()

    /**
     * 获取搜索关键字记录
     */
    fun getSearchKeys(from: String): String

    /**
     * 设置搜索关键字
     */
    fun setSearchKeys(from: String, keys: String)

    object CommonPrefsImpl : CommonPrefs {

        private lateinit var holderPrefs: CommonPrefs

        override fun setHolder(holder: CommonPrefs) {
            holderPrefs = holder
        }

        override val isLogin: Boolean
            get() = holderPrefs.isLogin

        override var guideVersion: String = "1.0.0"

        override var userRole: UserRole = DefaultRole()

        override var isFirstOpenApp: Boolean
            get() = mPreferences.getBoolean("is_first_open_app", true)
            set(value) {
                mPreferences.edit().putBoolean("is_first_open_app", value).apply()
            }

        /**是否第一次点击我的年度报告*/
        override var firstReportClick: Boolean
            get() = mPreferences.getBoolean("first_click_report", true)
            set(value) {
                mPreferences.edit().putBoolean("first_click_report", value).apply()
            }

        override var historyNativeIP: String
            get() = mPreferences.getString("history_native_ip", "http://172.28.86.100:9000/")
            set(value) {
                mPreferences.edit().putString("history_native_ip", value).apply()
            }

        override var hasClearVersionLogin: Boolean
            get() = mPreferences.getBoolean("has_clear_login_info_" + VersionUtils.getVersionCode(), false)
            set(value) {
                mPreferences.edit().putBoolean("has_clear_login_info_" + VersionUtils.getVersionCode(), value).apply()
            }

        override val mPreferences: SharedPreferences = Sdk.app()
                .getSharedPreferences("common_info", Context.MODE_PRIVATE)

        override var lastIgnoreVersion: String
            get() = mPreferences.getString("last_ignore_version", "")
            set(version) = mPreferences.edit().putString("last_ignore_version", version).apply()


        override var isNoReadGuide: Boolean
            get() = mPreferences.getBoolean("first_install_$guideVersion", true)
            set(isFirst) = mPreferences.edit().putBoolean("first_install_$guideVersion", isFirst).apply()

        override var accountHistory: String
            get() = mPreferences.getString("test_login_history", "")
            set(json) = mPreferences.edit().putString("test_login_history", json).apply()

        override fun clear() {
            mPreferences.edit().clear().apply()

        }

        override fun getSearchKeys(from: String): String {
            return mPreferences.getString("search_keys$from", "")
        }

        override fun setSearchKeys(from: String, keys: String) {
            mPreferences.edit().putString("search_keys$from", keys).apply()
        }
    }

}