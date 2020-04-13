package com.meili.moon.mock.initializer

import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.clazz
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.toT
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties

/**
 * Created by imuto on 2018/12/13.
 */

interface Initializer {
    fun isMatch(info: InitializerInfo): Boolean
    fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T?
}

data class InitializerInfo(
        /**当前要解析的对象*/
        var annotated: KAnnotatedElement,
        /**当前对象对应的class实体*/
        var clazz: KClass<*>? = null,
        /**当前对象上的注解*/
        var annotations: List<Annotation>? = null,
        /**当前对象上的泛型信息，如果没有则为null，如果有，则size肯定大于0*/
        var typedArguments: Map<String, KTypeProjection>? = null)

internal abstract class AbsInitializer : Initializer {

    internal companion object {
        val mRandom = Random()

        /**适用给定元素初始化Info信息*/
        fun createInfo(annotated: KAnnotatedElement): InitializerInfo {
            val info = InitializerInfo(annotated)
            when (annotated) {
                is KProperty<*> -> {
                    val classifier = annotated.returnType.classifier
                    val clazz = classifier as? KClass<*> ?: return info
                    info.clazz = clazz.toT()
                    info.annotations = annotated.annotations
                }
                is KParameter -> {
                    val clazz = annotated.clazz ?: return info
                    info.clazz = clazz.toT()
                    info.annotations = annotated.annotations

                    val arguments = annotated.type.arguments
                    if (!isEmpty(arguments)) {
                        val args = mutableMapOf<String, KTypeProjection>()
                        val typeParameters = info.clazz?.typeParameters
                        typeParameters?.forEachIndexed { index, item ->
                            args[item.name] = arguments[index]
                        }
                        if (!isEmpty(args)) {
                            info.typedArguments = args
                        }
                    }
                }
                is KClass<*> -> {
                    info.clazz = annotated
                    info.annotations = annotated.annotations
                }
            }

            return info
        }

        /**从外部持有holder中，检查初始化child的泛型信息和注解信息*/
        fun fixClazzFromHolderInfo(param: KParameter, child: InitializerInfo, holder: InitializerInfo) {
            /*
                检查child的class是否已经解析，如果没有解析，则认为可能当前是泛型类型，需要从持有者对象的泛型中解析
                类似的如：

                class A<T>(val value:T)

                这时候解析value的时候，解析到的是T，T作为泛型，无法转化为class。但是T的信息是记录在A这个类上的，所以需要用持有者进行尝试解析
            */
            if (child.clazz == null) {
                val args = holder.typedArguments
                val classifier = param.type.classifier
                if (args != null && classifier is KTypeParameter) {
                    val kTypeProjection = args[classifier.name]
                    child.clazz = kTypeProjection?.type?.classifier.toT()
                }
            }

            /*
                这里的处理逻辑是，kotlin的构造方法上，使用了类成员变量，并在其上使用到了注解。
                但是在真正的构造方法的参数上，不能解析到注解信息，这里尝试使用对应名称的成员变量的注解进行修复
                这种方式不是最好的方式，但是暂时还没有发现其他解决方案
            */
            val holderClazz = holder.clazz ?: return
            val findProperty = holderClazz.declaredMemberProperties.find { it.name == param.name }
            fixInfoAnnotations(child, findProperty?.annotations)
        }

        /**将给定的[annotations]添加到[info]中*/
        fun fixInfoAnnotations(info: InitializerInfo, annotations: List<Annotation>?) {
            annotations ?: return
            val annotationsOld = info.annotations
            if (annotationsOld != null) {
                val mutableList = annotationsOld.toMutableList()
                mutableList.addAll(annotations)
                info.annotations = mutableList
            } else {
                info.annotations = annotations
            }
        }
    }

    protected fun randomChar(canNum: Boolean = true, canLetter: Boolean = true, canPunctuation: Boolean = true): Char {
        if (!canNum && !canLetter && !canPunctuation) {
            return 'a'
        }
        val nextFloat = mRandom.nextFloat()
        val count = 126 - 32
        val code = 32 + (count * nextFloat).toInt()
        if (code in 30..39) {
            if (!canNum) {
                return randomChar(canNum, canLetter, canPunctuation)
            }
        } else if (code in 65..90 || code in 97..122) {
            if (!canLetter) {
                return randomChar(canNum, canLetter, canPunctuation)
            }
        } else {
            if (!canPunctuation) {
                return randomChar(canNum, canLetter, canPunctuation)
            }
        }
        return code.toChar()
    }

    protected inline fun <reified T : Annotation> List<Annotation>?.findAnnotation(): T? {
        this ?: return null
        return find { it is T } as? T
    }

}