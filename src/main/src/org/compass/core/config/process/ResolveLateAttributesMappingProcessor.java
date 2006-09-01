/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.config.process;

import java.util.Iterator;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.osem.ClassMapping;

/**
 * Reolves late attributes associated usually with {@link ClassMapping}, they are:
 * <p/>
 * SupportUnmarshall: One can set globally if ClassMappings will support unmarshalling or not.
 *
 * @author kimchy
 */
public class ResolveLateAttributesMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        compassMapping.setPath(namingStrategy.getRootPath());
        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) m;
                if (!classMapping.isSupportUnmarshallSet()) {
                    classMapping.setSupportUnmarshall(settings.getSettingAsBoolean(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, true));
                }
            }
        }

        return compassMapping;
    }
}
