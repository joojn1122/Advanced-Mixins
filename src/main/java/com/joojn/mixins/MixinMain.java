package com.joojn.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.joojn.helper.MixinHelper;
import com.joojn.mixins.annotation.*;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MixinMain {

    // private constructor
    private MixinMain(){}

    private static final MixinTransformer mixinTransformer = new MixinTransformer();

    public static MixinTransformer getMixinTransformer()
    {
        return mixinTransformer;
    }

    private static final Set<Mixin> registeredObjects = new HashSet<>();
    private static final Logger logger = new Logger("LunarMixin");

    public static void loadMixins() throws ClassNotFoundException, IOException, NullPointerException
    {
        InputStream stream = ClassLoader.getSystemResourceAsStream("mixin-config.json");
        JsonObject object = JsonParser.parseString(String.join("\n", IOUtils.readLines(stream, StandardCharsets.UTF_8))).getAsJsonObject();

        for(JsonElement element : object.getAsJsonArray("mixins"))
        {
            String className = element.getAsString();

            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);

            if(!Arrays.asList(clazz.getInterfaces()).contains(Mixin.class))
            {
                logger.error("Class doesn't implement LunarMixin! " + clazz.getName());
                continue;
            }

            try
            {
                registeredObjects.add((Mixin) clazz.newInstance());
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                logger.error("Could not create new instance for class " + clazz.getName());
            }
        }
    }

    public static Mixin getByName(String className)
    {
        for(Mixin mixin : registeredObjects)
        {
            if(mixin.getTargetClass().replace("/", ".").equals(className)) return mixin;
        }

        return null;
    }

    public static byte[] transformClass(Mixin mixin, ClassReader cr)
    {
        ClassNode node = new ClassNode();
        cr.accept(node, 0);

        HashMap<String, MethodNode> registeredMethods = new HashMap<>();

        for(Method method : mixin.getClass().getDeclaredMethods())
        {
            if(
                    MixinHelper.isMixinMethodFor(method)
            )
            {

                try
                {
                    registeredMethods.put(method.getAnnotation(MixinMethodFor.class).name(),
                            (MethodNode) method.invoke(mixin, node));
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // shadow

        HashMap<String, FieldInsnNode> shadowedFields = new HashMap<>();
        HashMap<String, MethodInsnNode> shadowedMethods = new HashMap<>();

        HashMap<Field, FieldInsnNode> registeredShadowedFields = new HashMap<>();
        HashMap<Method, MethodInsnNode> registeredShadowedMethods = new HashMap<>();

        // register names
        for(Method method : mixin.getClass().getDeclaredMethods())
        {
            if(
                    MixinHelper.isShadowFor(method)
            )
            {
                try
                {
                    String shadowFor = method.getAnnotation(ShadowFor.class).name();
                    Object returnedValue = method.invoke(mixin, node);

                    if(method.getReturnType().equals(FieldInsnNode.class))
                        shadowedFields.put(shadowFor, (FieldInsnNode) returnedValue);
                    else if(method.getReturnType().equals(MethodInsnNode.class))
                        shadowedMethods.put(shadowFor, (MethodInsnNode) returnedValue);

                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // get fields
        for(Field field : mixin.getClass().getDeclaredFields())
        {
            if(field.isAnnotationPresent(Shadow.class))
            {
                String name = field.getAnnotation(Shadow.class).name();

                FieldInsnNode fieldInsnNode = shadowedFields.get(name);
                if(fieldInsnNode == null)
                {
                    logger.error("FieldNode is null, field node " + name);
                    continue;
                }

                registeredShadowedFields.put(field, fieldInsnNode);
            }
        }

        // get methods
        for(Method method : mixin.getClass().getDeclaredMethods())
        {
            if(method.isAnnotationPresent(Shadow.class))
            {
                String name = method.getAnnotation(Shadow.class).name();

                MethodInsnNode methodNode = shadowedMethods.get(name);
                if(methodNode == null)
                {
                    logger.error("MethodNode is null, method node " + name);
                    continue;
                }

                registeredShadowedMethods.put(method, methodNode);
            }
        }

        // transform classes
        for(Method method : mixin.getClass().getDeclaredMethods())
        {
            if(method.isAnnotationPresent(MixinMethod.class))
            {
                String name = method.getAnnotation(MixinMethod.class).name();
                MethodNode methodNode = registeredMethods.get(name);

                if(methodNode == null) {
                    logger.error("MethodNode is null, skipping for method " + method.getDeclaringClass().getName() + "." + method.getName() + "()");
                    continue;
                }

                try
                {
                    transformMethod(
                            method,
                            methodNode,
                            cr,
                            registeredShadowedMethods,
                            registeredShadowedFields
                    );
                }
                catch (IOException e)
                {
                    logger.error("Error while transforming method " + methodNode.name + " " + e);
                }
            }
            else if(method.isAnnotationPresent(MixinNewMethod.class))
            {
                try
                {
                    createNewMethod(node, method, registeredShadowedMethods, registeredShadowedFields);
                }
                catch (IOException e)
                {
                    logger.error("Error while trying to create new method -> " + method.getName() + " in class " + method.getDeclaringClass().getName());
                    e.printStackTrace();
                }
            }
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        node.accept(cw);

        return cw.toByteArray();
    }

    private static void createNewMethod(ClassNode cn,
                                        Method method,
                                        HashMap<Method, MethodInsnNode> registeredShadowedMethods,
                                        HashMap<Field, FieldInsnNode> registeredShadowedFields
    ) throws IOException
    {
        Class<?> classCaller = method.getDeclaringClass();

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(classCaller.getName());

        reader.accept(node, 0);

        for(MethodNode methodNode : node.methods)
        {
            if(
                    MixinHelper.methodEquals(methodNode, method)
            )
            {

                logger.info("Creating new method %s.%s() from method %s.%s()" ,
                        cn.name,
                        methodNode.name,
                        classCaller.getName(),
                        method.getName()
                );

                filter(methodNode.instructions,
                        registeredShadowedMethods,
                        registeredShadowedFields);

                cn.methods.add(methodNode);

                break;
            }
        }
    }

    protected static boolean debug = false;
    public static void setDebug(boolean debug_)
    {
        debug = debug_;
    }

    private static void transformMethod(Method method,
                                        MethodNode methodNode,
                                        ClassReader cr,
                                        HashMap<Method, MethodInsnNode> registeredShadowedMethods,
                                        HashMap<Field, FieldInsnNode> registeredShadowedFields
    ) throws IOException
    {
        Class<?> classCaller = method.getDeclaringClass();
        MixinMethod methodAnnotation = method.getAnnotation(MixinMethod.class);

        ClassReader reader = new ClassReader(classCaller.getName());
        ClassNode node = new ClassNode();

        reader.accept(node, 0);

        for(MethodNode methodNode1 : node.methods)
        {
            if(
                    MixinHelper.methodEquals(methodNode1, method)
            )
            {
                logger.info("Transforming method %s.%s() from method %s.%s()" ,
                        cr.getClassName(),
                        methodNode.name,
                        classCaller.getName(),
                        method.getName()
                );

                // filter values
                filter(methodNode1.instructions,
                        registeredShadowedMethods,
                        registeredShadowedFields);

///////////// TODO: Make lambda working
//
//                String prevClass = classCaller.getName().replace(".", "/");
//                String newClass = cr.getClassName();
//
//                for(AbstractInsnNode abNode : methodNode1.instructions)
//                {
//                    MixinHelper.replaceStrings(abNode, prevClass, newClass, true);
//                }

//                for(AbstractInsnNode abNode : methodNode1.instructions)
//                {
//                    if(abNode instanceof InvokeDynamicInsnNode)
//                    {
//                        InvokeDynamicInsnNode dynamicInsnNode = (InvokeDynamicInsnNode) abNode;
//
//                        for(int j = 0 ; j < dynamicInsnNode.bsmArgs.length ; j++)
//                        {
//                            Object object = dynamicInsnNode.bsmArgs[j];
//
//                            if(object instanceof Handle)
//                            {
//                                System.out.println("Handle!");
//
//                                Handle handle = (Handle) object;
//                                if(
//                                        handle.getOwner().equals(classCaller.getName().replace(".", "/"))
//                                )
//                                {
//                                    dynamicInsnNode.bsmArgs[j] = new Handle(
//                                                    handle.getTag(),
//                                                    cr.getClassName(),
//                                                    handle.getName(),
//                                                    handle.getDesc(),
//                                                    handle.isInterface());
//                                }
//                            }
//                        }
//                    }
//                }

                if(debug)
                {
                    printInstructions(methodNode.instructions);
                    printInstructions(methodNode1.instructions);
                }

                if(methodAnnotation.override())
                {
                    methodNode.instructions = methodNode1.instructions;
                }
                // at end
                else if(methodAnnotation.atLine() == -1)
                {
                    InsnList list = new InsnList();

                    // remove RETURN
                    int size = getReturnNodeIndex(methodNode.instructions);

                    for(int i = 0; i < size ; i++)
                    {
                        list.add(methodNode.instructions.get(i));
                    }

                    methodNode1.instructions.forEach(list::add);
                    methodNode.instructions = list;
                }
                else
                {
                    InsnList list = new InsnList();
                    int line = methodAnnotation.atLine();
                    
                    // example -2 => size = 10 => 7th index
                    if(line < 0) line = methodNode.instructions.size() - line - 1;

                    for(int i = 0; i < methodNode.instructions.size() ; i++)
                    {
                        if(i == line)
                        {
                            // remove RETURN
                            int size = getReturnNodeIndex(methodNode1.instructions);
                            
                            for(int j = 0; j < size ; j++)
                            {
                                list.add(methodNode1.instructions.get(j));
                            }
                        }

                        list.add(methodNode.instructions.get(i));
                    }

                    methodNode.instructions = list;

                    if(debug)
                    {
                        printInstructions(methodNode.instructions);
                    }
                }

                break;
            }
        }

        ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        node.accept(classWriter);
    }

    private static int getReturnNodeIndex(InsnList list)
    {
        for( int i = list.size() - 1 ; i >= 0 ; i-- )
        {
            AbstractInsnNode n = list.get(i);
            if(n.getOpcode() == Opcodes.RETURN)
            {
                return i;
            }
        }
        
        return 0;
    }
    
    private static void filter(InsnList list, HashMap<Method, MethodInsnNode> registeredShadowedMethods, HashMap<Field, FieldInsnNode> registeredShadowedFields)
    {
        for(AbstractInsnNode node : list)
        {
            if(node instanceof MethodInsnNode)
            {
                MethodInsnNode methodNode = (MethodInsnNode) node;
                filterMethod(methodNode, registeredShadowedMethods);
            }
            else if(node instanceof FieldInsnNode)
            {
                FieldInsnNode fieldNode = (FieldInsnNode) node;
                filterField(fieldNode, registeredShadowedFields);
            }
        }
    }

    private static void filterField(FieldInsnNode fieldNode, HashMap<Field, FieldInsnNode> map)
    {
        for(Field field : map.keySet())
        {
            if(
                    field.getName().equals(fieldNode.name)
            )
            {
                FieldInsnNode node = map.get(field);

                fieldNode.name = node.name;
                fieldNode.desc = node.desc;
                fieldNode.owner = node.owner;

                break;
            }
        }
    }

    private static void filterMethod(MethodInsnNode methodNode, HashMap<Method, MethodInsnNode> map)
    {
        for(Method method : map.keySet())
        {
            if(
                    method.getName().equals(methodNode.name)
                    && Type.getType(method).toString().equals(methodNode.desc)
            )
            {
                MethodInsnNode node = map.get(method);

                methodNode.name = node.name;
                methodNode.desc = node.desc;
                methodNode.owner = node.owner;

                break;
            }
        }
    }

    private static void printInstructions(InsnList inst)
    {
        for(AbstractInsnNode node : inst.toArray())
        {
//            if(node instanceof LineNumberNode || node instanceof LabelNode) continue;

            System.out.println(node + " " + node.getOpcode());
        }

        System.out.println("-----------------");
    }
}
