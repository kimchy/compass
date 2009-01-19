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

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class JexlDynamicConverter extends AbstractDynamicConverter {

    private Expression expression;

    public void setExpression(String expression) throws ConversionException {
        try {
            this.expression = ExpressionFactory.createExpression(expression);
        } catch (Exception e) {
            throw new ConversionException("Failed to compile expression [" + expression + "]", e);
        }
    }

    protected Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        JexlContext jc = JexlHelper.createContext();
        jc.getVars().put(DATA_CONTEXT_KEY, o);
        try {
            return expression.evaluate(jc);
        } catch (Exception e) {
            throw new ConversionException("Failed to evaluate [" + o + "] with expression [" + expression + "]", e);
        }
    }
}
