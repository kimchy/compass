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

package org.compass.core.util.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.reflection.asm.AsmReflectionConstructorGenerator;
import org.compass.core.util.reflection.asm.AsmReflectionFieldGenerator;
import org.compass.core.util.reflection.asm.AsmReflectionMethodGenerator;
import org.compass.core.util.reflection.plain.PlainReflectionConstructor;
import org.compass.core.util.reflection.plain.PlainReflectionField;
import org.compass.core.util.reflection.plain.PlainReflectionMethod;

/**
 * A factory allowing to create {@link org.compass.core.util.reflection.ReflectionMethod} or
 * {@link org.compass.core.util.reflection.ReflectionField} implementations.
 *
 * @author kimchy
 */
public class ReflectionFactory {

    private static final Log log = LogFactory.getLog(ReflectionFactory.class);

    public static ReflectionMethod getMethod(CompassSettings settings, Method method) throws NoSuchMethodException {
        if (canGenerateAsm(method) && CompassEnvironment.Reflection.ASM.equals(settings.getSetting(CompassEnvironment.Reflection.TYPE, CompassEnvironment.Reflection.ASM))) {
            try {
                return AsmReflectionMethodGenerator.generateMethod(method);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to generate ASM (should have worked...) for method [" + method + "]", e);
                }
                // do nothing
            }
        }
        return new PlainReflectionMethod(method);
    }

    public static ReflectionField getField(CompassSettings settings, Field field) throws NoSuchFieldException {
        if (canGenerateAsm(field) && CompassEnvironment.Reflection.ASM.equals(settings.getSetting(CompassEnvironment.Reflection.TYPE, CompassEnvironment.Reflection.ASM))) {
            try {
                return AsmReflectionFieldGenerator.generateField(field);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to generate ASM (should have worked...) for field [" + field + "]", e);
                }
                // do nothing
            }
        }
        return new PlainReflectionField(field);
    }

    public static ReflectionConstructor getDefaultConstructor(CompassSettings settings, Class clazz) {
        Constructor defaultConstructor = ClassUtils.getDefaultConstructor(clazz);
        if (defaultConstructor == null) {
            return null;
        }
        return getConstructor(settings, defaultConstructor);
    }

    public static ReflectionConstructor getConstructor(CompassSettings settings, Constructor constructor) {
        if (canGenerateAsm(constructor) && CompassEnvironment.Reflection.ASM.equals(settings.getSetting(CompassEnvironment.Reflection.TYPE, CompassEnvironment.Reflection.ASM))) {
            try {
                return AsmReflectionConstructorGenerator.generateConstructor(constructor);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to generate ASM (should have worked...) for constructor [" + constructor + "]", e);
                }
                // do nothing
            }
        }
        return new PlainReflectionConstructor(constructor);
    }

    private static boolean canGenerateAsm(Member member) {
        // we can only generate for members that are not private (so we can set them)
        // and that the class is public and not static (i.e. not intenral class within another class)
        return !Modifier.isPrivate(member.getModifiers());
    }
}
