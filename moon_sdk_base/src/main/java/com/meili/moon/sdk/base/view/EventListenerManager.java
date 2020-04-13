package com.meili.moon.sdk.base.view;

import android.text.TextUtils;
import android.view.View;

import com.meili.moon.sdk.base.util.DoubleKeyValueMap;
import com.meili.moon.sdk.base.view.annotation.OnClick;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*package*/ final class EventListenerManager {

    private final static HashSet<String> AVOID_QUICK_EVENT_SET = new HashSet<>(2);

    static {
        AVOID_QUICK_EVENT_SET.add("onClick");
        AVOID_QUICK_EVENT_SET.add("onItemClick");
    }

    private EventListenerManager() {
    }

    /**
     * k1: viewInjectInfo
     * k2: interface Type
     * value: listener
     */
    private final static DoubleKeyValueMap<ViewInfo, String, Object>
            listenerCache = new DoubleKeyValueMap<>();


    public static void addEventMethod(
            //根据页面或view holder生成的ViewFinder
            ViewFinder finder,
            //根据当前注解ID生成的ViewInfo
            ViewInfo info,
            //注解对象
            OnClick event,
            //页面或view holder对象
            Object handler,
            //当前注解方法
            Method method) {
        try {
            View view = finder.findByInfo(info);

            if (view != null) {
                // 注解中定义的接口，比如Event注解默认的接口为View.OnClickListener
                Class<?> listenerType = event.type();
                // 默认为空，注解接口对应的Set方法，比如setOnClickListener方法
                String listenerSetter = event.setter();
                if (TextUtils.isEmpty(listenerSetter)) {
                    listenerSetter = "set" + listenerType.getSimpleName();
                }


                String methodName = event.method();

                boolean addNewMethod = false;
                /*
                    根据View的ID和当前的接口类型获取已经缓存的接口实例对象，
                    比如根据View.id和View.OnClickListener.class两个键获取这个View的OnClickListener对象
                 */
                Object listener = listenerCache.get(info, listenerSetter);
                DynamicHandler dynamicHandler = null;
                /*
                    如果接口实例对象不为空
                    获取接口对象对应的动态代理对象
                    如果动态代理对象的handler和当前handler相同
                    则为动态代理对象添加代理方法
                 */
                if (listener != null) {
                    dynamicHandler = (DynamicHandler) Proxy.getInvocationHandler(listener);
                    addNewMethod = handler.equals(dynamicHandler.getHandler());
                    if (addNewMethod) {
                        dynamicHandler.addMethod(methodName, method, event);
                    }
                }

                // 如果还没有注册此代理
                if (!addNewMethod) {

                    dynamicHandler = new DynamicHandler(handler);

                    dynamicHandler.addMethod(methodName, method, event);

                    // 生成的代理对象实例，比如View.OnClickListener的实例对象
                    listener = Proxy.newProxyInstance(
                            listenerType.getClassLoader(),
                            new Class<?>[]{listenerType},
                            dynamicHandler);

                    listenerCache.put(info, listenerSetter, listener);
                }

                Method setEventListenerMethod = view.getClass().getMethod(listenerSetter, listenerType);
                setEventListenerMethod.invoke(view, listener);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static class DynamicHandler implements InvocationHandler {
        // 存放代理对象，比如Fragment或view holder
        private WeakReference<Object> handlerRef;
        // 存放代理方法
        private final HashMap<String, EventHold> methodMap = new HashMap<>(1);

        private static long lastClickTime = 0;

        public DynamicHandler(Object handler) {
            this.handlerRef = new WeakReference<>(handler);
        }

        public void addMethod(String name, Method method, OnClick event) {

            methodMap.put(name, new EventHold(method, event));
        }

        public Object getHandler() {
            return handlerRef.get();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object handler = handlerRef.get();
            if (handler != null) {

                String eventMethod = method.getName();
                if ("toString".equals(eventMethod)) {
                    return DynamicHandler.class.getSimpleName();
                }
                method = null;
                EventHold eventHold = methodMap.get(eventMethod);
                if (eventHold != null) {
                    method = eventHold.method;
                }
                if (method == null && methodMap.size() == 1) {
                    for (Map.Entry<String, EventHold> entry : methodMap.entrySet()) {
                        if (TextUtils.isEmpty(entry.getKey())) {
                            eventHold = entry.getValue();
                            method = entry.getValue().method;
                        }
                        break;
                    }
                }

                if (method != null) {
                    if (AVOID_QUICK_EVENT_SET.contains(eventMethod) && eventHold != null) {
                        long timeSpan = System.currentTimeMillis() - lastClickTime;
                        if (timeSpan < eventHold.event.minClickSpace()) {
                            if (method.getReturnType() != null && method.getReturnType().isPrimitive()) {
                                if (method.getReturnType() == boolean.class) {
                                    return false;
                                } else {
                                    return 0;
                                }
                            }
                            return null;
                        }
                        lastClickTime = System.currentTimeMillis();
                    }

                    try {
                        return method.invoke(handler, args);
                    } catch (Throwable ex) {
                        throw new RuntimeException("invoke method error:" +
                                handler.getClass().getName() + "#" + method.getName(), ex);
                    }
                } else {
                }
            }
            return null;
        }

        private class EventHold {
            Method method;
            OnClick event;

            EventHold(Method method, OnClick event) {
                this.method = method;
                this.event = event;
            }
        }
    }
}
