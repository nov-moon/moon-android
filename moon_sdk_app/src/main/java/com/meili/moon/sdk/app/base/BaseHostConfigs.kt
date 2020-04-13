package com.meili.moon.sdk.app.base

import android.content.Context
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.VersionUtils
import com.meili.moon.sdk.base.util.isDebug
import com.meili.moon.sdk.base.util.post
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.http.common.BaseModel
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.throwOnDebug
import java.util.*

/**
 * Created by imuto on 16/5/24.
 */
open class BaseHostConfigs {

    private val hostMap = HashMap<String, MutableList<HostExtra>>()

    /**
     * @param key 用来区分host的分组
     * @param id 用来区分相同host分组下的不同host
     *
     * @param dev     开发
     * @param test    测试
     * @param pre     预发
     * @param stg     灰度
     * @param release 线上
     */
    class Hosts(val key: String,
                val id: Int,
                val dev: String,
                val test: String,
                val release: String,
                val pre: String = "",
                val stg: String = "") : BaseModel() {

        private val mSp = Sdk.app().getSharedPreferences("host_config_${key}_$id", Context.MODE_PRIVATE)

        private var hosts = mutableListOf<HostItem>()

        private val hostKey = "hosts"

        init {
            mSp.edit().putString("key", key).apply()

            initHost()
        }

        fun select(item: HostItem) {
            if (!VersionUtils.isDebug()) {
                return
            }
            hosts.forEach {
                it.isSelected = false
            }
            item.isSelected = true

            sync()
        }

        fun updateCustom(host: String, name: String? = null) {
            val hostItem = hosts.find { it.buildType.isCustom } ?: return
            hostItem.name = name
            hostItem.host = host
            hostItem.isActive = host.isNotEmpty()

            select(hostItem)
        }

        fun getViewHosts(): List<HostItem> {
            return hosts.filter { it.isActive }
        }

        fun get(): String {
            if (!VersionUtils.isDebug()) {
                return release
            }

            //如果有匹配到runtime的host，直接使用runtime的host
            var host = hosts.find { it.buildType == RuntimeType.buildType }

            if (host == null) {
                //如果没有runtime的host，尝试使用已经选中的host
                host = hosts.find { it.isSelected && it.isActive }
            }

            return host?.host ?: hosts[0].host
        }

        /**
         * 初始化host设置
         */
        private fun initHost() {
            if (!isDebug) {
                return
            }
            val hostStr = mSp.getString(hostKey, "")
            if (hostStr.isEmpty()) {
                //使用内置的类型
                hosts.apply {

                    val buildType = RuntimeType.buildType

                    VersionUtils.BuildType.values().forEach {

                        val isSelected = buildType == it

                        val itemHost = when (it) {
                            VersionUtils.BuildType.BUILD_TYPE_DEBUG -> {
                                dev
                            }
                            VersionUtils.BuildType.BUILD_TYPE_DEV -> {
                                test
                            }
                            VersionUtils.BuildType.BUILD_TYPE_PRE_RELEASE -> {
                                pre
                            }
                            VersionUtils.BuildType.BUILD_TYPE_STAGE -> {
                                stg
                            }
                            VersionUtils.BuildType.BUILD_TYPE_RELEASE -> {
                                release
                            }
                            else -> {
                                ""
                            }
                        }

                        add(HostItem(itemHost, it.getName(), it, isSelected, itemHost.isNotEmpty()))
                    }
                    sync()
                }
                return
            }

            hosts = Sdk.json().toList(hostStr, HostItem::class.java)
            hosts.forEach {
                when (it.buildType) {
                    VersionUtils.BuildType.BUILD_TYPE_DEBUG -> {
                        it.host = dev
                    }
                    VersionUtils.BuildType.BUILD_TYPE_DEV -> {
                        it.host = test
                    }
                    VersionUtils.BuildType.BUILD_TYPE_PRE_RELEASE -> {
                        it.host = pre
                    }
                    VersionUtils.BuildType.BUILD_TYPE_STAGE -> {
                        it.host = stg
                    }
                    VersionUtils.BuildType.BUILD_TYPE_RELEASE -> {
                        it.host = release
                    }
                    else -> {
                    }
                }
            }

            sync()
        }

        private fun sync() {
            post {
                if (getActiveHost(key) == this) {
                    hosts.find { it.isSelected }?.apply {
                        RuntimeType.buildType = buildType
                    }
                }
            }

            val custom = hosts.find { it.buildType == VersionUtils.BuildType.BUILD_TYPE_CUSTOM }
            if (custom != null) {
                hosts.remove(custom)
                hosts.add(custom)
            }

            hosts.sortBy {
                if (it.buildType != VersionUtils.BuildType.BUILD_TYPE_CUSTOM) {
                    0
                } else 1
            }

            val toJson = Sdk.json().toJson(hosts)
            mSp.edit().putString(hostKey, toJson).apply()

        }

        data class HostItem(var host: String, var name: String?, var buildType: VersionUtils.BuildType,
                            var isSelected: Boolean = false,
                            var isActive: Boolean = true) : IKeyValueModel {

            override var isKeyValueSelected: Boolean
                get() = isSelected
                set(value) {
                    isSelected = value
                }

            override fun getKeyValueName(): String {
                return if (isEmpty(name)) host else "$name: $host"
            }
        }
    }

    companion object {
        private val instance = BaseHostConfigs()

        /**
         * 注册主机地址
         */
        fun register(hosts: Hosts, isActive: Boolean) {
            instance.apply {
                val hostList = hostMap[hosts.key] ?: mutableListOf()
                hostMap[hosts.key] = hostList
                hostList.add(HostExtra(hosts, isActive))
            }
        }

        /**激活指定host*/
        fun activeHost(hosts: Hosts) {
            instance.apply {
                val hostList = hostMap[hosts.key]
                if (isEmpty(hostList)) {
                    throwOnDebug(BaseException(msg = "不存在指定host"))
                }
                hostList?.forEach {
                    it.isActive = it.host == hosts
                }
            }
        }

        /**
         * 注销主机地址
         */
        fun unregister(hosts: Hosts?) {
            if (hosts == null) {
                return
            }
            instance.hostMap.remove(hosts.key)
        }

        fun get(key: String): String? {
            return getActiveHost(key).get()
        }

        fun get(key: String, id: Int): String? {
            val hosts = instance.hostMap[key]
            if (isEmpty(hosts)) {
                throwOnDebug(BaseException(msg = "没有对应的host"))
            }
            val h = hosts!!.find { it.host.id == id }
            if (h == null) {
                throwOnDebug(BaseException(msg = "没有激活的host"))
            }
            return h!!.host.get()
        }

        fun getActiveHost(key: String): Hosts {
            val hosts = instance.hostMap[key]
            if (isEmpty(hosts)) {
                throwOnDebug(BaseException(msg = "没有对应的host"))
            }
            val h = hosts!!.find { it.isActive }
            if (h == null) {
                throwOnDebug(BaseException(msg = "没有激活的host"))
            }
            return h!!.host
        }
    }

    data class HostExtra(var host: Hosts,
                         var isActive: Boolean = true) : BaseModel()

}
