package com.joojn.examples;

import com.joojn.mixins.Mixin;
import com.joojn.mixins.annotation.MixinMethod;
import com.joojn.mixins.annotation.MixinMethodFor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TargetClassMixin implements Mixin
{

    @MixinMethodFor(name="testClass")
    public MethodNode selectorMethod(ClassNode node)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("testClass"))
            {
                return method;
            }
        }

        return null;
    }

    @MixinMethod(name="testClass", atLine = 0, override = true)
    public void testClass(Object value)
    {

    }

    @Override
    public String getTargetClass()
    {
        return "com.joojn.InjectMe";
    }
}
