package com.luo.core;

import com.luo.lang.Nullable;
import com.luo.util.ConcurrentReferenceHashMap;
import com.luo.util.ObjectUtils;
import com.luo.util.ReflectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.*;

final class SerializableTypeWrapper {

    private static final Class<?>[] SUPPORTED_SERIALIZABLE_TYPES = {
            GenericArrayType.class, ParameterizedType.class, TypeVariable.class, WildcardType.class
    };

    //use ConcurrentReferenceHashMap to hold the type
    static final ConcurrentReferenceHashMap<Type, Type> cache = new ConcurrentReferenceHashMap<>(256);

    //私有化构造方法
    private SerializableTypeWrapper() {

    }

    static Type forTypeProvider(TypeProvider provider) {
        Type providedType = provider.getType();
        if (providedType == null || providedType instanceof Serializable) {
            return providedType;

        }

        if (GraalDetector.inImageCode() || !Serializable.class.isAssignableFrom(Class.class)) {
            return providedType;
        }

        //get a serializable type for the provider
        Type cached = cache.get(providedType);
        if (cached != null) {
            return cached;
        }

        for (Class<?> type : SUPPORTED_SERIALIZABLE_TYPES) {

            if (type.isInstance(providedType)) {
                ClassLoader classLoader = providedType.getClass().getClassLoader();
                Class<?>[] interfaces = {type, SerializableTypeProxy.class, Serializable.class};

            }

        }

        return null;
    }

    @Nullable
    public static Type forMethodParameter(MethodParameter methodParameter) {
        return forTypeProvider(new MethodParameterTypeProvider(methodParameter));
    }

    @SuppressWarnings("serial")
    static class MethodParameterTypeProvider implements TypeProvider {
        //attr
        private final String methdName;
        private final Class<?>[] parameterTypes;
        private final Class<?> declaringClass;
        private final int parameterIndex;
        private transient MethodParameter methodParameter;

        //通过methodParameter 来构造MethodParameterTypeProvider
        //从通过methodParameter可以获取：Method, parameterTypes, declaringClass, parameterIndex
        public MethodParameterTypeProvider(MethodParameter methodParameter) {
            this.methdName = (methodParameter.getMethod() != null ? methodParameter.getMethod().getName() : null);
            this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
            this.declaringClass = methodParameter.getDeclaringClass();
            this.parameterIndex = methodParameter.getParameterIndex();
            this.methodParameter = methodParameter;
        }

        @Override
        public Type getType() {
            return this.methodParameter.getGenericParameterType();
        }

        @Override
        public Object getSource() {
            return this.methodParameter;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            try {
                //分为普通方法和构造方法
                if (methdName != null) {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredMethod(
                            this.methdName, this.parameterTypes
                    ), this.parameterIndex);

                } else {
                    this.methodParameter = new MethodParameter(this.declaringClass.getDeclaredConstructor(this.parameterTypes),
                            this.parameterIndex);
                }
            } catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure", ex);
            }


        }
    }

    @SuppressWarnings("serial")
    static class MethodInvokeTypeProvider implements TypeProvider {

        private final TypeProvider provider;

        private final String methodName;
        private final Class<?> declaringClass;
        private final int index;
        private transient Method method;
        @Nullable
        private transient volatile Object result;

        public MethodInvokeTypeProvider(TypeProvider provider, Method method, int index) {
            this.provider = provider;
            this.methodName = method.getName();
            this.declaringClass = method.getDeclaringClass();
            this.index = index;
            this.method = method;
        }


        @Override
        public Type getType() {
            Object result = this.result;
            if (result == null) {
                //在给出的类型上调用给出的方法
                result = ReflectionUtils.invokeMethod(this.method, this.provider.getType());
                this.result = result;
            }
            return (result instanceof Type[] ? ((Type[]) result)[this.index] : (Type) result);
        }

        @Override
        @Nullable
        public Object getSource() {
            return null;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            Method method = ReflectionUtils.findMethod(this.declaringClass, this.methodName);
            if (method == null) {
                throw new IllegalStateException("Cannot find method on deserialization: " + this.methodName);
            }

            if (method.getReturnType() != Type.class && method.getReturnType() != Type[].class) {
                throw new IllegalStateException(
                        "Invalid return type on deserialized method - needs to be Type or Type[]: " + method);
            }
            this.method = method;
        }
    }
    @SuppressWarnings("serial")
    private class TypeProxyInvocationHandler implements InvocationHandler, Serializable {
        private final TypeProvider provider;

        public TypeProxyInvocationHandler(TypeProvider provider) {
            this.provider = provider;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("equals") && args != null) {
                Object other = args[0];
                if (other instanceof Type) {
                    other = unwrap((Type) other);

                }

                return ObjectUtils.nullSafeEquals(this.provider.getType(), other);//boolean也是一个对象
            } else if (method.getName().equals("hashCode")) {
                return ObjectUtils.nullSafeHashCode(this.provider.getType());
            }
            return null;
        }
    }

    //返回最初的非可序列化的类型：unwrap the given type
    public static <T extends Type> T unwrap(T type) {
        Type unwrapped = type;
        //通过TypeProvider的代理获得TypeProvider，进而获得type
        while (unwrapped instanceof SerializableTypeProxy) {
            unwrapped = ((SerializableTypeProxy) unwrapped).getTypeProvider().getType();
        }

        return (unwrapped != null ? (T) unwrapped : type);
    }
    //FieldTypeProvider实现
    @SuppressWarnings("serial")//序列化和反序列化的时候
    static class FieldTypeProvider implements TypeProvider {

        private final String fieldName;//字段的名字
        private final Class<?> declaringClass;//字段所在的类
        private transient Field field;

        public FieldTypeProvider(Field field) {
            this.fieldName = field.getName();
            this.declaringClass = field.getDeclaringClass();
            this.field = field;
        }

        @Override
        public Type getType() {
            return this.field.getGenericType();
        }

        @Override
        public Object getSource() {
            return this.field;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException, NoSuchFieldException {
            inputStream.defaultReadObject();
            //通过字段的名字获取字段对象
            try {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure", ex);
            }
        }
    }

    //类型的提供人
    @SuppressWarnings("serial")
    interface TypeProvider extends Serializable {
        @Nullable
        Type getType();
        //return the source of the type
        @Nullable
        default Object getSource() {
            return null;
        }
    }

    //type proxy:提供获取TypeProvider的方法
    interface SerializableTypeProxy {
        TypeProvider getTypeProvider();
    }
}
