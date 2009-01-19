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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.compass.core.config.ConfigurationException;

/**
 * This is the default <code>ConfigurationHelper</code> implementation.
 */
public class PlainConfigurationHelper extends AbstractConfigurationHelper implements ConfigurationHelper, Serializable {

    private static final long serialVersionUID = 3546076943545219376L;

    /**
     * An empty (length zero) array of configuration objects.
     */
    protected static final ConfigurationHelper[] EMPTY_ARRAY = new ConfigurationHelper[0];

    private final String name;

    private final String location;

    private final String namespace;

    private final String prefix;

    private HashMap<String, String> attributes;

    private ArrayList<ConfigurationHelper> children;

    private String value;

    private boolean readOnly;

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public PlainConfigurationHelper(final String name) {
        this(name, null, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public PlainConfigurationHelper(final String name, final String location) {
        this(name, location, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     */
    public PlainConfigurationHelper(final String name, final String location, final String ns, final String prefix) {
        this.name = name;
        this.location = location;
        namespace = ns;
        this.prefix = prefix; // only used as a serialization hint. Cannot be null
    }

    /**
     * Returns the name of this configuration element.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace of this configuration element
     */
    public String getNamespace() throws ConfigurationException {
        if (null != namespace) {
            return namespace;
        } else {
            throw new ConfigurationException("No namespace (not even default \"\") is associated with the "
                    + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns the prefix of the namespace
     */
    protected String getPrefix() throws ConfigurationException {
        if (null != prefix) {
            return prefix;
        } else {
            throw new ConfigurationException("No prefix (not even default \"\") is associated with the "
                    + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns a description of location of element.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue(final String defaultValue) {
        if (null != value) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public String getAttributeOrValue(String paramName) {
        String retVal = getAttribute(paramName, null);
        if (retVal != null) {
            return retVal;
        }
        return getValue();
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue() throws ConfigurationException {
        if (null != value) {
            return value;
        } else {
            throw new ConfigurationException("No value is associated with the " + "configuration element \""
                    + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Return an array of all attribute names.
     */
    public String[] getAttributeNames() {
        if (null == attributes) {
            return new String[0];
        } else {
            return attributes.keySet().toArray(new String[attributes.keySet().size()]);
        }
    }

    /**
     * Return an array of <code>Configuration</code> elements containing all
     * node children.
     */
    public ConfigurationHelper[] getChildren() {
        if (null == children) {
            return new ConfigurationHelper[0];
        } else {
            return children.toArray(new ConfigurationHelper[children.size()]);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     */
    public String getAttribute(final String name) throws ConfigurationException {
        final String value = (null != attributes) ? attributes.get(name) : null;
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
        if (null != children) {
            final int size = children.size();
            for (int i = 0; i < size; i++) {
                final ConfigurationHelper configuration = children.get(i);
                if (name.equals(configuration.getName())) {
                    return configuration;
                }
            }
        }
        if (createNew) {
            return new PlainConfigurationHelper(name, "<generated>" + getLocation(), namespace, prefix);
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
        if (null == children) {
            return new ConfigurationHelper[0];
        } else {
            final ArrayList<ConfigurationHelper> children = new ArrayList<ConfigurationHelper>();
            final int size = this.children.size();
            for (int i = 0; i < size; i++) {
                final ConfigurationHelper configuration = this.children.get(i);
                if (name.equals(configuration.getName())) {
                    children.add(configuration);
                }
            }
            return children.toArray(new ConfigurationHelper[children.size()]);
        }
    }

    public ConfigurationHelper[] getChildren(String... names) {
        final ArrayList<ConfigurationHelper> children = new ArrayList<ConfigurationHelper>();
        for (String name : names) {
            children.addAll(Arrays.asList(getChildren(name)));
        }
        return children.toArray(new ConfigurationHelper[children.size()]);
    }

    /**
     * Set the value of this <code>Configuration</code> object to the
     * specified string.
     */
    public void setValue(final String value) {
        checkWriteable();
        this.value = value;
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
            if (null == attributes) {
                attributes = new HashMap<String, String>();
            }
            attributes.put(name, value);
        } else {
            if (null != attributes) {
                attributes.remove(name);
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
        if (null == children) {
            children = new ArrayList<ConfigurationHelper>();
        }
        children.add(configuration);
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
        for (final String name : attributes) {
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
        for (ConfigurationHelper aChildren : children) {
            addChild(aChildren);
        }
    }

    /**
     * Add all child <code>Configuration</code> objects from specified
     * configuration element to current configuration element.
     */
    public void addAllChildrenBefore(final ConfigurationHelper other) {
        checkWriteable();
        final ConfigurationHelper[] children = other.getChildren();
        if (null == this.children) {
            this.children = new ArrayList<ConfigurationHelper>();
        }
        for (int i = children.length - 1; i >= 0; i--) {
            this.children.add(0, children[i]);
        }
    }

    /**
     * Remove a child <code>Configuration</code> to this configuration
     * element.
     */
    public void removeChild(final ConfigurationHelper configuration) {
        checkWriteable();
        if (null == children) {
            return;
        }
        children.remove(configuration);
    }

    /**
     * Return count of children.
     */
    public int getChildCount() {
        if (null == children) {
            return 0;
        }
        return children.size();
    }

    /**
     * Make this configuration read-only.
     */
    public void makeReadOnly() {
        readOnly = true;
    }

    /**
     * heck if this configuration is writeable.
     */
    protected final void checkWriteable() throws IllegalStateException {
        if (readOnly) {
            throw new IllegalStateException("Configuration is read only and can not be modified");
        }
    }

    /**
     * Returns true iff this DefaultConfiguration has been made read-only.
     */
    protected final boolean isReadOnly() {
        return readOnly;
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
        int hash = prefix.hashCode();
        if (name != null)
            hash ^= name.hashCode();
        hash >>>= 7;
        if (location != null)
            hash ^= location.hashCode();
        hash >>>= 7;
        if (namespace != null)
            hash ^= namespace.hashCode();
        hash >>>= 7;
        if (attributes != null)
            hash ^= attributes.hashCode();
        hash >>>= 7;
        if (children != null)
            hash ^= children.hashCode();
        hash >>>= 7;
        if (value != null)
            hash ^= value.hashCode();
        hash >>>= 7;
        hash ^= (readOnly) ? 1 : 3;
        return hash;
    }
}
