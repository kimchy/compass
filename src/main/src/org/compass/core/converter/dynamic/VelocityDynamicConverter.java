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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.StringBuilderWriter;

/**
 * @author kimchy
 */
public class VelocityDynamicConverter extends AbstractDynamicConverter {

    private String vtl;

    public void setExpression(String expression) throws ConversionException {
        this.vtl = expression;
        try {
            Velocity.init();
        } catch (Exception e) {
            throw new ConversionException("Failed to initialize velocity", e);
        }
    }

    protected Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        VelocityContext ctx = new VelocityContext();
        ctx.put(DATA_CONTEXT_KEY, o);

        StringBuilderWriter sw = StringBuilderWriter.Cached.cached();
        try {
            Velocity.evaluate(ctx, sw, "", vtl);
        } catch (Exception e) {
            throw new ConversionException("Failed to evaluate [" + o + "] with expression [" + vtl + "]", e);
        }
        return sw.toString();
    }
}
