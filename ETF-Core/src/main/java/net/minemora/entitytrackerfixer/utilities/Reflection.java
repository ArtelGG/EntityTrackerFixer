package net.minemora.entitytrackerfixer.utilities;

import net.minemora.entitytrackerfixer.nms.NMS;
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

    public Field getClassPrivateField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Method getPrivateMethod(Class<?> clazz, String methodName, Class[] params) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public NMS getNMS() {
        try {
            final Class<?> clazz = Class.forName("net.minemora.entitytrackerfixer.tasks." + getServerVersion() + ".Tasks");
            if (NMS.class.isAssignableFrom(clazz)) {
                return (NMS) clazz.getConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}