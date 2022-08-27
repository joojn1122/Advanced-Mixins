package com.joojn.mixins.annotation;

import com.joojn.mixins.annotation_processor.RequiredAnnotation;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

@RequiredAnnotation(
        access = Modifier.PUBLIC,
        params = {ClassNode.class},
        allowedReturnTypes = {MethodInsnNode.class, FieldInsnNode.class}
)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ShadowFor {
    String name();
}
