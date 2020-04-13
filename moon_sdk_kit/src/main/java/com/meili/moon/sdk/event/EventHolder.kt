package com.meili.moon.sdk.event

import java.lang.ref.WeakReference
import java.lang.reflect.Method

/**
 * Created by imuto on 2018/4/17.
 */
internal class EventHolder(val method: Method, subscriber: Any, val priority: Int, private val seq: Int) : Comparable<EventHolder> {

    private val subscriberInternal: WeakReference<Any> = WeakReference(subscriber)

    fun subscriber(): Any? {
        return subscriberInternal.get()
    }

    override fun compareTo(other: EventHolder): Int {
        var result = priority - other.priority
        if (result == 0) {
            result = seq - other.seq
        }
        return result
    }
}