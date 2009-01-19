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

import org.compass.sample.petclinic.Owner;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * JavaBean Form controller that is used to edit an existing <code>Owner</code>.
 * 
 * @author Ken Krebs
 */

public class EditOwnerForm extends AbstractClinicForm {

    public EditOwnerForm() {
        // need a session to hold the formBackingObject
        setSessionForm(true);
        // initialize the form from the formBackingObject
        setBindOnNewForm(true);
    }

    /** Method forms a copy of an existing Owner for editing */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        // get the Owner referred to by id in the request
        return getClinic().loadOwner(RequestUtils.getRequiredIntParameter(request, "ownerId"));
    }

    /** Method updates an existing Owner. */
    protected ModelAndView onSubmit(Object command) throws ServletException {
        Owner owner = (Owner) command;
        // delegate the update to the Business layer
        getClinic().storeOwner(owner);
        return new ModelAndView(getSuccessView(), "ownerId", owner.getId());
    }
}
