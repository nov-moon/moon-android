package com.meili.moon.sdk.base;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 注解view的id，layout，click事件等
 * Created by imuto on 16/5/13.
 */
public interface IViewInjector {

    /**
     * 注解view中的属性
     */
    void inject(View v);

    /**
     * 注解activity的contentView和属性
     */
    void inject(Activity activity);

    /**
     * 使用指定view初始化handle中的对应注解属性
     */
    void inject(Object handle, View v);

    /**
     * 注解当前类到handle的属性中，默认不将生成的view添加到container中,
     * <p>如果这时候注解的layout是merge标签开头的，那么会报错，
     * 需要使用{@link #inject(Object, LayoutInflater, ViewGroup, boolean)}方法设置attach为true</p>
     *
     * @param handle    要初始化属性的类
     * @param inflater  没什么好说的
     * @param container 要绑定的container
     * @return 初始化成功的类
     */
    View inject(Object handle, LayoutInflater inflater, ViewGroup container);

    /**
     * 注解当前类到handle的属性中
     *
     * @param handle    要初始化属性的类
     * @param inflater  没什么好说的
     * @param container 要绑定的container
     * @param isAttach  是否将当前类初始化出来的view绑定到container中
     * @return 初始化成功的类
     */
    View inject(Object handle, LayoutInflater inflater, ViewGroup container, boolean isAttach);
}
