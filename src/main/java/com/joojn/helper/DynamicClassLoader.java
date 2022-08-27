package com.joojn.helper;

public class DynamicClassLoader extends ClassLoader{

    private DynamicClassLoader(){}

    public static DynamicClassLoader instance = new DynamicClassLoader();

    public Class<?> defineClass(String name, byte[] b)
    {
        return defineClass(name, b, 0, b.length);
    }

}
