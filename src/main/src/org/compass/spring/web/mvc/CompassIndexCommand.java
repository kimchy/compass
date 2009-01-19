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

package org.compass.spring.web.mvc;

/**
 * The command object which instructs the
 * {@link org.compass.spring.web.mvc.CompassIndexController} to perform
 * <code>CompassGps</code> index operation.
 * <p>
 * Since the index operation might result in heavy performance effects (re-index
 * all of <code>CompassGps</code>), the command proeprty <code>doIndex</code>
 * must be set to true in order to perform it.
 * 
 * @author kimchy
 */
public class CompassIndexCommand {

    private String doIndex;

    /**
     * Returns the <code>doIndex</code> commnand property, which instructs the
     * {@link CompassIndexController} to performs the index operation.
     * 
     * @return <code>true</code> if the index operation should be performed
     */
    public String getDoIndex() {
        return doIndex;
    }

    /**
     * Sets the <code>doIndex</code> commnand property, which instructs the
     * {@link CompassIndexController} to performs the index operation.
     * 
     * @param doIndex <code>true</code> if the index operation should be performed
     */
    public void setDoIndex(String doIndex) {
        this.doIndex = doIndex;
    }
}
