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

package org.compass.core.converter.basic;

import java.util.Calendar;
import java.util.Date;

import org.compass.core.mapping.ResourcePropertyMapping;

public class CalendarConverter extends DateConverter {

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) {
        Date date = (Date) super.fromString(str, resourcePropertyMapping);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        Date date = ((Calendar) o).getTime();
        return super.toString(date, resourcePropertyMapping);
    }
}
