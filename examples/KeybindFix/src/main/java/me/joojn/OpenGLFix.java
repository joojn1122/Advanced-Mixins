package me.joojn;

import com.joojn.mixins.MixinMain;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class OpenGLFix {

    public static void premain(String agentArgs, Instrumentation inst)
    {
        try
        {
            MixinMain.loadMixins();
            inst.addTransformer(MixinMain.getMixinTransformer());
        }
        catch (ClassNotFoundException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
