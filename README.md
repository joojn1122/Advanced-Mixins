# Mixins

This is low level mixin for modifying existing java classes.
This concept was inspired by
[SpongePowered Mixin](https://github.com/SpongePowered/Mixin).

You can download the latest release in [Releases](../../releases).
I'll add maven dependency later.

### Development
The code is terrible and really hard to read so please don't judge me ¯\\\_(ツ)_/¯.
**It's still in development.**

It has a lot of bugs for example you can't use lambda expressions in the method.

### To add target mixin
In resources folder you will need to create file `mixin-config.json`,
which looks like this

*(Right now this is only config this file has)*

```json
{
  "mixins" : [
    "path.to.your.mixin.class",
    ...
  ]
}
```

Then your class has to implement `com.joojn.mixins.Mixin`

**Override** `getTargetClass()` with class you want to target.

### Modifying existing method
To modify already existing method you need 2 methods,
one which tells the program which method to target and second to do the modifying

```java
import com.joojn.mixins.Mixin;
import com.joojn.mixins.annotation.MixinMethodFor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class TestClass implements Mixin {

    /**
     name is not the method name,
     but the name you are targeting 
     also, the method names doesn't matter
     ! IMPORTANT !
     @MixinMethodFor has always return type of MethodNode
     and 1 param of ClassNode
     */
    @MixinMethodFor(name = "someName")
    public MethodNode someMethodNameTargeter(ClassNode cn)
    {
        for (MethodNode method : cn.methods) 
        {
            // let's say we want to target first void with no params
            // method in the class
            // so the #someMethodName() has to be void with no params
            if (
                    method.desc.equals("()V")
            ) {
                return method;
            }
        }

        // if not found, return null (if null is returned this method is ignored)
        return null;
    }

    // same name, just to connect them together
    // the  only thing that matters, is the params and return type
    // then we can specify 

    // override = false/true, default false -> if the whole method should be overridden 
    // atLine = int, default = 0 (start) -> at which line of method 
    // this should be injected (negative numbers works from other side => -1 = end)
    // if override is true, atLine is ignored
    @MixinMethod(name = "someName", override = true)
    public void someMethodName() {
        System.out.println("Injected!");
    }

    @Override
    public String getTargetClass() {
        return "some.target.class";
    }
}
```

### Creating new method

This is pretty self-explanatory

```java
@MixinNewMethod
public void newMethod(String someParams){
    System.out.println(someParams);
}
```

This will create `newMethod(java/lang/String;)V` in target class.


`@MixinNewField` doesn't exist because I don't think it has any usage.

### Shadowing methods / fields

Shadowing method / field is similar to the MethodSelecting
Lets say we want to create `createClass(java/lang/String;[B)java/lang/Class;`
which is basically `defineClass(...)` but its protected 
and unable to invoke using reflection in `ClassLoader` class.

```java
import com.joojn.mixins.Mixin;
import com.joojn.mixins.annotation.Shadow;
import com.joojn.mixins.annotation.ShadowFor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ClassLoaderMixin implements Mixin {

    @Shadow(name = "defineClass__")
    public native Class<?> defineClass(String name,
                                       byte[] buffer,
                                       int offset,
                                       int length
    );

    /**  we can make it native or empty body, it doesn't matter
     its just placeholder, so we can call it

     this is selector for the defineClass (same name)

     ! IMPORTANT !
     @shadowFor has return type of MethodInsNode or FieldInsNode 
     (if you are targeting method or field)
     and always 1 param of ClassNode
     */
    @ShadowFor(name = "defineClass__")
    public MethodInsnNode getDefineClass(ClassNode cn)
    {
        return new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,                        // so it's method call
                cn.name,                                      // class name
                "defineClass",                                // method name
                "(Ljava/lang/String;[BII)Ljava/lang/Class;"   // method desc
        );
    }
    
    // create the method
    @MixinNewMethod
    public Class<?> createClass(String name, byte[] b)
    {
        return defineClass(name, b, 0, b.length); // we can use this shadowed method
    }
    
    // get target class
    @Override
    public String getTargetClass() {
        return ClassLoader.class.getName();
    }
}
```

Now we can simply define new class from any `ClassLoader` using reflection.
```java
ClassLoader cl = getClassLoader();

Method createClassMethod = cl.getClass().getDeclaredMethod("createClass", String.class, byte[].class);
Class<?> newClass = createClassMethod.invoke(cl, "new.class", classByteCode);
```

## Make mixin works
To make all working you have to use [Java Agent](https://www.developer.com/design/what-is-java-agent/).

```java
package your.main.clazz;

import com.joojn.mixins.MixinMain;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Main {

    // premain which java agent is using
    public static void premain(String agentsArgs, Instrumentation inst) {

        try
        {
            MixinMain.loadMixins(); // load mixins, without this it won't work
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        // add transformer
        inst.addTransformer(MixinMain.getMixinTransformer());
    }
}
```

You also need to register this to the [MANIFEST.MF](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html) file.

```manifest
Manifest-Version: 1.0
Premain-Class: your.main.clazz
Can-Redefine-Classes: true
Can-Retransform-Classes: true

```



## Conclusion
I know it's really useless to do this weird selectors,
but I made it like this because I used this to
modify program which was obfuscated, so I had to
find everything using return types and params.

I'll probably make a second version without these selectors, so it's easier to use.

I hope you understood everything, if not you can contact me 
on discord `joojn#5485` or open issue request.

I'll make YouTube video for setup in the future.

**Also, any help is appreciated, so you can create a pull request.**
