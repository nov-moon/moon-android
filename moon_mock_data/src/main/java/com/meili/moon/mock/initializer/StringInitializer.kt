package com.meili.moon.mock.initializer

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.mock.*
import com.meili.moon.sdk.util.closeIt
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.toT

/**
 * Created by imuto on 2018/12/22.
 */
internal object StringInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        return info.clazz == String::class || info.clazz == CharSequence::class
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val annotations = info.annotations
        val valueAnnotation = annotations.findAnnotation<MockString>()
        if (valueAnnotation != null) {
            val value = valueAnnotation.value
            if (!isEmpty(value)) {
                return value.toT()
            }
        }

        val enumAnnotation = annotations.findAnnotation<MockStringEnum>()

        if (enumAnnotation != null) {
            val enum = enumAnnotation.enum
            if (enum.isNotEmpty()) {
                val index = (mRandom.nextFloat() * enum.size).toInt()
                return enum[index].toT()
            }
        }

        val rangeAnnotation = annotations.findAnnotation<MockStringRange>()
        if (rangeAnnotation != null) {

            val result = randomStr(rangeAnnotation.minLen, rangeAnnotation.maxLen,
                    rangeAnnotation.startWith, rangeAnnotation.endWith,
                    rangeAnnotation.canIncludeNum, rangeAnnotation.canIncludeLetter,
                    rangeAnnotation.canIncludePunctuation)
            if (!isEmpty(result)) {
                return result.toT()
            }
        }

        val dicAnnotation = annotations.findAnnotation<MockStringDic>()
        if (dicAnnotation != null) {
            val dictionary = dicAnnotation.dictionary
            if (!dictionary.isEmpty()) {
                val inputStream = CommonSdk.app().assets.open(dictionary)
                val reader = inputStream.reader()
                try {
                    val lines = reader.readLines()
                    if (!isEmpty(lines)) {
                        val index = (lines.size * mRandom.nextFloat()).toInt()
                        return lines[index].toT()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    inputStream.closeIt()
                    reader.closeIt()
                }
            }
        }

        return randomStr().toT()
    }

    private fun randomStr(min: Int = 1, max: Int = 20,
                          startWith: String = "", endWith: String = "",
                          canIncludeNum: Boolean = true, canIncludeLetter: Boolean = true,
                          canIncludePunctuation: Boolean = true): String? {
        var maxLen = max
        val minLen = min

        //判定maxLen和minLen为合法值
        if (maxLen >= 0 && minLen >= 0 && maxLen > minLen) {
            //判定maxLen如果=0，则给maxLen默认值
            if (maxLen == 0) {
                maxLen = 20
            }

            val suffixLen = startWith.length + endWith.length
            if (maxLen <= suffixLen) {
                return (startWith + endWith)
            }
            var count = maxLen - minLen
            var startLen = minLen
            if (suffixLen > minLen) {
                count = maxLen - suffixLen
                startLen = suffixLen
            }

            val len = startLen + (mRandom.nextFloat() * count).toInt()

            if (len == suffixLen) {
                return (startWith + endWith)
            }

            val randomLen = len - suffixLen
            val sb = StringBuilder()

            for (i in 0 until randomLen) {
                sb.append(randomChar(canIncludeNum,
                        canIncludeLetter, canIncludePunctuation))
            }
            return sb.toString()
        }
        return null
    }
}