package com.meili.moon.sdk.app.callback


import com.meili.moon.sdk.common.Callback
import java.io.Serializable
import kotlin.reflect.KType

/**
 * Created by imuto
 */
open class SimpleKTypedCallback<ResultType>(override var typed: KType) : Callback.KTyped, Callback.SimpleCallback<ResultType>(), Serializable
