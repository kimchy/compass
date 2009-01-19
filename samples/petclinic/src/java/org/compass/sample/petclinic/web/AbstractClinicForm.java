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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compass.sample.petclinic.Clinic;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * JavaBean abstract base class for petclinic-aware form controllers. Provides
 * convenience methods for subclasses.
 * 
 * @author Ken Krebs
 */
public abstract class AbstractClinicForm extends SimpleFormController {

    private Clinic clinic;

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    protected Clinic getClinic() {
        return this.clinic;
    }

    public void afterPropertiesSet() {
        if (this.clinic == null) {
            throw new IllegalArgumentException("'clinic' is required");
        }
    }

    /**
     * Set up a custom property editor for the application's date format.
     */
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    /**
     * Method disallows duplicate form submission. Typically used to prevent
     * duplicate insertion of entities into the datastore. Shows a new form with
     * an error message.
     */
    protected ModelAndView disallowDuplicateFormSubmission(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        BindException errors = getErrorsForNewForm(request);
        errors.reject("duplicateFormSubmission", "Duplicate form submission");
        return showForm(request, response, errors);
    }
}
