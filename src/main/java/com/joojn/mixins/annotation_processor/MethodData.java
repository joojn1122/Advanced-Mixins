package com.joojn.mixins.annotation_processor;

import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Container class for method information
 *
 * @author <a href=\"mailto:christoffer@christoffer.me\">Christoffer Pettersson</a>
 */

final class MethodData {

    private TypeMirror returnType;
    private List<? extends TypeMirror> parameterTypes;
    private List<? extends TypeMirror> thrownTypes;

    public void setParameterTypes(final List<? extends TypeMirror> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public List<? extends TypeMirror> getParameterTypes() {
        return parameterTypes;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public void setReturnType(final TypeMirror returnType) {
        this.returnType = returnType;
    }

    public void setThrownTypes(final List<? extends TypeMirror> thrownTypes) {
        this.thrownTypes = thrownTypes;
    }

    public List<? extends TypeMirror> getThrownTypes() {
        return thrownTypes;
    }

}