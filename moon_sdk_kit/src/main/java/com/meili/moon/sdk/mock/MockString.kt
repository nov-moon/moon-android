package com.meili.moon.sdk.mock

/**
 * 适用类型：string，否则无效
 *
 * 注解属性的取值范围
 *
 * 使用指定的值作为mock的返回结果，优先级最高
 *
 * @param [value] 确定的返回结果
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockString(val value: String)


/**
 * 适用类型：string，否则无效
 *
 * 注解属性的取值范围
 *
 * 优先级第三
 *
 * 你可以指定当前mock的最小长度和最大长度，[minLen]、[maxLen]必须大于等于0，否则认为是无效的
 *
 * [minLen]应该小于[maxLen]，否则则认为[minLen]无效
 *
 * 如果[startWith]+[endWith]的长度大于[maxLen]，则直接返回两个拼接的字符串
 *
 * @param [minLen] 字符串的最小长度
 * @param [maxLen] 字符串的最大长度
 * @param [startWith] 字符串的开始字符
 * @param [endWith] 字符串的结束字符
 * @param [canIncludeNum] 是否可以包含数字
 * @param [canIncludeLetter] 是否可以包含字母
 * @param [canIncludePunctuation] 是否可以包含标点符号
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockStringRange(val minLen: Int = 5, val maxLen: Int = 20,
                                 val startWith: String = "", val endWith: String = "",
                                 val canIncludeNum: Boolean = true,
                                 val canIncludeLetter: Boolean = true,
                                 val canIncludePunctuation: Boolean = true)

/**
 * 适用类型：string，否则无效
 *
 * 注解属性的取值范围
 *
 * 优先级第二
 *
 * [enum]枚举出了所有的可选字符串，将在其中随机进行mock
 *
 * @param [enum] 字符串的枚举值
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockStringEnum(vararg val enum: String)


/**
 * 适用类型：string，否则无效
 *
 * 注解属性的取值范围
 *
 * 优先级第四
 *
 * 字典注解，将使用type在asset目录下的dictionary目录，找对应文件，并在其中随机取值。
 * 建议type使用常亮或枚举进行管理，例如[MockStringDicType]中的定义。
 * 在dictionary目录中的文件定义，以换行为一个item
 *
 * @param [dictionary] 字典的名称，路径为：assets/mock/dictionary/$type
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockStringDic(val dictionary: String)

enum class MockStringDicType(val dicName: String) {
    /**ID*/
    ID("ids"),
    /**名称*/
    NAME("names"),
    /**title*/
    TITLE("titles"),
    /**地址*/
    ADDRESS("address"),
    /**公司名称*/
    COMPANY("company"),
    /**省份*/
    PROVINCE("province"),
    /**城市*/
    CITY("city"),
    /**车辆品牌*/
    CAR_BRAND("car_brand"),
    /**车辆车系*/
    CAR_SERIES("car_series"),
    /**车辆车型*/
    CAR_MODEL("car_model")
}
