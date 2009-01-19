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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.compass.sample.petclinic.Pet;
import org.compass.sample.petclinic.PetType;
import org.compass.sample.petclinic.util.EntityUtils;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * JavaBean Form controller that is used to edit an existing <code>Pet</code>.
 * 
 * @author Ken Krebs
 */
public class EditPetForm extends AbstractClinicForm {

    public EditPetForm() {
        // need a session to hold the formBackingObject
        setSessionForm(true);
        // initialize the form from the formBackingObject
        setBindOnNewForm(true);
    }

    protected Map referenceData(HttpServletRequest request) throws ServletException {
        Map refData = new HashMap();
        refData.put("types", getClinic().getPetTypes());
        return refData;
    }

    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        // get the Pet referred to by id in the request
        return getClinic().loadPet(RequestUtils.getRequiredIntParameter(request, "petId"));
    }

    protected void onBind(HttpServletRequest request, Object command) throws ServletException {
        Pet pet = (Pet) command;
        int typeId = RequestUtils.getRequiredIntParameter(request, "typeId");
        pet.setType((PetType) EntityUtils.getById(getClinic().getPetTypes(), PetType.class, typeId));
    }

    /** Method updates an existing Pet */
    protected ModelAndView onSubmit(Object command) throws ServletException {
        Pet pet = (Pet) command;
        // delegate the update to the business layer
        getClinic().storePet(pet);
        return new ModelAndView(getSuccessView(), "ownerId", pet.getOwner().getId());
    }
}
