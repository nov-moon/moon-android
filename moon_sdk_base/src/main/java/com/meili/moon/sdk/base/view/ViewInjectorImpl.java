package com.meili.moon.sdk.base.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.base.IViewInjector;
import com.meili.moon.sdk.base.view.annotation.LayoutContentId;
import com.meili.moon.sdk.base.view.annotation.OnClick;
import com.meili.moon.sdk.base.view.annotation.ViewInject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imuto on 16/5/13.
 */
public enum  ViewInjectorImpl implements IViewInjector {

    INSTANCE;

    private static final List<Class> IGNORES = new ArrayList<>();
    private static final List<String> IGNORES_PACKAGE_START = new ArrayList<>();

    static {
        IGNORES.add(Object.class);
//        IGNORES.add(SdkFragment.class);
        IGNORES_PACKAGE_START.add("android.");
    }

    @Override
    public void inject(View handler) {
        ViewFinder viewFinder = new ViewFinder(handler);
        injectFields(handler, viewFinder, handler.getClass());
        injectMethod(handler, viewFinder, handler.getClass());
    }

    @Override
    public void inject(Activity handler) {
        Class<?> handlerType = handler.getClass();
        int layoutId = initContentViewId(handlerType);
        if (layoutId > 0) {
            try {
                Method setContentView = handlerType.getMethod("setContentView", int.class);
                setContentView.invoke(handler, layoutId);
                ViewFinder viewFinder = new ViewFinder(handler);
                injectFields(handler, viewFinder, handler.getClass());
                injectMethod(handler, viewFinder, handler.getClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void inject(Object handle, View from) {
        ViewFinder viewFinder = new ViewFinder(from);
        injectFields(handle, viewFinder, handle.getClass());
        injectMethod(handle, viewFinder, handle.getClass());
    }

    @Override
    public View inject(Object handler, LayoutInflater inflater, ViewGroup container) {
        return inject(handler, inflater, container, false);
    }

    @Override
    public View inject(Object handler, LayoutInflater inflater, ViewGroup container, boolean isAttach) {
        View view = null;
        int layoutId = initContentViewId(handler.getClass());
        if (layoutId > 0) {
            view = inflater.inflate(layoutId, container, isAttach);
            ViewFinder viewFinder = new ViewFinder(view);
            injectFields(handler, viewFinder, handler.getClass());
            injectMethod(handler, viewFinder, handler.getClass());
        }
        return view;
    }

    private void injectFields(Object handle, ViewFinder viewFinder, Class<?> handlerType) {

        if (isIgnoreClass(handlerType) || handle == null || viewFinder == null) {
            return;
        }

        //先初始化父类的变量
        injectFields(handle, viewFinder, handlerType.getSuperclass());

        Field[] fields = handlerType.getDeclaredFields(); // 获取字段
        if (fields != null) {
            try {
                for (Field field : fields) {
                    Class<?> fieldType = field.getType();
                    if (
                    /* 不注入静态字段 */     Modifier.isStatic(field.getModifiers()) ||
                    /* 不注入final字段 */    Modifier.isFinal(field.getModifiers()) ||
                    /* 不注入基本类型字段 */  fieldType.isPrimitive() ||
                    /* 不注入枚举类型字段 */  fieldType.isEnum() ||
                    /* 不注入数组类型字段 */  fieldType.isArray() ||
                    /* 不注入String字段 */  fieldType == String.class) {
                        continue;
                    }
                    ViewInject viewInject = field.getAnnotation(ViewInject.class);
                    if (viewInject != null) {
                        int viewId = viewInject.value();
                        field.setAccessible(true); // 设为可访问
                        field.set(handle, viewFinder.find(viewId));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void injectMethod(Object handle, ViewFinder finder, Class<?> handlerType) {
        if (isIgnoreClass(handlerType) || handle == null || finder == null) {
            return;
        }

        //先初始化父类的方法
        injectMethod(handle, finder, handlerType.getSuperclass());

        Method[] methods = handlerType.getDeclaredMethods();
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {

                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                //检查当前方法是否是event注解的方法
                OnClick event = method.getAnnotation(OnClick.class);
                if (event != null) {
                    try {
                        // id参数
                        int[] values = event.value();
                        int[] parentIds = event.parentId();
                        int parentIdsLen = parentIds == null ? 0 : parentIds.length;
                        //循环所有id，生成ViewInfo并添加代理反射
                        for (int i = 0; i < values.length; i++) {
                            int value = values[i];
                            if (value > 0) {
                                ViewInfo info = new ViewInfo();
                                info.value = value;
                                info.parentId = parentIdsLen > i ? parentIds[i] : 0;
                                method.setAccessible(true);
                                EventListenerManager.addEventMethod(finder, info, event, handle, method);
                            }
                        }
                    } catch (Throwable ex) {
                    }
                }
            }
        }
    }

    private int initContentViewId(Class<?> clazz) {
        LayoutContentId layoutContentId = findLayoutContentId(clazz);
        if (layoutContentId != null) {
            return layoutContentId.value();
        }
        return 0;
    }

    private LayoutContentId findLayoutContentId(Class cls) {
        if (isIgnoreClass(cls)) {
            return null;
        }
        if (cls.isAnnotationPresent(LayoutContentId.class)) {
            return (LayoutContentId) cls.getAnnotation(LayoutContentId.class);
        }
        return findLayoutContentId(cls.getSuperclass());
    }

    private boolean isIgnoreClass(Class cls) {
        if (cls == null || IGNORES.contains(cls)) {
            return true;
        }
        for (String match : IGNORES_PACKAGE_START) {
            if (cls.getName().startsWith(match)) {
                return true;
            }
        }
        return false;
    }
}
