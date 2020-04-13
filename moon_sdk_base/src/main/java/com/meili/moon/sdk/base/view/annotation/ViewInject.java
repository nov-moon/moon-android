package com.meili.moon.sdk.base.view.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * view inect by id
 * 
 * @author andyhome
 * 页面控件的注入
 */
@Target(ElementType.FIELD)
// 表示用在字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewInject {
	int value();
}