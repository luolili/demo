package com.luo.core;

import com.luo.lang.Nullable;
import com.luo.util.ConcurrentReferenceHashMap;
import com.luo.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 封装java反射中的Type
 */
@SuppressWarnings("serial")
public class ResolvableType implements Serializable {

    public static final ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE, null,
            null, 0);

    private static final ConcurrentReferenceHashMap<ResolvableType, ResolvableType> cache =
            new ConcurrentReferenceHashMap<>(256);

    @Nullable
    private final SerializableTypeWrapper.TypeProvider typeProvider;

    //将要被解析的类型
    private final Type type;

    @Nullable
    private final VariableResolver variableResolver;

    @Nullable
    private final ResolvableType componentType;

    @Nullable
    private final Integer hash;
    @Nullable
    private Class<?> resolved;

    @Nullable
    private volatile ResolvableType superType;

    @Nullable
    private volatile ResolvableType[] interfaces;

    @Nullable
    private volatile ResolvableType[] generics;


    //构造方法:hash有默认的方法计算
    public ResolvableType(Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {
        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = calculateHashCode();
        this.resolved = null;

    }

    //自定义hash ，resolved
    public ResolvableType(Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider,
                          @Nullable VariableResolver variableResolver, @Nullable Integer hash) {
        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = hash;
        this.resolved = null;

    }

    private ResolvableType(Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider,
                           @Nullable VariableResolver variableResolver, @Nullable ResolvableType componentType) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = componentType;
        this.hash = null;
        this.resolved = resolveClass();
    }

    private Class<?> resolveClass() {
        if (this.type == EmptyType.INSTANCE) {
            return null;
        }

        if (this.type instanceof Class) {
            return (Class<?>) this.type;
        }


        if (this.type instanceof GenericArrayType) {

        }
    }

    public ResolvableType getComponentType() {
        if (this == NONE) {
            return NONE;
        }

        if (this.componentType != null) {
            return this.componentType;
        }

        if (this.type instanceof Class) {
            Class<?> componentType = ((Class<?>) this.type).getComponentType();


        }
    }

    static ResolvableType forType(
            @Nullable Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

        if (type == null && typeProvider != null) {
            type = SerializableTypeWrapper.forTypeProvider(typeProvider);
        }
        if (type == null) {
            return NONE;
        }

        if (type instanceof Class) {
            return new ResolvableType((Class<?>) type, typeProvider, variableResolver, (ResolvableType) null);
        }

        cache.purgeUnreferenceEntries();
        ResolvableType resultType = new ResolvableType(type, typeProvider, variableResolver);

        ResolvableType cachedType = cache.get(resultType);
        if (cachedType == null) {
            cachedType = new ResolvableType(type, typeProvider, variableResolver, resultType.hash);
            cache.put(cachedType, cachedType);
        }
        resultType.resolved = cachedType.resolved;
        return resultType;

    }
    private int calculateHashCode() {
        int hashcode = ObjectUtils.nullSafeHashCode(this.type);
        //考虑typeProvider获得的type的hashcode
        if (this.typeProvider != null) {
            hashcode = 31 * hashcode + ObjectUtils.nullSafeHashCode(this.typeProvider.getType());
        }

        if (this.variableResolver != null) {
            hashcode = 31 * hashcode + ObjectUtils.nullSafeHashCode(this.variableResolver.getSource());
        }
        //本类的hashcode
        if (this.componentType != null) {
            hashcode = 31 * hashcode + ObjectUtils.nullSafeHashCode(this.componentType);
        }
        return hashcode;


    }

    //实现了java 反射里面的Type接口，这里没有实现里面的方法
    @SuppressWarnings("serial")
    static class EmptyType implements Type, Serializable {
        static final Type INSTANCE = new EmptyType();
        Object readResolve() {
            return INSTANCE;
        }
    }

    //策略接口 用来解析TypeVariables
    interface VariableResolver extends Serializable {

        Object getSource();

        //接口里面直接引用包含该接口的类
        ResolvableType resolveVariable(TypeVariable<?> variable);
    }

    //VariableResolver 的默认实现
    @SuppressWarnings("serial")
    private static class TypeVariablesVariableResolver implements VariableResolver {
        private final TypeVariable<?>[] variables;
        private final ResolvableType[] generics;

        public TypeVariablesVariableResolver(TypeVariable<?>[] variables, ResolvableType[] generics) {
            this.variables = variables;
            this.generics = generics;
        }

        @Override
        public Object getSource() {
            return this.generics;
        }

        @Override
        @Nullable
        public ResolvableType resolveVariable(TypeVariable<?> variable) {
            for (int i = 0; i < this.variables.length; i++) {
                TypeVariable<?> v1 = SerializableTypeWrapper.unwrap(this.variables[i]);
                TypeVariable<?> v2 = SerializableTypeWrapper.unwrap(variable);
                if (ObjectUtils.nullSafeEquals(v1, v2)) {
                    return this.generics[i];
                }
            }
            return null;
        }
    }


}
