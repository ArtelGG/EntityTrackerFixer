package net.minemora.entitytrackerfixer.utilities;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Reflection {
    private final String name = Bukkit.getServer().getClass().getPackage().getName();
    private final String version = name.substring(name.lastIndexOf('.') + 1);
    private Object serverInstance;
    private Field tpsField;

    public Reflection() {
        try {
            serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
            tpsField = serverInstance.getClass().getField("recentTps");
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Reflection getInstance() {
        return new Reflection();
    }

    public Object getPrivateField(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object ret = field.get(obj);
        field.setAccessible(false);
        return ret;
    }

    public Field getClassPrivateField(Class<?> clazz, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return invokePrivateMethod(clazz, obj, methodName, new Class[0]);
    }

    public Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName, Class[] params, Object... args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = getPrivateMethod(clazz, methodName, params);
        return method.invoke(obj, args);
    }

    public Method getPrivateMethod(Class<?> clazz, String methodName, Class[] params) throws NoSuchMethodException, SecurityException {
        Method method = clazz.getDeclaredMethod(methodName, params);
        method.setAccessible(true);
        return method;
    }

    public String getServerVersion() {
        return version;
    }

    private Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public double getTPS(int time) {
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return tps[time];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}