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

package org.compass.annotations.test.subindexhash;

import org.compass.annotations.SearchSetting;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.SearchableSubIndexHash;
import org.compass.core.engine.subindex.ModuloSubIndexHash;

/**
 * @author kimchy
 */
@Searchable
@SearchableSubIndexHash(value = ModuloSubIndexHash.class,
        settings = {@SearchSetting(name = "prefix", value = "index"), @SearchSetting(name = "size", value = "2")})
public class A {

    @SearchableId
    Integer id;

    @SearchableProperty
    String value;
}
