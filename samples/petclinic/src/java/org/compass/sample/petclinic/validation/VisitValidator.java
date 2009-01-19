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

package org.compass.sample.petclinic.validation;

import org.compass.sample.petclinic.Visit;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * <code>Validator</code> for <code>Visit</code> forms.
 * 
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class VisitValidator implements Validator {

    public boolean supports(Class clazz) {
        return Visit.class.isAssignableFrom(clazz);
    }

    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "description", "required", "required");
    }
}
