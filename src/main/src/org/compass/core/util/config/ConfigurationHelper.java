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

package org.compass.core.util.config;

import org.compass.core.config.ConfigurationException;

public interface ConfigurationHelper {

    /**
     * Return the name of the node.
     */
    String getName();

    /**
     * Return a string describing location of Configuration. Location can be
     * different for different mediums (ie "file:line" for normal XML files or
     * "table:primary-key" for DB based configurations);
     */
    String getLocation();
    
    /**
     * Return a new <code>Configuration</code> instance encapsulating the
     * specified child node.
     */
    ConfigurationHelper getChild(String child);

    /**
     * Return a <code>Configuration</code> instance encapsulating the
     * specified child node.
     */
    ConfigurationHelper getChild(String child, boolean createNew);

    /**
     * Return an <code>Array</code> of <code>Configuration</code> elements
     * containing all node children. The array order will reflect the order in
     * the source config file.
     */
    ConfigurationHelper[] getChildren();

    /**
     * Return an <code>Array</code> of <code>Configuration</code> elements
     * containing all node children with the specified name. The array order
     * will reflect the order in the source config file.
     */
    ConfigurationHelper[] getChildren(String name);

    ConfigurationHelper[] getChildren(String ... names);

    /**
     * Return an array of all attribute names.
     */
    String[] getAttributeNames();

    /**
     * Return the value of specified attribute.
     */
    String getAttribute(String paramName) throws ConfigurationException;

    /**
     * Return the <code>int</code> value of the specified attribute contained
     * in this node.
     */
    int getAttributeAsInteger(String paramName) throws ConfigurationException;

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     */
    long getAttributeAsLong(String name) throws ConfigurationException;

    /**
     * Return the <code>float</code> value of the specified parameter
     * contained in this node.
     */
    float getAttributeAsFloat(String paramName) throws ConfigurationException;

    /**
     * Return the <code>boolean</code> value of the specified parameter
     * contained in this node.
     */
    boolean getAttributeAsBoolean(String paramName) throws ConfigurationException;

    /**
     * First tries to get the attribute based on hte parameter, and there is none, will return
     * the value.
     */
    String getAttributeOrValue(String paramName);

    /**
     * Return the <code>String</code> value of the node.
     */
    String getValue() throws ConfigurationException;

    /**
     * Return the <code>int</code> value of the node.
     */
    int getValueAsInteger() throws ConfigurationException;

    /**
     * Return the <code>float</code> value of the node.
     */
    float getValueAsFloat() throws ConfigurationException;

    /**
     * Return the <code>boolean</code> value of the node.
     */
    boolean getValueAsBoolean() throws ConfigurationException;

    /**
     * Return the <code>long</code> value of the node.
     */
    long getValueAsLong() throws ConfigurationException;

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     * If the configuration value is not set, the default value will be used.
     */
    String getValue(String defaultValue);

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     * If the configuration value is not set, the default value will be used.
     */
    int getValueAsInteger(int defaultValue);

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     * If the configuration value is not set, the default value will be used.
     */
    long getValueAsLong(long defaultValue);

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     * If the configuration value is not set, the default value will be used.
     */
    float getValueAsFloat(float defaultValue);

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     * If the configuration value is not set, the default value will be used.
     */
    boolean getValueAsBoolean(boolean defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>, or the default value if no attribute by that name
     * exists or is empty.
     */
    String getAttribute(String name, String defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>int</code>, or the default value if no attribute by that name
     * exists or is empty.
     */
    int getAttributeAsInteger(String name, int defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>, or the default value if no attribute by that name
     * exists or is empty.
     */
    long getAttributeAsLong(String name, long defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>, or the default value if no attribute by that name
     * exists or is empty.
     */
    float getAttributeAsFloat(String name, float defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>, or the default value if no attribute by that
     * name exists or is empty.
     */
    boolean getAttributeAsBoolean(String name, boolean defaultValue);

    void makeReadOnly();
}
