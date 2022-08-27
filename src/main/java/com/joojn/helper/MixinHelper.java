package com.joojn.helper;

import com.joojn.mixins.annotation.MixinMethodFor;
import com.joojn.mixins.annotation.MixinNewMethod;
import com.joojn.mixins.annotation.ShadowFor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class MixinHelper {

    public static boolean methodEquals(MethodNode node, Method method)
    {
        return node.name.equals(method.getName())
                && Type.getType(method).toString().equals(node.desc);
    }

    public static boolean isMixinMethodFor(Method method)
    {
        return method.isAnnotationPresent(MixinMethodFor.class)
                && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0].equals(ClassNode.class)
                && method.getReturnType().equals(MethodNode.class);
    }

    public static boolean isShadowFor(Method method)
    {
        return method.isAnnotationPresent(ShadowFor.class)
                && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0].equals(ClassNode.class);
    }

    public static String[] getExceptions(Class<?>[] exceptions)
    {
        if(exceptions.length == 0) return null;

        String[] strings = new String[exceptions.length];

        for(int i = 0; i < exceptions.length ; i++)
        {
            strings[i] = exceptions[i].getName().replace(".", "/");
        }

        return strings;
    }

    public static HashMap<String, MethodNode> getClassMixinForMethods(Object instance, ClassNode cn)
    {
        HashMap<String, MethodNode> map = new HashMap<>();

        for(Method method : instance.getClass().getDeclaredMethods())
        {
            if(
                    isMixinMethodFor(method)
            )
            {
                String name = method.getAnnotation(MixinMethodFor.class).name();

                try
                {
                    map.put(
                            name, (MethodNode) method.invoke(instance, cn)
                    );
                }
                catch (InvocationTargetException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return map;
    }

    public static void setFinal(Field field, Object instance, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, newValue);
    }

    public static void replaceStrings(Object object, String from, String to, boolean finals)
    {
        for(Field field : object.getClass().getFields())
        {
            try
            {
                if(Object[].class.isAssignableFrom(field.getType()))
                {
                    for(Object ob : (Object[]) field.get(object))
                    {
                        replaceStrings(ob, from, to, finals);
                    }
                }

                if(!field.getType().equals(String.class)) continue;

                if(!field.isAccessible()) field.setAccessible(true);
                String val = ((String) field.get(object)).replace(from, to);

                if(Modifier.isFinal(field.getModifiers()))
                {
                    if(!finals) continue;
                    setFinal(field, object, val);
                }
                else
                {
                    field.set(object, val);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
