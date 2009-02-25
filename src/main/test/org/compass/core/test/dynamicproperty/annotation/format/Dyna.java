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

package org.compass.core.test.dynamicproperty.annotation.format;

import org.compass.annotations.SearchableDynamicName;
import org.compass.annotations.SearchableDynamicValue;

/**
 * @author kimchy
 *
 */
public class Dyna {

    @SearchableDynamicName(format = "000")
    long name;

    @SearchableDynamicValue(format = "0000")
    long value;

    public Dyna() {
    }

    public Dyna(long name, long value) {
        this.name = name;
        this.value = value;
    }
}