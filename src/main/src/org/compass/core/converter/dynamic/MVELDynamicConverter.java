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

package org.compass.core.converter.dynamic;

import java.util.HashMap;

import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

/**
 * @author kimchy
 */
public class MVELDynamicConverter extends AbstractDynamicConverter {

    private CompiledTemplate compiledTemplate;

    public void setExpression(String expression) throws ConversionException {
        this.compiledTemplate = TemplateCompiler.compileTemplate(expression);
    }

    protected Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(DATA_CONTEXT_KEY, o);
        return TemplateRuntime.execute(compiledTemplate, map);
    }
}