package com.joojn.mixins.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MixinMethod {
    String name();
    int atLine() default 0; // -1 == end
    boolean override() default false;
}
