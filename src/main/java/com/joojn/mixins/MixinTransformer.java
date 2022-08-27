package com.joojn.mixins;

import org.objectweb.asm.ClassReader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class MixinTransformer implements ClassFileTransformer {

    public static final HashMap<String, byte[]> usedClasses = new HashMap<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
    {
        // to prevent load one class multiple times
        if(usedClasses.containsKey(className)) {
            return classfileBuffer;
        }

        usedClasses.put(className, classfileBuffer);

        Mixin mixin = MixinMain.getByName(className.replace("/", "."));

        if(mixin == null) return classfileBuffer;

        ClassReader cr = new ClassReader(classfileBuffer);

        return MixinMain.transformClass(mixin, cr);
    }
}
