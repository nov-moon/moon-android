package com.meili.moon.sdk.permission.internal

import java.lang.reflect.Method

/**
 * Author wudaming
 * Created on 2019/1/17
 * 反射工具类
 */
object Util {


    /**
     * 根据方法名、方法描述在host上查找对应的方法。
     */
    fun findMethod(host: Any, methodName: String, methodDesc: String): Method? {
        var method: Method? = null

        try {
            if (methodDesc.length == 2) {
                //methodDesc=="()"，也就是无参数方法
                method = host.javaClass.getMethod(methodName)
            } else {
                host.javaClass.declaredMethods.filter { it.name == methodName }.forEach {
                    if (method != null) {
                        return@forEach
                    }
                    val desc = generateParamDesc(it)
                    if (desc == methodDesc.subSequence(0, methodDesc.lastIndexOf(")"))) {
                        method = it
                        return@forEach
                    }
                }
                if (method == null) {
                    host.javaClass.methods.filter { it.name == methodName }.forEach {
                        if (method != null) {
                            return@forEach
                        }
                        val desc = generateParamDesc(it)
                        if (desc == methodDesc.subSequence(0, methodDesc.lastIndexOf(")"))) {
                            method = it
                            return@forEach
                        }
                    }
                }
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        method?.isAccessible = true
        return method
    }


    /**
     * 提取一个方法的虚拟机描述
     */
    private fun generateParamDesc(method: Method): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(")
        for (type in method.parameterTypes) {
            stringBuilder.append(getDesc(type))
        }

        return stringBuilder.toString().replace(".", "/")
    }


    /**
     * 获取一个类的虚拟机描述类型
     */
    private fun getDesc(clazz: Class<*>): String {
        return if (clazz.isArray) {
            clazz.name
        } else {
            type2Desc(clazz.name)
        }
    }

    /**
     * 将变量的运行时类型转换成对应的虚拟机描述类型
     */
    private fun type2Desc(type: String): String {
        return when (type) {
            "boolean" -> "Z"
            "char" -> "C"
            "byte" -> "B"
            "short" -> "S"
            "int" -> "I"
            "float" -> "F"
            "long" -> "J"
            "double" -> "D"
            else -> "L$type;"
        }

    }
}