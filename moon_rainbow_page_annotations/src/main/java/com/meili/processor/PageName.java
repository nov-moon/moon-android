package com.meili.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个页面的配置信息
 * <p>
 * Author： fanyafeng
 * Date： 2019/2/26 4:32 PM
 * Email: fanyafeng@live.cn
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface PageName {

    /**
     * 当前页面的PageName
     */
    String value() default "";

    /**
     * 页面的PageName
     */
    @Deprecated
    String name() default "";

    /**
     * 页面的简介信息
     */
    String note() default "";

    /**
     * 当前页面的分组信息
     */
    String affinity() default "";

    /**
     * 配置页面拦截器
     */
    Class<?>[] interceptors() default Object.class;
}
