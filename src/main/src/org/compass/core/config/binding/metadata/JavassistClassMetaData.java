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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

/**
 * @author kimchy
 */
public class JavassistClassMetaData implements ClassMetaData {

    private ClassFile classFile;

    private AnnotationsAttribute visible;

    private AnnotationsAttribute invisible;

    public JavassistClassMetaData(ClassFile classFile) {
        this.classFile = classFile;
        visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        invisible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.invisibleTag);
    }

    public String getClassName() {
        return classFile.getName();
    }

    public boolean isInterface() {
        return classFile.isInterface();
    }

    public boolean isAbstract() {
        return classFile.isAbstract();
    }

    public boolean isConcrete() {
        throw new UnsupportedOperationException();
    }

    public boolean isIndependent() {
        throw new UnsupportedOperationException();
    }

    public boolean hasEnclosingClass() {
        throw new UnsupportedOperationException();
    }

    public String getEnclosingClassName() {
        throw new UnsupportedOperationException();
    }

    public boolean hasSuperClass() {
        return classFile.getSuperclass() != null;
    }

    public String getSuperClassName() {
        return classFile.getSuperclass();
    }

    public String[] getInterfaceNames() {
        return classFile.getInterfaces();
    }

    public Set<String> getAnnotationTypes() {
        Set<String> types = new HashSet<String>();
        for (Annotation ann : visible.getAnnotations()) {
            types.add(ann.getTypeName());
        }
        return types;
    }

    public boolean hasAnnotation(String annotationType) {
        if (visible != null && visible.getAnnotation(annotationType) != null) {
            return true;
        }
        if (invisible != null && invisible.getAnnotation(annotationType) != null) {
            return true;
        }
        return false;
    }

    public Set<String> getMetaAnnotationTypes(String annotationType) {
        throw new UnsupportedOperationException();
    }

    public boolean hasMetaAnnotation(String metaAnnotationType) {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> getAnnotationAttributes(String annotationType) {
        throw new UnsupportedOperationException();
    }
}
