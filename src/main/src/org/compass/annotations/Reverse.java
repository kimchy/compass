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

package org.compass.annotations;

/**
 * Specifies whether and how the meta-data proeprty should value will be revered.
 *
 * @author kimchy
 */
public enum Reverse {
    /**
     * No reversing will happen on the meta-data value.
     */
    NO,

    /**
     * A special reader will wrap the actual string value,
     * and reverse it. More performant than the {@link #STRING}
     * option, but means that it will always be {@link Index#ANALYZED}
     * and {@link Store#NO}.
     */
    READER,

    /**
     * A new string will be used to hold the reverse value of the meta-data.
     * Less performant than the {@link #READER} option, but allows to control
     * the {@link Index} and the {@link Store}.
     */
    STRING
}
