package com.joojn.mixins.annotation_processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE})
public @interface RequiredAnnotation {

    Class<?>[] allowedReturnTypes();
    Class<?>[] params();
    int access() default -1;

}
