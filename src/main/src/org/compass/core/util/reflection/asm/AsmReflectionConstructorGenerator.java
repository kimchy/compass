/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.util.reflection.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.compass.core.util.reflection.ReflectionConstructor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Allows to generate an {@link ReflectionConstructor} implementation based on ASM that does not use
 * reflection.
 *
 * @author kimchy
 */
public class AsmReflectionConstructorGenerator {

    final private static String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

    final private static String[] REFLECTIONCONSTRUCTOR_INTERNAL_NAME = new String[]{Type.getInternalName(ReflectionConstructor.class)};

    /**
     * Allows to generate an {@link ReflectionConstructor} implementation based on ASM that does not use
     * reflection.
     */
    public static synchronized ReflectionConstructor generateConstructor(Constructor originalCtor) throws NoSuchMethodException {
        final Class declaringClass = originalCtor.getDeclaringClass();
        String ownerClassName = declaringClass.getName();

        Constructor[] declaredCtors = declaringClass.getDeclaredConstructors();
        int ctorIndex = 0;
        for (; ctorIndex < declaredCtors.length; ++ctorIndex) {
            if (declaredCtors[ctorIndex].equals(originalCtor))
                break;
        }
        String className = ownerClassName + "ConstReflection" + ctorIndex;


        try {
            Class definedClass;
            try {// checks if was already loaded
                definedClass = declaringClass.getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)   // need to build a new class
            {

                String classInternalName = className.replace('.', '/'); // build internal name for ASM

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                        classInternalName, null, OBJECT_INTERNAL_NAME,
                        REFLECTIONCONSTRUCTOR_INTERNAL_NAME);

                createConstructor(cw);
                createNewInstanceMethod(cw, declaringClass);

                cw.visitEnd();

                byte[] b = cw.toByteArray();
                definedClass = defineClass(declaringClass.getClassLoader(), className, b);
            }

            return (ReflectionConstructor) definedClass.newInstance();
        } catch (Exception e) {
            NoSuchMethodException ex = new NoSuchMethodException("Can't create ASM constructor reflection helper for [" + originalCtor + "]");
            ex.initCause(e);
            throw ex;
        }
    }

    private static void createConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static void createNewInstanceMethod(ClassWriter cw, Class clz) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(clz));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(clz), "<init>", "()V");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private static Class defineClass(ClassLoader loader, String name, byte[] b) throws Exception {
        Method defineMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineMethod.setAccessible(true);
        return (Class) defineMethod.invoke(loader, name, b, 0, b.length);
    }

}
