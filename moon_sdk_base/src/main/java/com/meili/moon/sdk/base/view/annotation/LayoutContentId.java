package com.meili.moon.sdk.base.view.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * view inect by id
 * 用于 activitiy 加载对应的布局 xml
 *
 * @author andyhome
 */
@Target(ElementType.TYPE)//表示用在类上
@Retention(RetentionPolicy.RUNTIME)//表示在生命周期是运行时
public @interface LayoutContentId {
    int value();
}