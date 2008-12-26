/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.mapping.osem.builder;

/**
 * @author kimchy
 */
public abstract class OSEM {

    private OSEM() {

    }

    public static ClassMappingBuilder clazz(Class clazz) {
        return new ClassMappingBuilder(clazz);
    }

    public static ClassIdMappingBuilder id(String name) {
        return new ClassIdMappingBuilder(name);
    }

    public static ClassIdComponentMappingBuilder idComponent(String name) {
        return new ClassIdComponentMappingBuilder(name);
    }

    public static ClassComponentMappingBuilder component(String name) {
        return new ClassComponentMappingBuilder(name);
    }

    public static ClassReferenceMappingBuilder reference(String name) {
        return new ClassReferenceMappingBuilder(name);
    }

    public static ClassConstantMappingBuilder constant(String name) {
        return new ClassConstantMappingBuilder(name);
    }

    public static ClassDynamicMetaDataMappingBuilder dynamicMetadata(String name, String converter, String expression) {
        return new ClassDynamicMetaDataMappingBuilder(name, converter, expression);
    }

    public static ClassPropertyMappingBuilder property(String name) {
        return new ClassPropertyMappingBuilder(name);
    }

    public static ClassMetaDataMappingBuilder metadata(String name) {
        return new ClassMetaDataMappingBuilder(name);
    }

    public static ClassAnalyzerMappingBuilder analyzer(String name) {
        return new ClassAnalyzerMappingBuilder(name);
    }

    public static ClassCascadeMappingBuilder cascade(String name) {
        return new ClassCascadeMappingBuilder(name);
    }

    public static ClassParentMappingBuilder parent(String name) {
        return new ClassParentMappingBuilder(name);
    }

    public static ClassBoostMappingBuilder boost(String name) {
        return new ClassBoostMappingBuilder(name);
    }

    public static ClassAllMappingBuilder all() {
        return new ClassAllMappingBuilder();
    }
}
