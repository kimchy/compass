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

package org.compass.core.converter.basic.format;

import java.text.Format;
import java.text.ParseException;

/**
 * A {@link org.compass.core.converter.basic.format.Formatter} implementation that uses
 * {@link java.text.Format}.
 *
 * @author kimchy
 */
public class TextFormatFormatter implements Formatter {

    private Format format;

    public TextFormatFormatter(Format format) {
        this.format = format;
    }

    public String format(Object obj) {
        return format.format(obj);
    }

    public Object parse(String str) throws ParseException {
        return format.parseObject(str);
    }

    public boolean isThreadSafe() {
        return false;
    }
}
