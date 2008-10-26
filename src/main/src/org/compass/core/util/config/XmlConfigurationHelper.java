/*
 * Copyright 2004-2006 the original author or authors.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.compass.core.config.ConfigurationException;

/**
 * This is the default <code>ConfigurationHelper</code> implementation.
 */
public class XmlConfigurationHelper extends AbstractConfigurationHelper implements ConfigurationHelper, Serializable {

    private static final long serialVersionUID = 3546076943545219376L;

    /**
     * An empty (length zero) array of configuration objects.
     */
    protected static final ConfigurationHelper[] EMPTY_ARRAY = new ConfigurationHelper[0];

    private final String m_name;

    private final String m_location;

    private final String m_namespace;

    private final String m_prefix;

    private HashMap m_attributes;

    private ArrayList m_children;

    private String m_value;

    private boolean m_readOnly;

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public XmlConfigurationHelper(final String name) {
        this(name, null, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public XmlConfigurationHelper(final String name, final String location) {
        this(name, location, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public XmlConfigurationHelper(final String name, final String location, final String ns, final String prefix) {
        m_name = name;
        m_location = location;
        m_namespace = ns;
        m_prefix = prefix; // only used as a serialization hint. Cannot be null
    }

    /**
     * Returns the name of this configuration element.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the namespace of this configuration element
     */
    public String getNamespace() throws ConfigurationException {
        if (null != m_namespace) {
            return m_namespace;
        } else {
            throw new ConfigurationException("No namespace (not even default \"\") is associated with the "
                    + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns the prefix of the namespace
     */
    protected String getPrefix() throws ConfigurationException {
        if (null != m_prefix) {
            return m_prefix;
        } else {
            throw new ConfigurationException("No prefix (not even default \"\") is associated with the "
                    + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns a description of location of element.
     */
    public String getLocation() {
        return m_location;
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue(final String defaultValue) {
        if (null != m_value) {
            return m_value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue() throws ConfigurationException {
        if (null != m_value) {
            return m_value;
        } else {
            throw new ConfigurationException("No value is associated with the " + "configuration element \""
                    + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Return an array of all attribute names.
     */
    public String[] getAttributeNames() {
        if (null == m_attributes) {
            return new String[0];
        } else {
            return (String[]) m_attributes.keySet().toArray(new String[0]);
        }
    }

    /**
     * Return an array of <code>Configuration</code> elements containing all
     * node children.
     */
    public ConfigurationHelper[] getChildren() {
        if (null == m_children) {
            return new ConfigurationHelper[0];
        } else {
            return (ConfigurationHelper[]) m_children.toArray(new ConfigurationHelper[0]);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     */
    public String getAttribute(final String name) throws ConfigurationException {
        final String value = (null != m_attributes) ? (String) m_attributes.get(name) : null;
        if (null != value) {
            return value;
        } else {
            throw new ConfigurationException("No attribute named \"" + name + "\" is "
                    + "associated with the configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name.
     */
    public ConfigurationHelper getChild(final String name, final boolean createNew) {
        if (null != m_children) {
            final int size = m_children.size();
            for (int i = 0; i < size; i++) {
                final ConfigurationHelper configuration = (ConfigurationHelper) m_children.get(i);
                if (name.equals(configuration.getName())) {
                    return configuration;
                }
            }
        }
        if (createNew) {
            return new XmlConfigurationHelper(name, "<generated>" + getLocation(), m_namespace, m_prefix);
        } else {
            return null;
        }
    }

    /**
     * Return an array of <code>Configuration</code> objects children of this
     * associated with the given name. <br>
     * The returned array may be empty but is never <code>null</code>.
     */
    public ConfigurationHelper[] getChildren(final String name) {
        if (null == m_children) {
            return new ConfigurationHelper[0];
        } else {
            final ArrayList children = new ArrayList();
            final int size = m_children.size();
            for (int i = 0; i < size; i++) {
                final ConfigurationHelper configuration = (ConfigurationHelper) m_children.get(i);
                if (name.equals(configuration.getName())) {
                    children.add(configuration);
                }
            }
            return (ConfigurationHelper[]) children.toArray(new ConfigurationHelper[0]);
        }
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified string.
     */
    public void setValue(final String value) {
        checkWriteable();
        m_value = value;
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified int.
     */
    public void setValue(final int value) {
        setValue(String.valueOf(value));
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified long.
     */
    public void setValue(final long value) {
        setValue(String.valueOf(value));
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified boolean.
     */
    public void setValue(final boolean value) {
        setValue(String.valueOf(value));
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified float.
     */
    public void setValue(final float value) {
        setValue(String.valueOf(value));
    }

    /**
     * Set the value of the specified attribute to the specified string.
     */
    public void setAttribute(final String name, final String value) {
        checkWriteable();
        if (null != value) {
            if (null == m_attributes) {
                m_attributes = new HashMap();
            }
            m_attributes.put(name, value);
        } else {
            if (null != m_attributes) {
                m_attributes.remove(name);
            }
        }
    }

    /**
     * Set the value of the specified attribute to the specified int.
     */
    public void setAttribute(final String name, final int value) {
        setAttribute(name, String.valueOf(value));
    }

    /**
     * Set the value of the specified attribute to the specified long.
     */
    public void setAttribute(final String name, final long value) {
        setAttribute(name, String.valueOf(value));
    }

    /**
     * Set the value of the specified attribute to the specified boolean.
     */
    public void setAttribute(final String name, final boolean value) {
        setAttribute(name, String.valueOf(value));
    }

    /**
     * Set the value of the specified attribute to the specified float.
     */
    public void setAttribute(final String name, final float value) {
        setAttribute(name, String.valueOf(value));
    }

    /**
     * Add a child <code>Configuration</code> to this configuration element.
     */
    public void addChild(final ConfigurationHelper configuration) {
        checkWriteable();
        if (null == m_children) {
            m_children = new ArrayList();
        }
        m_children.add(configuration);
    }

    /**
     * Add all the attributes, children and value from specified configuration
     * element to current configuration element.
     */
    public void addAll(final ConfigurationHelper other) {
        checkWriteable();
        setValue(other.getValue(null));
        addAllAttributes(other);
        addAllChildren(other);
    }

    /**
     * Add all attributes from specified configuration element to current
     * configuration element.
     */
    public void addAllAttributes(final ConfigurationHelper other) {
        checkWriteable();
        final String[] attributes = other.getAttributeNames();
        for (int i = 0; i < attributes.length; i++) {
            final String name = attributes[i];
            final String value = other.getAttribute(name, null);
            setAttribute(name, value);
        }
    }

    /**
     * Add all child <code>Configuration</code> objects from specified
     * configuration element to current configuration element.
     */
    public void addAllChildren(final ConfigurationHelper other) {
        checkWriteable();
        final ConfigurationHelper[] children = other.getChildren();
        for (int i = 0; i < children.length; i++) {
            addChild(children[i]);
        }
    }

    /**
     * Add all child <code>Configuration</code> objects from specified
     * configuration element to current configuration element.
     */
    public void addAllChildrenBefore(final ConfigurationHelper other) {
        checkWriteable();
        final ConfigurationHelper[] children = other.getChildren();
        if (null == m_children) {
            m_children = new ArrayList();
        }
        for (int i = children.length - 1; i >= 0; i--) {
            m_children.add(0, children[i]);
        }
    }

    /**
     * Remove a child <code>Configuration</code> to this configuration
     * element.
     */
    public void removeChild(final ConfigurationHelper configuration) {
        checkWriteable();
        if (null == m_children) {
            return;
        }
        m_children.remove(configuration);
    }

    /**
     * Return count of children.
     */
    public int getChildCount() {
        if (null == m_children) {
            return 0;
        }
        return m_children.size();
    }

    /**
     * Make this configuration read-only.
     */
    public void makeReadOnly() {
        m_readOnly = true;
    }

    /**
     * heck if this configuration is writeable.
     */
    protected final void checkWriteable() throws IllegalStateException {
        if (m_readOnly) {
            throw new IllegalStateException("Configuration is read only and can not be modified");
        }
    }

    /**
     * Returns true iff this DefaultConfiguration has been made read-only.
     */
    protected final boolean isReadOnly() {
        return m_readOnly;
    }

    /**
     * Compare if this configuration is equal to another.
     */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof ConfigurationHelper))
            return false;
        return ConfigurationHelperUtil.equals(this, (ConfigurationHelper) other);
    }

    /**
     * Obtaine the hashcode for this configuration.
     */
    public int hashCode() {
        int hash = m_prefix.hashCode();
        if (m_name != null)
            hash ^= m_name.hashCode();
        hash >>>= 7;
        if (m_location != null)
            hash ^= m_location.hashCode();
        hash >>>= 7;
        if (m_namespace != null)
            hash ^= m_namespace.hashCode();
        hash >>>= 7;
        if (m_attributes != null)
            hash ^= m_attributes.hashCode();
        hash >>>= 7;
        if (m_children != null)
            hash ^= m_children.hashCode();
        hash >>>= 7;
        if (m_value != null)
            hash ^= m_value.hashCode();
        hash >>>= 7;
        hash ^= (m_readOnly) ? 1 : 3;
        return hash;
    }
}
