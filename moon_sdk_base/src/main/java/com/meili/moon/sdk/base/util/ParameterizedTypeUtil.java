package com.meili.moon.sdk.base.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ParameterizedTypeUtil {

    private ParameterizedTypeUtil() {
    }

    /** 获取指定clazz上定义的泛型类型 */
    public static Type getDirectParameterizedType(Class clazz) {
        ParameterizedType p = findParameterizedType(clazz.getGenericSuperclass());
        return p.getActualTypeArguments()[0];
    }

    private static ParameterizedType findParameterizedType(Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        }
        Type genericSuperclass = ((Class<?>) type).getGenericSuperclass();
        return findParameterizedType(genericSuperclass);
    }

    /**
     * 获取泛型参数
     *
     * @param ownerType     当前类型
     * @param declaredClass 最终的父类型
     * @param paramIndex    参数位与参数列表的第几个
     * @return 真实的参数类型
     */
    public static Type getParameterizedType(Type ownerType, Class<?> declaredClass, int paramIndex) {

        //需要处理的class
        Class<?> clazz;
        ParameterizedType pt;
        Type[] ats = null;
        TypeVariable<?>[] tps = null;

        if (ownerType instanceof ParameterizedType) {
            pt = (ParameterizedType) ownerType;
            clazz = (Class<?>) pt.getRawType();
            ats = pt.getActualTypeArguments();
            tps = clazz.getTypeParameters();
        } else {
            clazz = (Class<?>) ownerType;
        }
        if (declaredClass == clazz) {
            if (ats != null) {
                return ats[paramIndex];
            }
            return Object.class;
        }

        //获取clazz的实现接口
        Type[] types = clazz.getGenericInterfaces();
        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                Type t = types[i];
                if (t instanceof ParameterizedType) {
                    Class<?> cls = (Class<?>) ((ParameterizedType) t).getRawType();//获取真实的类
                    if (declaredClass.isAssignableFrom(cls)) {
                        try {
                            return getTrueType(getParameterizedType(t, declaredClass, paramIndex), tps, ats);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            if (declaredClass.isAssignableFrom(superClass)) {
                return getTrueType(
                        getParameterizedType(clazz.getGenericSuperclass(),
                                declaredClass, paramIndex), tps, ats);
            }
        }

        throw new IllegalArgumentException("FindGenericType:" + ownerType +
                ", declaredClass: " + declaredClass + ", index: " + paramIndex);

    }


    private static Type getTrueType(Type type, TypeVariable<?>[] typeVariables, Type[] actualTypes) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            String name = tv.getName();
            if (actualTypes != null) {
                for (int i = 0; i < typeVariables.length; i++) {
                    if (name.equals(typeVariables[i].getName())) {
                        return actualTypes[i];
                    }
                }
            }
            return tv;
            // }else if (type instanceof Class<?>) {
            // return type;
        } else if (type instanceof GenericArrayType) {
            Type ct = ((GenericArrayType) type).getGenericComponentType();
            if (ct instanceof Class<?>) {
                return Array.newInstance((Class<?>) ct, 0).getClass();
            }
        }
        return type;
    }

}
