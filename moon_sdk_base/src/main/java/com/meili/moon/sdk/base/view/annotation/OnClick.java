package com.meili.moon.sdk.base.view.annotation;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件注解.
 * <p/>
 * 被注解的方法必须具备以下形式:
 * 1. private 修饰
 * 2. 返回值类型没有要求
 * 3. 参数签名和type的接口要求的参数签名一致.
 * <p/>
 */
@Target(ElementType.METHOD)
// 表示用在方法上
@Retention(RetentionPolicy.RUNTIME)
// 表示在生命周期是运行时
public @interface OnClick {
	/**
	 * 控件的id集合, id<1时不执行ui事件绑定.
	 *
	 * @return
	 */
	int[] value();

	/**
	 * 控件的parent控件的id集合, 组合为(value[i], parentId[i] or 0).
	 *
	 * @return
	 */
	int[] parentId() default 0;

	/**
	 * 事件的listener, 默认为点击事件.
	 *
	 * @return
	 */
	Class<?> type() default View.OnClickListener.class;

	/**
	 * 事件的setter方法名, 默认为set+type#simpleName.
	 *
	 * @return
	 */
	String setter() default "";

	/**
	 * 如果type的接口类型提供多个方法, 需要使用此参数指定方法名.
	 *
	 * @return
	 */
	String method() default "";

	/**
	 * 点击事件的最小间隔时间，单位：毫秒
	 * @return 默认500毫秒
	 */
	long minClickSpace() default 500;
}