package com.joojn;

import com.joojn.mixins.MixinMain;
import com.joojn.mixins.MixinTransformer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Main {

    public static void premain(String agentArgs, Instrumentation inst)
    {
        try
        {
            MixinMain.loadMixins();
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        inst.addTransformer(MixinMain.getMixinTransformer());
    }
}
