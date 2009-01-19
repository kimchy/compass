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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ClassMetaData} built based on an actual {@link Class}.
 *
 * @author kimchy
 */
public class StandardClassMetaData implements ClassMetaData {

    private final Class introspectedClass;

    public StandardClassMetaData(Class introspectedClass) {
        this.introspectedClass = introspectedClass;
    }

    public final Class getIntrospectedClass() {
        return this.introspectedClass;
    }

    public String getClassName() {
        return getIntrospectedClass().getName();
    }

    public boolean isInterface() {
        return getIntrospectedClass().isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getIntrospectedClass().getModifiers());
    }

    public boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    public boolean isIndependent() {
        return (!hasEnclosingClass() ||
                (getIntrospectedClass().getDeclaringClass() != null &&
                        Modifier.isStatic(getIntrospectedClass().getModifiers())));
    }

    public boolean hasEnclosingClass() {
        return (getIntrospectedClass().getEnclosingClass() != null);
    }

    public String getEnclosingClassName() {
        Class enclosingClass = getIntrospectedClass().getEnclosingClass();
        return (enclosingClass != null ? enclosingClass.getName() : null);
    }

    public boolean hasSuperClass() {
        return (getIntrospectedClass().getSuperclass() != null);
    }

    public String getSuperClassName() {
        Class superClass = getIntrospectedClass().getSuperclass();
        return (superClass != null ? superClass.getName() : null);
    }

    public String[] getInterfaceNames() {
        Class[] ifcs = getIntrospectedClass().getInterfaces();
        String[] ifcNames = new String[ifcs.length];
        for (int i = 0; i < ifcs.length; i++) {
            ifcNames[i] = ifcs[i].getName();
        }
        return ifcNames;
    }

    public Set<String> getAnnotationTypes() {
        Set<String> types = new HashSet<String>();
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (int i = 0; i < anns.length; i++) {
            types.add(anns[i].annotationType().getName());
        }
        return types;
    }

    public boolean hasAnnotation(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (int i = 0; i < anns.length; i++) {
            if (anns[i].annotationType().getName().equals(annotationType)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getMetaAnnotationTypes(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (int i = 0; i < anns.length; i++) {
            if (anns[i].annotationType().getName().equals(annotationType)) {
                Set<String> types = new HashSet<String>();
                Annotation[] metaAnns = anns[i].annotationType().getAnnotations();
                for (Annotation meta : metaAnns) {
                    types.add(meta.annotationType().getName());
                }
                return types;
            }
        }
        return null;
    }

    public boolean hasMetaAnnotation(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (int i = 0; i < anns.length; i++) {
            Annotation[] metaAnns = anns[i].annotationType().getAnnotations();
            for (Annotation meta : metaAnns) {
                if (meta.annotationType().getName().equals(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, Object> getAnnotationAttributes(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType().getName().equals(annotationType)) {
                Map<String, Object> attrs = new HashMap<String, Object>();
                Method[] methods = ann.annotationType().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                        try {
                            attrs.put(method.getName(), method.invoke(ann));
                        }
                        catch (Exception ex) {
                            throw new IllegalStateException("Could not obtain annotation attribute values", ex);
                        }
                    }
                }
                return attrs;
            }
        }
        return null;
    }
}
