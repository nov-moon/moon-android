package com.meili.moon.sdk.app.base.adapter.annotation

import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder
import kotlin.reflect.KClass

/**
 *  列表页面的viewHolder绑定注解
 * Created by imuto on 2018/5/21.
 */
@Target(AnnotationTarget.CLASS)
annotation class ViewHolders(
        /**ViewHolder的class类，可以是多个*/
        vararg val value: KClass<out AbsViewHolder<*>>,
        /**
         * 如果ViewHolder有多个的话，viewType用于把数据源和对应的ViewHolder关联起来。
         * viewType的值代表数据源对象的属性名称。
         * exp：viewType = type,那么在数据对象中必须有{type：Int}属性。
         * 根据type的值在value数组中取出相应下标的ViewHolder。
         * */
        val viewType: String = "")