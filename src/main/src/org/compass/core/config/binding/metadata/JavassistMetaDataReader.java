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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javassist.bytecode.ClassFile;
import org.compass.core.mapping.MappingException;

/**
 * @author kimchy
 */
public class JavassistMetaDataReader implements MetaDataReader {

    public ClassMetaData getClassMetaData(InputStream is, String resourceName) throws MappingException {
        DataInputStream dstream = new DataInputStream(new BufferedInputStream(is));
        try {
            return new JavassistClassMetaData(new ClassFile(dstream));
        } catch (IOException e) {
            throw new MappingException("Failed to read [" + resourceName + "] class meta data using javassist", e);
        }
    }
}
