package com.meili.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置PageConfig的命名等内容，一般设置在Application类上或者某个入口类上，全局设置一次即可
 * <p>
 * Author： fanyafeng
 * Date： 2019/2/27 11:27 AM
 * Email: fanyafeng@live.cn
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface PageConfigParams {
    /**
     * 设置生成的的pageConfig的名称前缀
     * <p>
     * 在library项目中必须设置，并且前缀必须唯一，重名的话在多module情况下，会有PageConfig覆盖的情况
     * <p>
     * 一般使用当前项目名称，例如：设置为Login，则最后生成的为LoginPageConfig。注意首字母大写
     * <p>
     * 在Application项目中可以不设置，默认使用PageConfig
     */
    String pageConfigPrefixName() default "";

//    /**
//     * 生成的pageConfig的package
//     * <p>
//     * 默认情况不使用任何的package
//     */
//    String pageConfigPackage() default "";

//    /**
//     * 是否自动将收集到的内容注册到Rainbow中，默认开启。
//     * <p>
//     * 如果你有特殊的业务需求，需要自行控制注册方式和流程，可以设置为false。但是请注意，如果设置为false，
//     * 则你必须自己进行注册页面操作，否则不能跳转页面
//     */
//    boolean autoRegister() default true;
}
