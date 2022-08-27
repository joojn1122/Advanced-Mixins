package com.joojn.helper;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SuperClassHelper {

    public static byte[] createSuperClass(
            Class<?> clazz,
            Class<?> superClass,
            String name
    )
    {
        return createSuperClass(clazz, superClass, name, Opcodes.V1_8);
    }

    public static byte[] createSuperClass(
                                          Class<?> clazz,
                                          Class<?> superClass,
                                          String name,
                                          int javaVersion
    )
    {

        ClassWriter currentCW = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        currentCW.visit(
                javaVersion,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                name.replace(".", "/"),
                null,
                superClass.getName().replace(".", "/"),
                null
        );

        // create constructor
        MethodVisitor mw = currentCW.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );

        mw.visitCode();
        mw.visitVarInsn(Opcodes.ALOAD, 0);

        mw.visitMethodInsn(Opcodes.INVOKESPECIAL,
                superClass.getName().replace(".", "/"),
                "<init>",
                "()V",
                superClass.isInterface());

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(1, 1);

        currentCW.visitEnd();

        byte[] classBuffer = currentCW.toByteArray();

        ClassReader currentCR;

        try
        {
            currentCR = new ClassReader(clazz.getName());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        ClassNode currentCN = new ClassNode();
        currentCR.accept(currentCN, 0);

        ClassReader newCR = new ClassReader(classBuffer);
        ClassNode newCN = new ClassNode();
        newCR.accept(newCN, 0);

        // replace methods
        List<MethodNode> methods = new ArrayList<>();

        for(MethodNode methodNode : newCN.methods)
        {
            if(methodNode.name.equals("<init>")
                    || methodNode.name.equals("<clinit>"))
            {
                for(AbstractInsnNode node : methodNode.instructions)
                {
                    if(node instanceof MethodInsnNode)
                    {
                        ((MethodInsnNode) node).owner = superClass.getName().replace(".", "/");
                    }
                }

                methods.add(methodNode);
            }
        }

        for(MethodNode methodNode : currentCN.methods)
        {
            if(methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) continue;
            methods.add(methodNode);
        }

        newCN.methods = methods;

        ClassWriter newCW = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        newCN.accept(newCW);

        return newCW.toByteArray();
    }
}
