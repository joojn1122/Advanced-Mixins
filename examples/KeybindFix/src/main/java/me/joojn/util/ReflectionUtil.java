package me.joojn.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    public static Class<?> loadClass(String s)
    {
        try
        {
            return ReflectionUtil.class.getClassLoader().loadClass(s);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... args)
    {
        try
        {
            return clazz.getDeclaredMethod(name, args);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object invoke(Method method, Object instance, Object... args)
    {
        try
        {
            return method.invoke(instance, args);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String name)
    {
        try
        {
            return clazz.getDeclaredField(name);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void setField(Field field, Object instance, Object value)
    {
        if(!field.isAccessible())
            field.setAccessible(true);

        try
        {
            field.set(instance, value);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public static <T> T getFieldValue(Field field, Object instance)
    {
        if(!field.isAccessible())
            field.setAccessible(true);

        try
        {
            return (T) field.get(instance);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}

