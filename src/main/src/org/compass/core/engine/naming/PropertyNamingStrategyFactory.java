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

package org.compass.core.engine.naming;

import org.compass.core.config.CompassSettings;

/**
 * The property naming strategy factory. Used to create property naming
 * strategies. Created by the
 * {@link org.compass.core.config.CompassConfiguration} and can be set using the
 * {@link org.compass.core.config.CompassEnvironment.NamingStrategy#FACTORY_TYPE}
 * setting by setting the fully qulified class name of the factory. Defaults to
 * the {@link DefaultPropertyNamingStrategyFactory}
 *
 * @author kimchy
 * @see PropertyNamingStrategy
 */
public interface PropertyNamingStrategyFactory {

    PropertyNamingStrategy createNamingStrategy(CompassSettings settings);
}
