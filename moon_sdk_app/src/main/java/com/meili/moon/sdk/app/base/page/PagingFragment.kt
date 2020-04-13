package com.meili.moon.sdk.app.base.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.adapter.AbsAdapter
import com.meili.moon.sdk.app.base.adapter.ViewHolderCreator
import com.meili.moon.sdk.app.base.adapter.annotation.ViewHolders
import com.meili.moon.sdk.app.base.adapter.dataset.IDataSet
import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder
import com.meili.moon.sdk.http.common.BaseModel
import com.meili.moon.sdk.page.exception.StartPageException
import com.meili.moon.sdk.util.largerSize
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.findAnnotation

/**
 * 新的包装PagingFragment，慢慢会弃用老的PagingOlderFragment
 * Created by imuto on 2018/5/21.
 */
abstract class PagingFragment<DataType, ItemType : BaseModel> : PagingOlderFragment<DataType>() {

    private var mAdapter: AbsAdapter<ItemType>? = null

    /**绑定到ViewHolder的DataSet*/
    protected abstract val mDataSet: IDataSet<ItemType>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val createdView = super.onCreateView(inflater, container, savedInstanceState)
        if (createdView == null || createdView.background != null) {
            return createdView
        }
        createdView.setBackgroundColor(pageActivity!!.resources.getColor(R.color.page_background))
        return createdView
    }

    override fun getListAdapter(): AbsAdapter<ItemType> {
        if (mAdapter == null) {
            val annotation = this::class.findAnnotation<ViewHolders>()
                    ?: throw StartPageException(msg = "请在类上使用ViewHolders注解绑定ViewHolder类")

            val viewHolderArray = annotation.value
            mAdapter = if (largerSize(viewHolderArray, 1)) {
                val viewHolderCreator = object : ViewHolderCreator<ItemType> {
                    override fun getItemViewType(position: Int, data: ItemType): Int {
                        val typedData = getDataType() as Class<*>
                        val field = typedData.declaredFields.find { it.name == annotation.viewType }
                        field!!.isAccessible = true
                        return field.getInt(data)
                    }

                    override fun getItemViewHolder(viewType: Int): Class<out AbsViewHolder<*>> {
                        return viewHolderArray[viewType].java
                    }
                }

                AbsAdapter<ItemType>(pageActivity, mDataSet, viewHolderCreator)
            } else {
                AbsAdapter<ItemType>(pageActivity, mDataSet, viewHolderArray[0].java)
            }
        }
        return mAdapter!!
    }

    protected override fun getResType(): Type {
        val p = findParameterizedType(javaClass.genericSuperclass)
        return p!!.actualTypeArguments[0]
    }

    protected fun getDataType(): Type {
        val p = findParameterizedType(javaClass.genericSuperclass)
        return p!!.actualTypeArguments[1]
    }

    private fun findParameterizedType(type: Type): ParameterizedType? {
        if (type is ParameterizedType) {
            return type
        }
        if (type === PagingFragment::class.java) {
            return null
        }
        val genericSuperclass = (type as Class<*>).genericSuperclass
        return findParameterizedType(genericSuperclass)
    }
}