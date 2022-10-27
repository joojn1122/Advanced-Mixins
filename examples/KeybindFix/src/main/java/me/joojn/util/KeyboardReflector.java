package me.joojn.util;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class KeyboardReflector {

    private static Field keyField;
    private static Field stateField;
    private static Field characterField;
    private static Field nanosField;
    private static Field repeatField;

    private static Class<?> KEYBOARD_CLASS = null;
    private static void reloadClass(Class<?> clazz)
    {
        KEYBOARD_CLASS = clazz;

        keyField =
                ReflectionUtil.getField(KEYBOARD_CLASS, "key");

        stateField =
                ReflectionUtil.getField(KEYBOARD_CLASS, "state");

        characterField =
                ReflectionUtil.getField(KEYBOARD_CLASS, "character");

        nanosField =
                ReflectionUtil.getField(KEYBOARD_CLASS, "nanos");

        repeatField =
                ReflectionUtil.getField(KEYBOARD_CLASS, "repeat");
    }

    public static void setKey(Object instance, int key)
    {
        if(KEYBOARD_CLASS == null)
        {
            reloadClass(instance.getClass());
        }

        ReflectionUtil.setField(keyField, instance, key);
    }

    public static void setState(Object instance, boolean state)
    {
        ReflectionUtil.setField(stateField, instance, state);
    }

    public static void setCharacter(Object instance, int character)
    {
        ReflectionUtil.setField(characterField, instance, character);
    }

    public static void setNanos(Object instance, long nanos)
    {
        ReflectionUtil.setField(nanosField, instance, nanos);
    }

    public static void setRepeat(Object instance, boolean repeat)
    {
        ReflectionUtil.setField(repeatField, instance, repeat);
    }

    private static Field readBufferField = null;
    public static ByteBuffer getReadBuffer(Object instance)
    {
        if(readBufferField == null)
            readBufferField = ReflectionUtil.getField(
                    instance.getClass().getDeclaringClass(), "readBuffer"
            );

        return ReflectionUtil.getFieldValue(readBufferField, null);
    }

    private static int getFixedKey(int character)
    {
        switch(character)
        {
            // 1
            case 43  :
            case 49  :
                return 2;

            // 2
            case 283 :
            case 50  :
                return 3;

                // 3
            case 353 :
            case 51  :
                return 4;

                // 4
            case 269 :
            case 52  :
                return 5;

                // 5
            case 345 :
            case 53  :
                return 6;

                // 6
            case 382 :
            case 54  :
                return 7;

                // 7
            case 253 :
            case 55  :
                return 8;

                // 8
            case 225 :
            case 56  :
                return 9;

                // 9
            case 57  :
            case 237 :
                return 10;

                // 0
            case 233 :
            case 48  :
                return 11;

            default  :
                return 0;
        }
    }

    public static boolean readNext(Object event)
    {
        ByteBuffer readBuffer = getReadBuffer(event);

        if(readBuffer.hasRemaining())
        {
            int keyBuffer = readBuffer.getInt();

            // event.key = keyBuffer & 255;
            setKey(
                    event, keyBuffer & 255
            );

            // event.state = readBuffer.get() != 0;
            setState(
                    event, readBuffer.get() != 0
            );

            int character = readBuffer.getInt();

            // event.character = readBuffer.getInt();
            setCharacter(
                    event, character
            );

            if(keyBuffer == 0 && character != 0)
            {
                setKey(event, getFixedKey(character));
            }

            // event.nanos = readBuffer.getLong();
            setNanos(
                    event, readBuffer.getLong()
            );

            // event.repeat = readBuffer.get() == 1;
            setRepeat(
                    event, readBuffer.get() == 1
            );

            return true;
        }

        return false;
    }
}