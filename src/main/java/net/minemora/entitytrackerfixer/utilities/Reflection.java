package net.minemora.entitytrackerfixer.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Reflection {

    public static Object getPrivateField(Class<?> clazz, Object obj, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object ret = field.get(obj);
        field.setAccessible(false);
        return ret;
    }

    public static Field getClassPrivateField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return invokePrivateMethod(clazz, obj, methodName, new Class[0]);
    }

    public static Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName, Class[] params, Object... args)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = getPrivateMethod(clazz, methodName, params);
        return method.invoke(obj, args);
    }

    public static Method getPrivateMethod(Class<?> clazz, String methodName, Class[] params)
            throws NoSuchMethodException, SecurityException {
        Method method = clazz.getDeclaredMethod(methodName, params);
        method.setAccessible(true);
        return method;
    }
}