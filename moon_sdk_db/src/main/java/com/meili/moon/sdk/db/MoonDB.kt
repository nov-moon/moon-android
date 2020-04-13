package com.meili.moon.sdk.db

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent

/**
 * db管理类
 * Created by imuto on 18/1/25.
 */
object MoonDB : DBManager, IComponent {

    override var logable: Boolean = false

    override fun init(env: Environment) {
        ComponentsInstaller.installEnvironment(env)
        ComponentsInstaller.installDb(this)
    }

    private var defDBConfig = IDB.Config()

    override fun getDBInstance(dbConfig: IDB.Config?): IDB {
        val config = dbConfig ?: defDBConfig
        return DBImpl.getInstance(CommonSdk.app(), config)
    }

    override fun setDefaultDBConfig(dbConfig: IDB.Config) {
        defDBConfig = dbConfig
    }
}