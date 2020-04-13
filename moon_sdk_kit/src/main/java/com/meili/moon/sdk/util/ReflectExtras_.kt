@file:JvmName("ReflectUtils")

package com.meili.moon.sdk.util

import com.meili.moon.sdk.common.ReflectException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.reflect

/**
 * 反射相关的扩展工具类
 * Created by imuto on 2018/12/27.
 */

/**指定type是否为数组*/
@Deprecated("已经弃用，因为这个方法不能明确区分list和array，这种定义方式不好")
fun Type.isArrayType(): Boolean {
    var loadType = this
    if (loadType is ParameterizedType) {
        loadType = loadType.rawType
    }
    var result = List::class.java.isAssignableFrom(loadType as Class<*>)
    if (!result) {
        result = loadType.isArray
    }
    return result
}

/**
 * 当前KAnnotatedElement是否是一个List
 */
val KAnnotatedElement?.isList: Boolean
    get() {
        this ?: return false
        val clazz = getReturnKClass()
        clazz ?: return false
        return this == List::class || this == ArrayList::class || this == Collection::class
    }


/**
 * 当前对象是否是数组，不包含list和map类，只是指数组
 */
val KAnnotatedElement?.isArray: Boolean
    get() {
        this ?: return false
        val clazz = getReturnKClass()

        val jClazz = clazz?.java

        return jClazz?.isArray ?: false
    }

/**
 * 当前KAnnotatedElement是否是一个Map
 */
val KAnnotatedElement?.isMap: Boolean
    get() {
        this ?: return false
        val clazz = getReturnKClass()

        clazz ?: return false

        return this == Map::class || this == HashMap::class || this == LinkedHashMap::class
    }


///**当前class是否是一个Map*/
//val KClass<*>?.isMap: Boolean
//    get() {
//        this ?: return false
//        return this == Map::class || this == HashMap::class || this == LinkedHashMap::class
//    }

/**
 * 当前类型是否是java的基本类型，包括包装的对象类型和基本类型，例如Boolean和boolean
 *
 * 这里的基本类型不包含Void类型
 */
val KAnnotatedElement?.isPrimitive: Boolean
    get() {
        this ?: return false

        val clazz = getReturnKClass()

        val primitiveType = clazz?.javaPrimitiveType
        return primitiveType != null && primitiveType != Void.TYPE
    }


/**
 * 当前属性类型是否是java的基本类型，包括包装的对象类型和基本类型，例如Boolean和boolean
 *
 * 这里的基本类型不包含Void类型
 */
val KAnnotatedElement?.javaPrimitive: Class<*>?
    get() {
        this ?: return null

        val clazz = getReturnKClass()

        return clazz?.javaPrimitiveType
    }

/**
 * 获取当前KParameter的类型class，例如：val a = 1。他的class应该为Int
 */
val KParameter?.clazz: KClass<*>?
    get() {
        this ?: return null
        return type.classifier as? KClass<*>
    }

/**
 * 获取当前KProperty的类型class，例如：val a = 1。他的class应该为Int
 */
val KProperty<*>?.clazz: KClass<*>?
    get() {
        this ?: return null
        return this.getReturnKClass()
    }

/**
 * 获取包含一个泛型的lambda传参中的泛型KType信息
 *
 * 例如：
 *
 * ``` kotlin
 * test<String> {
 * }
 *
 * fun <T> test(lambda: (T)->Unit){
 *      val paramType = lambda.reflect1Type() // paramType == KType<String>
 * }
 * ```
 *
 */
fun <R> Function<R>?.reflect1Type(): KType? {
    this ?: return null
    try {
        val parameters = reflect()?.parameters
        if (parameters.isNullOrEmpty()) return null
        if (parameters.size > 1) {
            throwOnDebug(ReflectException())
            return null
        }
        return parameters[0].type
    } catch (t: Throwable) {
        throwOnDebug(ReflectException(cause = t))
        t.printStackTrace()
    }
    return null
}

/**
 * 获取包含一个泛型的lambda传参中的泛型KClass信息
 *
 * 例如：
 *
 * ``` kotlin
 * test<String> {
 * }
 *
 * fun <T> test(lambda: (T)->Unit){
 *      val paramClass = lambda.reflect1Class() // paramClass == String::class
 * }
 * ```
 *
 */
fun <R : Any> Function<R>?.reflect1Class(): KClass<R>? {
    val reflect1Type = reflect1Type() ?: return null

    try {
        return reflect1Type.classifier as? KClass<R>
    } catch (t: Throwable) {
        throwOnDebug(ReflectException(cause = t))
        t.printStackTrace()
    }
    return null
}

@Suppress("UNCHECKED_CAST")
fun <T> Any?.toT(): T? {
    this ?: return null
    return this as? T
}

/**从一个对象复制相同属性名称的内容到另一个对象，忽略错误*/
fun Any?.copyFrom(from: Any?) {
    this ?: return
    from ?: return
    val fromProp = from::class
            .declaredMemberProperties
    this::class.declaredMemberProperties.forEach { item ->
        item as? KMutableProperty1<Any, Any?> ?: return@forEach
        val find: KProperty1<Any, Any?> = fromProp.find { item.name == it.name }.toT()
                ?: return@forEach
        try {
            val fromValue = find.get(from)
            item.set(this, fromValue)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

/**
 * 获取匿名内部类的外部引用。一般情况下用于获取lambda表达式的外部实例引用。
 * 在kotlin中，如果lambda表达式，没有直接使用外部类的内容，则lambda中不会持有其引用。
 * 例如：
 * ``` kotlin
 * class TestOutClassReference{
 *      fun testLambda(lambda: ()->Unit){
 *
 *      }
 * }
 *
 * class A{
 *      fun test(){
 *          val testOutClass = TestOutClassReference()
 *          testOutClass.testLambda{
 *          }
 *      }
 * }
 * class B{
 *      var value = 3
 *      fun test(){
 *          val testOutClass = TestOutClassReference()
 *          testOutClass.testLambda{
 *              value = 4
 *          }
 *      }
 * }
 * ```
 * 在上面的例子中：
 * A中的调用，testLambda方法中，没有类A的外部引用，只有匿名内部类的实例引用：INSTANCE。使用此方法获取时，返回值为null。
 * B中的调用，由于在lambda中重新赋值了B.value参数值，所以在testLambda中的入参lambda会持有外部类B的实例引用，可以通过此方法获取到类B对象。
 *
 */
fun <T> Any?.getOutClassInstance(): T? {
    this ?: return null
    val instance = this
    val java = instance::class.java
    if (!java.isAnonymousClass) {
        return null
    }
    val outClassField = getOutClassField(instance) ?: return null
    val outClazz = outClassField::class.java
    if (outClazz.isAnonymousClass) {
        return outClassField.getOutClassInstance()
    }
    return outClassField as? T
}

inline fun <reified T : Any> Any?.getClassDelegate(targetKClass: KClass<*>? = null): T? {
    this ?: return null
    val kClass = targetKClass ?: this::class
    val find = kClass.java.declaredFields.find { it.type == T::class.java } ?: return null

    return find.get(this).toT()
}


private fun getOutClassField(target: Any, nameField: String? = null, filedClass: Class<*>? = null): Any? {
    val name = nameField ?: "this$0"
    val classCache = filedClass ?: target.javaClass

    try {
        //如果这里找不到这个名称的field，一般情况下就是没有外部引用
        val field = classCache.getDeclaredField(name)

        field.isAccessible = true

        if (field.isSynthetic && field.modifiers and Modifier.FINAL != 0) {
            return field.get(target)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return getOutClassField(target, "$name$", classCache)
}

private fun KAnnotatedElement?.getReturnKClass(): KClass<*>? {
    return when (this) {
        is KProperty<*> -> {
            returnType.classifier.toT<KClass<*>>()
        }
        is KParameter -> {
            val clazz = this.type.classifier.toT<KClass<*>>()
            clazz
        }
        is KClass<*> -> {
            this
        }
        else -> null
    }
}
