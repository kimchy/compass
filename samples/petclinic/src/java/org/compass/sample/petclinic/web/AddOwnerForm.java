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

package org.compass.sample.petclinic.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compass.sample.petclinic.Owner;
import org.springframework.web.servlet.ModelAndView;

/**
 * JavaBean form controller that is used to add a new <code>Owner</code> to
 * the system.
 * 
 * @author Ken Krebs
 */
public class AddOwnerForm extends AbstractClinicForm {

    public AddOwnerForm() {
        // OK to start with a blank command object
        setCommandClass(Owner.class);
        // activate session form mode to allow for detection of duplicate
        // submissions
        setSessionForm(true);
    }

    /** Method inserts a new <code>Owner</code>. */
    protected ModelAndView onSubmit(Object command) throws ServletException {
        Owner owner = (Owner) command;
        // delegate the insert to the Business layer
        getClinic().storeOwner(owner);
        return new ModelAndView(getSuccessView(), "ownerId", owner.getId());
    }

    protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return disallowDuplicateFormSubmission(request, response);
    }
}
