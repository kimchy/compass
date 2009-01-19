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

import org.compass.sample.petclinic.Pet;
import org.compass.sample.petclinic.Visit;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * JavaBean form controller that is used to add a new <code>Visit</code> to
 * the system.
 * 
 * @author Ken Krebs
 */

public class AddVisitForm extends AbstractClinicForm {

    public AddVisitForm() {
        // need a session to hold the formBackingObject
        setSessionForm(true);
    }

    /**
     * Method creates a new <code>Visit</code> with the correct
     * <code>Pet</code> info
     */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        Pet pet = getClinic().loadPet(RequestUtils.getRequiredIntParameter(request, "petId"));
        Visit visit = new Visit();
        pet.addVisit(visit);
        return visit;
    }

    /** Method inserts a new <code>Visit</code>. */

    protected ModelAndView onSubmit(Object command) throws ServletException {
        Visit visit = (Visit) command;
        // delegate the insert to the Business layer
        getClinic().storeVisit(visit);
        return new ModelAndView(getSuccessView(), "ownerId", visit.getPet().getOwner().getId());
    }

    protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return disallowDuplicateFormSubmission(request, response);
    }
}
