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
import javax.servlet.http.HttpServletResponse;

import org.compass.sample.petclinic.Clinic;
import org.compass.sample.petclinic.Owner;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 * 
 * @author Ken Krebs
 */
public class ClinicController extends MultiActionController implements InitializingBean {

    /** Holds value of property clinic. */
    private Clinic clinic;

    /**
     * Setter for property clinic.
     * 
     * @param clinic
     *            New value of property clinic.
     */
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public void afterPropertiesSet() throws Exception {
        if (clinic == null)
            throw new ApplicationContextException("Must set clinic bean property on " + getClass());
    }

    // handlers
    /**
     * 
     * Custom handler for welcome
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @return a ModelAndView to render the response
     */

    public ModelAndView welcomeHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        return new ModelAndView("welcomeView");
    }

    /**
     * 
     * Custom handler for tutorial
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @return a ModelAndView to render the response
     */
    public ModelAndView tutorialHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        return new ModelAndView("tutorialView");
    }

    /**
     * 
     * Custom handler for tutorial
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @return a ModelAndView to render the response
     */
    public ModelAndView springTutorialHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        return new ModelAndView("springTutorialView");
    }

    /**
     * Custom handler for vets display
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * 
     * @return a ModelAndView to render the response
     */
    public ModelAndView vetsHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        return new ModelAndView("vetsView", "vets", clinic.getVets());
    }

    /**
     * 
     * Custom handler for owner display
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @return a ModelAndView to render the response
     */

    public ModelAndView ownerHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Owner owner = clinic.loadOwner(RequestUtils.getIntParameter(request, "ownerId", 0));
        if (owner == null) {
            return new ModelAndView("findOwnersRedirect");
        }

        Map model = new HashMap();
        model.put("owner", owner);
        return new ModelAndView("ownerView", "model", model);
    }
}
