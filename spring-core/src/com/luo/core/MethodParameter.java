package com.luo.core;

import com.luo.lang.Nullable;
import com.luo.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

/**
 * helper class for the method parameters
 */
public class MethodParameter {
    //attr
    private static final Annotation[] empty_annotation_array = new Annotation[0];


    private final Executable executable;//from java.reflect

    private final int parameterIndex;

    //volatile 修饰的变量 不需要立即初始化
    @Nullable
    private volatile Parameter parameter;

    private int nestingLevel = 1;

    @Nullable
    private Map<Integer, Integer> typeIndexesPerLevel;

    @Nullable
    private volatile Class<?> containingClass;
    @Nullable
    private volatile Class<?> parameterType;
    @Nullable
    private volatile Type genericParameterType;
    //参数上的注解数组
    @Nullable
    private volatile Annotation[] parameterAnnotations;
    @Nullable
    private volatile ParameterNameDiscover parameterNameDiscover;

    //参数的名称
    @Nullable
    private volatile String parameterName;

    @Nullable
    private volatile MethodParameter nestedMethodParameter;

    //构造方法

    public MethodParameter(MethodParameter original) {
        Assert.notNull(original, "Original must not be null");
        this.executable = original.executable;

        //参数名，参数对象，参数的索引
        this.parameterIndex = original.parameterIndex;
        this.parameter = original.parameter;
        this.parameterName = original.parameterName;

        //参数类型，通用参数类型，
        this.parameterType = original.parameterType;
        this.genericParameterType = original.genericParameterType;
        //参数上的注解
        this.parameterAnnotations = original.parameterAnnotations;

        this.containingClass = original.containingClass;
        this.nestingLevel = original.nestingLevel;
        this.typeIndexesPerLevel = original.typeIndexesPerLevel;

        this.parameterNameDiscover = original.parameterNameDiscover;

    }

    //-----参数是方法对象  exectable代表了方法对象或构造对象
    public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
        Assert.notNull(method, "Method must not be null");
        this.executable = method;
        this.parameterIndex = validateIndex(method, parameterIndex);
        this.nestingLevel = nestingLevel;
    }

    //默认参数的内嵌等级为1
    public MethodParameter(Method method, int parameterIndex) {
        this(method, parameterIndex, 1);
    }
    //----参数是构造对象

    /**
     * @param ctor           constructor to specify a parameter for
     * @param parameterIndex
     * @param nestingLevel   a List of Lists: its nesting level is 1
     */
    public MethodParameter(Constructor<?> ctor, int parameterIndex, int nestingLevel) {
        Assert.notNull(ctor, "Constructor must not be null");
        this.executable = ctor;
        this.parameterIndex = validateIndex(ctor, parameterIndex);
        this.nestingLevel = nestingLevel;
    }

    //默认参数的内嵌等级为1
    public MethodParameter(Constructor<?> ctor, int parameterIndex) {
        this(ctor, parameterIndex, 1);
    }

    //对参数索引的范围检查
    private static int validateIndex(Executable executable, int parameterIndex) {
        //-1 获取参数的个数
        int count = executable.getParameterCount();
        Assert.isTrue((parameterIndex >= -1 && parameterIndex < count),
                () -> "Parameter index needs to be between -1 and  " + (count - 1));
        return parameterIndex;

    }

    //通过exectable来获取方法对象
    @Nullable
    public Method getMethod() {
        return (this.executable instanceof Method ? (Method) this.executable : null);
    }

    @Nullable
    public Constructor<?> getConstructor() {
        return (this.executable instanceof Constructor ? (Constructor<?>) this.executable : null);
    }

    public Class<?> getDeclaringClass() {
        return this.executable.getDeclaringClass();
    }
}