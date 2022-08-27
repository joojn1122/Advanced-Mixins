package com.joojn.mixins.annotation;

import com.joojn.mixins.annotation_processor.RequiredAnnotation;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

@RequiredAnnotation(
        allowedReturnTypes = {MethodNode.class},
        params = {ClassNode.class},
        access = Modifier.PUBLIC
)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MixinMethodFor {
    String name();
}
