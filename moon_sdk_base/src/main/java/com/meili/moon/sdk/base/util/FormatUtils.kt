package com.meili.moon.sdk.base.util

import android.text.TextUtils
import java.util.*
import java.util.regex.Pattern

/**
 * Created by imuto on 2018/9/17.
 */


/**是否是电话号码*/
fun String?.isPhoneNum(): Boolean {
    return isMobilePhoneNum() || isTelephoneNum()
}

/**是否是手机电话号码*/
fun String?.isMobilePhoneNum(): Boolean {
    if (this == null || this.trim() == "") {
        return false
    }
    val replaceStr = this.replace("-", "")
            .replace(" ", "")

    if (!TextUtils.isDigitsOnly(replaceStr)) return false

    return replaceStr.length == 11
}

/**是否是座机电话号码*/
fun String?.isTelephoneNum(): Boolean {
    if (this == null || this.trim() == "") {
        return false
    }
    val replaceStr = this.replace("-", "")
            .replace(" ", "")

    if (!TextUtils.isDigitsOnly(replaceStr)) return false

    return replaceStr.length >= 11
}

fun String?.isIDNum(): Boolean {
    this ?: return false
    return this.length == 18
}
fun String?.isDigits(): Boolean {
    this ?: return false
    return TextUtils.isDigitsOnly(this)
}

/**
 * 身份证号是否基本有效
 */
fun String?.isIDNumOld(): Boolean {
    this ?: return false
    val idNum = this
    if (idNum.length != 15 && idNum.length != 18)
        return false
    val cs = idNum.toUpperCase().toCharArray()
    // （1）校验位数
    var power = 0
    for (i in cs.indices) {// 循环比正则表达式更快
        if (i == cs.size - 1 && cs[i] == 'X')
            break// 最后一位可以是X或者x
        if (cs[i] < '0' || cs[i] > '9')
            return false
        if (i < cs.size - 1)
            power += (cs[i] - '0') * POWER_LIST[i]
    }
    // （2）校验区位码
    if (!zoneNum.containsKey(Integer.valueOf(idNum.substring(0, 2)))) {
        return false
    }
    // （3）校验年份
    val year = if (idNum.length == 15) "19" + idNum.substring(6, 8) else idNum.substring(6, 10)
    val iyear = Integer.parseInt(year)
    if (iyear < 1900 || iyear > Calendar.getInstance().get(Calendar.YEAR)) {
        return false// 1900年的PASS，超过今年的PASS
    }
    // （4）校验月份
    val month = if (idNum.length == 15) idNum.substring(8, 10) else idNum.substring(10, 12)
    val imonth = Integer.parseInt(month)
    if (imonth < 1 || imonth > 12)
        return false
    // （5）校验天数
    val day = if (idNum.length == 15) idNum.substring(10, 12) else idNum.substring(12, 14)
    val iday = Integer.parseInt(day)
    if (iday < 1 || iday > 31)
        return false
    // （6）校验一个合法的年月日
    if (!validate(iyear, imonth, iday))
        return false
    // （7）校验“校验码”
    return if (idNum.length == 15) true else cs[cs.size - 1].toInt() == PARITYBIT[power % 11]
}

/**当前char是否为汉字*/
fun Char.isChinese(): Boolean {
    val v = toInt()
    return v in 19968..171941
}

private val zoneNum: MutableMap<Int, String> = mutableMapOf(
        Pair(11, "北京"),
        Pair(12, "天津"),
        Pair(13, "河北"),
        Pair(14, "山西"),
        Pair(15, "内蒙古"),
        Pair(21, "辽宁"),
        Pair(22, "吉林"),
        Pair(23, "黑龙江"),
        Pair(31, "上海"),
        Pair(32, "江苏"),
        Pair(33, "浙江"),
        Pair(34, "安徽"),
        Pair(35, "福建"),
        Pair(36, "江西"),
        Pair(37, "山东"),
        Pair(41, "河南"),
        Pair(42, "湖北"),
        Pair(43, "湖南"),
        Pair(44, "广东"),
        Pair(45, "广西"),
        Pair(46, "海南"),
        Pair(50, "重庆"),
        Pair(51, "四川"),
        Pair(52, "贵州"),
        Pair(53, "云南"),
        Pair(54, "西藏"),
        Pair(61, "陕西"),
        Pair(62, "甘肃"),
        Pair(63, "青海"),
        Pair(64, "宁夏"),
        Pair(65, "新疆"),
        Pair(71, "台湾"),
        Pair(81, "香港"),
        Pair(82, "澳门"),
        Pair(91, "国外")
)

private val PARITYBIT = intArrayOf('1'.toInt(), '0'.toInt(), 'X'.toInt(), '9'.toInt(), '8'.toInt(), '7'.toInt(), '6'.toInt(), '5'.toInt(), '4'.toInt(), '3'.toInt(), '2'.toInt())
private val POWER_LIST = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)

private fun validate(year: Int, month: Int, day: Int): Boolean {
    // 比如考虑闰月，大小月等
    return true
}

/**
 * 大陆号码或香港号码均可
 */
private fun isPhoneLegal(str: String): Boolean {
    return isChinaPhoneLegal(str) || isHKPhoneLegal(str)
}

/**
 * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
 * 此方法中前三位格式有：
 * 13+任意数
 * 15+除4的任意数
 * 18+除1和4的任意数
 * 17+除9的任意数
 * 147
 */
private fun isChinaPhoneLegal(str: String): Boolean {
    val regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$"
    val p = Pattern.compile(regExp)
    val m = p.matcher(str)
    return m.matches()
}

/**
 * 香港手机号码8位数，5|6|8|9开头+7位任意数
 */
private fun isHKPhoneLegal(str: String): Boolean {
    val regExp = "^(5|6|8|9)\\d{7}$"
    val p = Pattern.compile(regExp)
    val m = p.matcher(str)
    return m.matches()
}

/**
 * 验证固话号码
 * @param telephone
 * @return
 */
private fun checkTelephone(telephone: String): Boolean {
    //带区号
    val regex = "^[0][1-9]{2,3}-[0-9]{5,10}$"
    //不带区号
//    val regex = "^[1-9]{1}[0-9]{5,8}$"
//    val regex = "^(0\\d{2}-\\d{8}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(telephone)
    return matcher.matches()
}