package com.meili.moon.sdk.mock

import com.meili.moon.sdk.http.IRequestParams
import kotlin.reflect.KClass

/**
 * 优先级：默认
 *
 * @see [IMOCKER_PRIORITY_FROM_DB]
 * @see [IMOCKER_PRIORITY_FROM_JSON]
 * @see [IMOCKER_PRIORITY_FROM_MODEL]
 */
const val IMOCKER_PRIORITY_DEF = -88765

/**
 * 优先级：数据库
 *
 * @see [IMOCKER_PRIORITY_DEF]
 * @see [IMOCKER_PRIORITY_FROM_JSON]
 * @see [IMOCKER_PRIORITY_FROM_MODEL]
 */
const val IMOCKER_PRIORITY_FROM_DB = -1000

/**
 * 优先级：json格式的file
 *
 * @see [IMOCKER_PRIORITY_DEF]
 * @see [IMOCKER_PRIORITY_FROM_DB]
 * @see [IMOCKER_PRIORITY_FROM_MODEL]
 */
const val IMOCKER_PRIORITY_FROM_JSON = -2000

/**
 * 优先级：根据class生成
 *
 * @see [IMOCKER_PRIORITY_DEF]
 * @see [IMOCKER_PRIORITY_FROM_DB]
 * @see [IMOCKER_PRIORITY_FROM_JSON]
 */
const val IMOCKER_PRIORITY_FROM_MODEL = -3000

/**mock的大小标记*/
const val IMOCKER_SIZE_MASK = -1


/**
 * 模拟数据对象，用来模拟各种指定类型的数据
 * Created by imuto on 2018/12/12.
 */
interface MockManager : IMocker, TemplateMaker {

    /**注册一个mocker*/
    fun registerMocker(mockCreator: Mocker?)

    /**移除一个mocker*/
    fun removeMocker(mockCreator: Mocker?)

    /**注册一个模板生成器*/
    fun registerTemplateMaker(maker: TemplateMaker?)

    /**移除一个模板生成器*/
    fun removeTemplateMaker(maker: TemplateMaker?)

    /**
     * 设置最低优先级，如果注册的mocker的优先级小于此值，则此mocker不生效
     *
     * @param [minPriority] 只有 mocker.priority >= minPriority 时，mocker才能生效
     *
     */
    fun setMinPriorityThreshold(minPriority: Int)

    /**
     * 设置最高优先级，如果注册的mocker的优先级大于此值，则此mocker不生效
     *
     * @param [maxPriority] 只有 mocker.priority <= minPriority 时，mocker才能生效
     *
     */
    fun setMaxPriorityThreshold(maxPriority: Int)

    /**mock是否可用*/
    fun validate(): Boolean

    /**设置mock是否可用*/
    fun setValidate(validate: Boolean)
}

/**
 * mock数据的生成器
 */
interface Mocker : IMocker {

    @Deprecated("Mocker不提供此方法，如果是java类，请不要使用此方法，此方法不会得到回调",
            ReplaceWith("none"), DeprecationLevel.HIDDEN)
    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>, minPriority: Int, maxPriority: Int): T? {
        return null
    }

    @Deprecated("Mocker不提供此方法，如果是java类，请不要使用此方法，此方法不会得到回调",
            ReplaceWith("none"), DeprecationLevel.HIDDEN)
    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int, minPriority: Int, maxPriority: Int): MutableList<T>? {
        return null
    }

    @Deprecated("Mocker不提供此方法，如果是java类，请不要使用此方法，此方法不会得到回调",
            ReplaceWith("none"), DeprecationLevel.HIDDEN)
    override fun <T : IRequestParams> mockData(param: T, minPriority: Int, maxPriority: Int): String? {
        return null
    }

    /**mock数据生成器的优先级，高优先级会优先处理mock，如果用户指定了优先级区间，则会根据此优先级做过滤处理*/
    fun priority(): Int
}

interface IMocker {

    /**
     * 生成一个指定类型的实体数据。
     * 此方法在当前线程执行
     *
     * @param [mockId] mock的id值，用来做数据区分，比如如果数据来源是数据库，则id可能是数据库id
     * @param [mockClz] 要mock的数据类型
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : Any> mock(mockId: String? = null, mockClz: KClass<T>): T?


    /**
     * 生成一个指定类型的实体数据。
     * 此方法在当前线程执行
     *
     * @param [mockId] mock的id值，用来做数据区分，比如如果数据来源是数据库，则id可能是数据库id
     * @param [mockClz] 要mock的数据类型
     * @param [minPriority] 最小的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     * @param [maxPriority] 最大的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : Any> mock(mockId: String? = null, mockClz: KClass<T>, minPriority: Int, maxPriority: Int): T?

    /**
     * 生成一组指定类型的实体数据。
     * 此方法在当前线程执行
     *
     * @param [mockId] mock的id值，用来做数据区分，比如如果数据来源是数据库，则id可能是数据库id
     * @param [mockClz] 要mock的数据类型
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : Any> mockList(mockId: String? = null, mockClz: KClass<T>, size: Int = IMOCKER_SIZE_MASK): MutableList<T>?

    /**
     * 生成一组指定类型的实体数据。
     * 此方法在当前线程执行
     *
     * @param [mockId] mock的id值，用来做数据区分，比如如果数据来源是数据库，则id可能是数据库id
     * @param [mockClz] 要mock的数据类型
     * @param [size] mock的list大小
     * @param [minPriority] 最小的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     * @param [maxPriority] 最大的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : Any> mockList(mockId: String? = null, mockClz: KClass<T>, size: Int = IMOCKER_SIZE_MASK, minPriority: Int, maxPriority: Int): MutableList<T>?

    /**
     * 根据[param]获取一个string类型的结果，返回结果推荐使用json格式，方便使用方解析。
     * 此方法在当前线程执行
     *
     * @param [param] mock的请求参数，要求是一个[IRequestParams]类型，并且是[IRequestParams.MockFeatures]类型
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : IRequestParams> mockData(param: T): String?

    /**
     * 根据[param]获取一个string类型的结果，返回结果推荐使用json格式，方便使用方解析。
     * 此方法在当前线程执行
     *
     * @param [param] mock的请求参数，要求是一个[IRequestParams]类型，并且是[IRequestParams.MockFeatures]类型
     * @param [minPriority] 最小的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     * @param [maxPriority] 最大的优先级，默认值为[DEF_PRIORITY]，如果为[DEF_PRIORITY]则认为没有设置
     *
     * @return mock的数据，如果为null则为不支持此类型的mock
     */
    fun <T : IRequestParams> mockData(param: T, minPriority: Int, maxPriority: Int): String?
}

interface TemplateMaker {
    /**
     * 将给定参数的param，模板化，并保存起来
     *
     * @param [param] 要进行模板化的参数
     *
     * @return true，不再进行传递
     */
    fun <T : IRequestParams> template(param: T): Boolean
}