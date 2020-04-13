package com.meili.moon.sdk.db.sqlite

/**
 * Created by imuto on 18/1/3.
 */
enum class ColumnDbType(val value: String) {
    INTEGER("INTEGER"),
    REAL("REAL"),
    TEXT("TEXT"),
    BLOB("BLOB");

    override fun toString(): String {
        return value
    }
}