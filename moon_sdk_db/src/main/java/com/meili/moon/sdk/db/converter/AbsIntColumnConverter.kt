package com.meili.moon.sdk.db.converter

import com.meili.moon.sdk.db.sqlite.ColumnDbType


/**
 * int类型的转换器
 * Created by imuto on 17/12/27.
 */
abstract class AbsIntColumnConverter<PropertyType> : ColumnConverter<PropertyType> {
    override fun getColumnType() = ColumnDbType.INTEGER
}