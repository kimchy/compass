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

import org.compass.core.CompassException;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * A factory for creating {@link org.compass.core.config.binding.metadata.MetaDataReader} based on the settings
 * provided ({@link org.compass.core.config.CompassEnvironment.Scanner#READER}.
 *
 * @author kimchy
 */
public class MetaDataReaderFactory {

    /**
     * Returns the {@link MetaDataReader} to use. The default one is the internal ASM one.
     */
    public static MetaDataReader getMetaDataReader(CompassSettings settings) throws CompassException {
        return (MetaDataReader) settings.getSettingAsInstance(CompassEnvironment.Scanner.READER, AsmMetaDataReader.class.getName());
    }
}
