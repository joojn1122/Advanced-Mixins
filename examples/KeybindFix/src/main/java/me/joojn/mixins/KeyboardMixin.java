package me.joojn.mixins;

import com.joojn.mixins.Mixin;
import com.joojn.mixins.annotation.MixinMethod;
import com.joojn.mixins.annotation.MixinMethodFor;
import me.joojn.util.KeyboardReflector;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class KeyboardMixin implements Mixin {

    @MixinMethodFor(name = "readNext")
    public MethodNode readNextFinder(ClassNode cn)
    {
        for(MethodNode method : cn.methods)
        {
            if(method.name.equals("readNext")) return method;
        }

        return null;
    }

    @MixinMethod(
            name = "readNext",
            override = true
    )
    private static boolean readNext(Object event)
    {
        return KeyboardReflector.readNext(event);
    }

    @Override
    public String getTargetClass() {
        return "org.lwjgl.input.Keyboard";
    }
}
