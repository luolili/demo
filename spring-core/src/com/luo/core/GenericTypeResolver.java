package com.luo.core;

import com.luo.util.Assert;
import com.luo.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

/**
 * 解析通用类型的帮助类
 */
public final class GenericTypeResolver {

    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, Map<TypeVariable, Type>> typeVariableCache = new ConcurrentReferenceHashMap<>();

    private GenericTypeResolver() {

    }

    public static Class<?> resolveParameterType(MethodParameter methodParameter, Class<?> implementationClass) {
        Assert.notNull(methodParameter, "MethodParameter must not be null");
        Assert.notNull(implementationClass, "Class must not be null");
        methodParameter.setContainingClass(implementationClass);
        ResolvableType.resolveMethodParameter(methodParameter);
        return methodParameter.getParameterType();

    }
}