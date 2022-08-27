package com.joojn.mixins.annotation_processor;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes("com.joojn.mixins.annotation_processor.RequiredAnnotation")
public class RequiredAnnotationProcessor extends AbstractProcessor {

    private final Class<?>[] allowedReturnTypes;
    private final Class<?>[] params;
    private final int access;

    public RequiredAnnotationProcessor(
            Class<?>[] allowedReturnTypes,
            Class<?>[] params,
            int access
    )
    {
        this.allowedReturnTypes = allowedReturnTypes;
        this.params = params;
        this.access = access;
    }

    private <T> boolean listEquals(List<T> classes, List<T> classes2)
    {
        if(classes.size() != classes2.size()) return false;

        for(int i = 0; i < classes.size() ; i++)
        {
            T var1 = classes.get(i);
            T var2 = classes2.get(i);

            if(!Objects.equals(var1, var2)) return false;
        }

        return true;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {

        for(TypeElement element : annotations)
        {
            if(element instanceof ExecutableType)
            {
                ExecutableType type = (ExecutableType) element.asType();
                if(
                        !(Arrays.stream(allowedReturnTypes)
                                .anyMatch(returnType -> type.getReturnType()
                                        .toString().equals(returnType.getName())
                                )
                        && params.length == type.getParameterTypes().size()

                        && listEquals(Arrays.stream(this.params)
                                .map(Class::getName).collect(Collectors.toList()),

                            type.getParameterTypes().stream().map(TypeMirror::toString)
                                    .collect(Collectors.toList())
                        ))
                )
                {
                    // error
                    return true;
                }
            }
            else if(element instanceof javax.lang.model.element.TypeParameterElement)
            {

            }
        }

        return false;
    }
}
