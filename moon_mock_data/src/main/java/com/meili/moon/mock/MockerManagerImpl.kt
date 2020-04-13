package com.meili.moon.mock

import com.meili.moon.mock.mocker.FileMocker
import com.meili.moon.mock.mocker.ModelMocker
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.mock.IMOCKER_PRIORITY_DEF
import com.meili.moon.sdk.mock.MockManager
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.mock.TemplateMaker
import com.meili.moon.sdk.util.isEmpty
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass

/**
 * Created by imuto on 2018/12/12.
 */
object MockerManagerImpl : MockManager, IComponent {

    private var isValidate = true

    private var minPriorityThreshold = Int.MIN_VALUE
    private var maxPriorityThreshold = Int.MAX_VALUE

    @Transient
    private val mTempMocker = TempMocker(0)
    @Transient
    private val mTempMockerThresholdMax = TempMocker(maxPriorityThreshold)

    private val mMockerList = ConcurrentSkipListSet<Mocker>(Comparator<Mocker> { mocker: Mocker, mocker1: Mocker ->
        return@Comparator when {
            mocker.priority() > mocker1.priority() -> 1
            mocker.priority() == mocker1.priority() -> 0
            else -> -1
        }
    })

    private val mMakerList = ConcurrentSkipListSet<TemplateMaker>(Comparator<TemplateMaker> { _: TemplateMaker, _: TemplateMaker ->
        return@Comparator 0
    })

    init {
        registerMocker(ModelMocker())
        registerMocker(FileMocker())
    }

    override fun init(env: Environment) {
        ComponentsInstaller.installMocker(this, env)
    }

    override fun validate(): Boolean {
        return isValidate
    }

    override fun setValidate(validate: Boolean) {
        isValidate = validate
    }

    override fun registerMocker(mockCreator: Mocker?) {
        if (mockCreator == null) return
        mMockerList.add(mockCreator)
    }

    override fun removeMocker(mockCreator: Mocker?) {
        if (mockCreator == null) return
        mMockerList.remove(mockCreator)
    }

    override fun registerTemplateMaker(maker: TemplateMaker?) {
        maker ?: return
        mMakerList.add(maker)
    }

    override fun removeTemplateMaker(maker: TemplateMaker?) {
        mMakerList.remove(maker)
    }

    override fun <T : IRequestParams> template(param: T): Boolean {
        mMakerList.forEach {
            if (it.template(param)) {
                return true
            }
        }
        return false
    }

    @Synchronized
    override fun setMinPriorityThreshold(minPriority: Int) {
        minPriorityThreshold = minPriority
    }

    @Synchronized
    override fun setMaxPriorityThreshold(maxPriority: Int) {
        maxPriorityThreshold = maxPriority
        mTempMockerThresholdMax.priority = maxPriorityThreshold
    }

    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>): T? {
        return mock(mockId, mockClz, IMOCKER_PRIORITY_DEF, IMOCKER_PRIORITY_DEF)
    }

    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>, minPriority: Int, maxPriority: Int): T? {
        return mockInner(mockId, mockClz, getPriorityCollection(minPriority, maxPriority))
    }

    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int): MutableList<T>? {
        return mockList(mockId, mockClz, size, IMOCKER_PRIORITY_DEF, IMOCKER_PRIORITY_DEF)
    }

    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int, minPriority: Int, maxPriority: Int): MutableList<T>? {
        return mockListInner(mockId, mockClz, getPriorityCollection(minPriority, maxPriority))
    }

    override fun <T : IRequestParams> mockData(param: T): String? {
        return mockData(param, IMOCKER_PRIORITY_DEF, IMOCKER_PRIORITY_DEF)
    }

    override fun <T : IRequestParams> mockData(param: T, minPriority: Int, maxPriority: Int): String? {
        return mockDataInner(param, getPriorityCollection(minPriority, maxPriority))
    }

    private fun <T : Any> mockInner(mockId: String?, mockClz: KClass<T>, collection: Collection<Mocker>): T? {
        collection.forEach {
            val result = it.mock(mockId, mockClz)
            if (result != null) return result
        }

        return null
    }

    private fun <T : Any> mockListInner(mockId: String?, mockClz: KClass<T>, collection: Collection<Mocker>): MutableList<T>? {
        collection.forEach {
            val result = it.mockList(mockId, mockClz)
            if (result != null) return result
        }

        return null
    }

    private fun <T : IRequestParams> mockDataInner(param: T, collection: Collection<Mocker>): String? {

        collection.forEach {
            val result = it.mockData(param)
            if (!isEmpty(result)) return result
        }

        return null
    }

    private fun getPriorityCollection(minPriority: Int, maxPriority: Int): Collection<Mocker> {
        synchronized(mTempMocker) {
            val tempSet = mMockerList
            val result = if (minPriority == IMOCKER_PRIORITY_DEF && maxPriority == IMOCKER_PRIORITY_DEF) {
                //使用默认优先级处理方式
                mTempMocker.priority = minPriorityThreshold
                synchronized(mTempMockerThresholdMax) {
                    tempSet.subSet(mTempMocker, true, mTempMockerThresholdMax, true)
                }
            } else if (minPriority != IMOCKER_PRIORITY_DEF) {
                //使用最小值自定义方式
                mTempMocker.priority = minPriority
                tempSet.tailSet(mTempMocker, true)
            } else {
                //使用最大值自定义方式
                mTempMocker.priority = maxPriority
                tempSet.headSet(mTempMocker, true)
            }
            return result.descendingSet()
        }
    }

    private class TempMocker(var priority: Int) : Mocker {
        override fun priority(): Int = priority

        override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>): T? {
            return null
        }

        override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int): MutableList<T>? {
            return null
        }

        override fun <T : IRequestParams> mockData(param: T): String? {
            return null
        }
    }
}