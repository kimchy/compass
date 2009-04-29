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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.compass.core.util.reflection.ReflectionField;
import org.compass.core.util.reflection.plain.PlainReflectionField;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Allows to generate {@link org.compass.core.util.reflection.ReflectionField} implementation based on
 * ASM that does not use reflection.
 *
 * @author kimchy
 */
public class AsmReflectionFieldGenerator {

    final private static String PLAIN_FIELD_INTERNAL_NAME = Type.getInternalName(PlainReflectionField.class);

    /**
     * Allows to generate {@link org.compass.core.util.reflection.ReflectionField} implementation based on
     * ASM that does not use reflection.
     */
    public static synchronized ReflectionField generateField(Field refField) throws NoSuchFieldException {
        Class declaringClass = refField.getDeclaringClass();
        String ownerClassName = declaringClass.getName();

        Field[] declaredFields = declaringClass.getDeclaredFields();
        int methodIndex = 0;
        for (; methodIndex < declaredFields.length; ++methodIndex) {
            if (declaredFields[methodIndex].equals(refField))
                break;
        }

        String className = ownerClassName + "FieldReflection" + methodIndex;

        try {
            Class definedClass;
            try { // checks if was already loaded
                definedClass = declaringClass.getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e) // need to build a new class
            {
                String classInternalName = className.replace('.', '/'); // build internal name for ASM

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classInternalName,
                        null, PLAIN_FIELD_INTERNAL_NAME, null);

                createConstructor(cw);
                createGetMethod(cw, declaringClass, refField);
                createSetMethod(cw, declaringClass, refField);
                cw.visitEnd();

                byte[] b = cw.toByteArray();

                definedClass = defineClass(refField.getDeclaringClass().getClassLoader(), className, b);
            }
            Constructor ctor = definedClass.getConstructor(Field.class);
            return (ReflectionField) ctor.newInstance(refField);
        }
        catch (Exception e) {
            NoSuchFieldException err = new NoSuchFieldException("Can't create ASM field reflection helper for [" + refField + "]");
            err.initCause(e);
            throw err;
        }
    }

    private static void createConstructor(ClassVisitor cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/Field;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(PlainReflectionField.class), "<init>",
                "(Ljava/lang/reflect/Field;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private static void createGetMethod(ClassVisitor cw, Class entryClass, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
                new String[]{"java/lang/IllegalArgumentException", "java/lang/IllegalAccessException"});
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(entryClass));
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(entryClass), field.getName(), Type.getDescriptor(field.getType()));
        boxIfNeeded(mv, field);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }

    private static void createSetMethod(ClassVisitor cw, Class entryClass, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V",
                null, new String[]{"java/lang/IllegalArgumentException", "java/lang/IllegalAccessException"});
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(entryClass));
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        castAndUnboxIfNeeded(mv, field);
        mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(entryClass), field.getName(), Type.getDescriptor(field.getType()));
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

    private static void castAndUnboxIfNeeded(MethodVisitor mv, Field field) {
        if (!field.getType().isPrimitive()) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(field.getType()));
            return;
        }

        Type type = Type.getType(field.getType());
        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                break;
            case Type.INT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                break;
            case Type.SHORT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                break;
            case Type.LONG:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                break;
            case Type.FLOAT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                break;
            case Type.DOUBLE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                break;
            case Type.BYTE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                break;
            case Type.CHAR:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                break;
        }
    }

    private static void boxIfNeeded(MethodVisitor mv, Field field) {
        if (!field.getType().isPrimitive())
            return;

        Type type = Type.getType(field.getType());
        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                break;
            case Type.INT:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                break;
            case Type.SHORT:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                break;
            case Type.LONG:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                break;
            case Type.FLOAT:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                break;
            case Type.DOUBLE:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                break;
            case Type.BYTE:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                break;
            case Type.CHAR:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                break;
        }
    }

    private static Class defineClass(ClassLoader loader, String name, byte[] b) throws Exception {
        Method defineMethod = ClassLoader.class.getDeclaredMethod("defineClass",
                String.class, byte[].class, int.class, int.class);
        defineMethod.setAccessible(true);
        return (Class) defineMethod.invoke(loader, name, b, 0, b.length);
    }

}
