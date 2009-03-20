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

package org.compass.core.config.binding.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.compass.core.util.ClassUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * An ASM based class meta data provider.
 *
 * @author kimchy
 */
public class AsmClassMetaData extends EmptyVisitor implements ClassMetaData {

    private String className;

    private boolean isInterface;

    private boolean isAbstract;

    private String enclosingClassName;

    private boolean independentInnerClass;

    private String superClassName;

    private String[] interfaces;

    private final Map<String, Map<String, Object>> attributesMap = new LinkedHashMap<String, Map<String, Object>>();

    private final Map<String, Set<String>> metaAnnotationMap = new LinkedHashMap<String, Set<String>>();

    public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
        this.className = ClassUtils.convertResourcePathToClassName(name);
        this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
        this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
        if (supername != null) {
            this.superClassName = ClassUtils.convertResourcePathToClassName(supername);
        }
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = ClassUtils.convertResourcePathToClassName(interfaces[i]);
        }
    }

    public void visitOuterClass(String owner, String name, String desc) {
        this.enclosingClassName = ClassUtils.convertResourcePathToClassName(owner);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (outerName != null && this.className.equals(ClassUtils.convertResourcePathToClassName(name))) {
            this.enclosingClassName = ClassUtils.convertResourcePathToClassName(outerName);
            this.independentInnerClass = ((access & Opcodes.ACC_STATIC) != 0);
        }
    }

    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
        final String className = Type.getType(desc).getClassName();
        final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        return new EmptyVisitor() {
            public void visit(String name, Object value) {
                // Explicitly defined annotation attribute value.
                attributes.put(name, value);
            }

            public void visitEnd() {
                try {
                    Class annotationClass = getClass().getClassLoader().loadClass(className);
                    // Check declared default values of attributes in the annotation type.
                    Method[] annotationAttributes = annotationClass.getMethods();
                    for (Method annotationAttribute : annotationAttributes) {
                        String attributeName = annotationAttribute.getName();
                        Object defaultValue = annotationAttribute.getDefaultValue();
                        if (defaultValue != null && !attributes.containsKey(attributeName)) {
                            attributes.put(attributeName, defaultValue);
                        }
                    }
                    // Register annotations that the annotation type is annotated with.
                    Annotation[] metaAnnotations = annotationClass.getAnnotations();
                    Set<String> metaAnnotationTypeNames = new HashSet<String>();
                    for (Annotation metaAnnotation : metaAnnotations) {
                        metaAnnotationTypeNames.add(metaAnnotation.annotationType().getName());
                    }
                    metaAnnotationMap.put(className, metaAnnotationTypeNames);
                }
                catch (ClassNotFoundException ex) {
                    // Class not found - can't determine meta-annotations.
                }
                attributesMap.put(className, attributes);
            }
        };
    }


    public Set<String> getAnnotationTypes() {
        return this.attributesMap.keySet();
    }

    public boolean hasAnnotation(String annotationType) {
        return this.attributesMap.containsKey(annotationType);
    }

    public Set<String> getMetaAnnotationTypes(String annotationType) {
        return this.metaAnnotationMap.get(annotationType);
    }

    public boolean hasMetaAnnotation(String metaAnnotationType) {
        Collection<Set<String>> allMetaTypes = this.metaAnnotationMap.values();
        for (Set<String> metaTypes : allMetaTypes) {
            if (metaTypes.contains(metaAnnotationType)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> getAnnotationAttributes(String annotationType) {
        return this.attributesMap.get(annotationType);
    }

    public String getClassName() {
        return this.className;
    }

    public boolean isInterface() {
        return this.isInterface;
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public boolean isConcrete() {
        return !(this.isInterface || this.isAbstract);
    }

    public boolean isIndependent() {
        return (this.enclosingClassName == null || this.independentInnerClass);
    }

    public boolean hasEnclosingClass() {
        return (this.enclosingClassName != null);
    }

    public String getEnclosingClassName() {
        return this.enclosingClassName;
    }

    public boolean hasSuperClass() {
        return (this.superClassName != null);
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public String[] getInterfaceNames() {
        return this.interfaces;
    }
}
