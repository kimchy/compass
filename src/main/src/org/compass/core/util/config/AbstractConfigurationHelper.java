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

/**
 * This is an abstract <code>Configuration</code> implementation that deals
 * with methods that can be abstracted away from underlying implementations.
 */
public abstract class AbstractConfigurationHelper implements ConfigurationHelper {

    /**
     * Returns the prefix of the namespace. This is only used as a serialization
     * hint, therefore is not part of the client API. It should be included in
     * all Configuration implementations though.
     * 
     * @return A non-null String (defaults to "")
     * @throws ConfigurationException
     *             if no prefix was defined (prefix is <code>null</code>.
     */
    protected abstract String getPrefix() throws ConfigurationException;

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
     * numbers begin with 0b, all other values are assumed to be decimal.
     */
    public int getValueAsInteger() throws ConfigurationException {
        final String value = getValue().trim();
        try {
            if (value.startsWith("0x")) {
                return Integer.parseInt(value.substring(2), 16);
            } else if (value.startsWith("0o")) {
                return Integer.parseInt(value.substring(2), 8);
            } else if (value.startsWith("0b")) {
                return Integer.parseInt(value.substring(2), 2);
            } else {
                return Integer.parseInt(value);
            }
        } catch (final Exception nfe) {
            final String message = "Cannot parse the value \"" + value
                    + "\" as an integer in the configuration element \"" + getName() + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
     * numbers begin with 0b, all other values are assumed to be decimal.
     */
    public int getValueAsInteger(final int defaultValue) {
        try {
            return getValueAsInteger();
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
     * numbers begin with 0b, all other values are assumed to be decimal.
     */
    public long getValueAsLong() throws ConfigurationException {
        final String value = getValue().trim();
        try {
            if (value.startsWith("0x")) {
                return Long.parseLong(value.substring(2), 16);
            } else if (value.startsWith("0o")) {
                return Long.parseLong(value.substring(2), 8);
            } else if (value.startsWith("0b")) {
                return Long.parseLong(value.substring(2), 2);
            } else {
                return Long.parseLong(value);
            }
        } catch (final Exception nfe) {
            final String message = "Cannot parse the value \"" + value + "\" as a long in the configuration element \""
                    + getName() + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
     * numbers begin with 0b, all other values are assumed to be decimal.
     */
    public long getValueAsLong(final long defaultValue) {
        try {
            return getValueAsLong();
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     */
    public float getValueAsFloat() throws ConfigurationException {
        final String value = getValue().trim();
        try {
            return Float.parseFloat(value);
        } catch (final Exception nfe) {
            final String message = "Cannot parse the value \"" + value
                    + "\" as a float in the configuration element \"" + getName() + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     */
    public float getValueAsFloat(final float defaultValue) {
        try {
            return getValueAsFloat();
        } catch (final ConfigurationException ce) {
            return (defaultValue);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean() throws ConfigurationException {
        final String value = getValue().trim();
        if (isTrue(value)) {
            return true;
        } else if (isFalse(value)) {
            return false;
        } else {
            final String message = "Cannot parse the value \"" + value
                    + "\" as a boolean in the configuration element \"" + getName() + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean(final boolean defaultValue) {
        try {
            return getValueAsBoolean();
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue(final String defaultValue) {
        try {
            return getValue();
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>. Hexadecimal numbers begin with 0x, Octal numbers
     * begin with 0o and binary numbers begin with 0b, all other values are
     * assumed to be decimal.
     */
    public int getAttributeAsInteger(final String name) throws ConfigurationException {
        final String value = getAttribute(name).trim();
        try {
            if (value.startsWith("0x")) {
                return Integer.parseInt(value.substring(2), 16);
            } else if (value.startsWith("0o")) {
                return Integer.parseInt(value.substring(2), 8);
            } else if (value.startsWith("0b")) {
                return Integer.parseInt(value.substring(2), 2);
            } else {
                return Integer.parseInt(value);
            }
        } catch (final Exception nfe) {
            final String message = "Cannot parse the value \"" + value + "\" as an integer in the attribute \"" + name
                    + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>. Hexadecimal numbers begin with 0x, Octal numbers
     * begin with 0o and binary numbers begin with 0b, all other values are
     * assumed to be decimal.
     */
    public int getAttributeAsInteger(final String name, final int defaultValue) {
        try {
            return getAttributeAsInteger(name);
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>. Hexadecimal numbers begin with 0x, Octal numbers
     * begin with 0o and binary numbers begin with 0b, all other values are
     * assumed to be decimal.
     */
    public long getAttributeAsLong(final String name) throws ConfigurationException {
        final String value = getAttribute(name);
        try {
            if (value.startsWith("0x")) {
                return Long.parseLong(value.substring(2), 16);
            } else if (value.startsWith("0o")) {
                return Long.parseLong(value.substring(2), 8);
            } else if (value.startsWith("0b")) {
                return Long.parseLong(value.substring(2), 2);
            } else {
                return Long.parseLong(value);
            }
        } catch (final Exception nfe) {
            final String message = "Cannot parse the value \"" + value + "\" as a long in the attribute \"" + name
                    + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>. Hexadecimal numbers begin with 0x, Octal numbers
     * begin with 0o and binary numbers begin with 0b, all other values are
     * assumed to be decimal.
     */
    public long getAttributeAsLong(final String name, final long defaultValue) {
        try {
            return getAttributeAsLong(name);
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     */
    public float getAttributeAsFloat(final String name) throws ConfigurationException {
        final String value = getAttribute(name);
        try {
            return Float.parseFloat(value);
        } catch (final Exception e) {
            final String message = "Cannot parse the value \"" + value + "\" as a float in the attribute \"" + name
                    + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     */
    public float getAttributeAsFloat(final String name, final float defaultValue) {
        try {
            return getAttributeAsFloat(name);
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(final String name) throws ConfigurationException {
        final String value = getAttribute(name);
        if (isTrue(value)) {
            return true;
        } else if (isFalse(value)) {
            return false;
        } else {
            final String message = "Cannot parse the value \"" + value + "\" as a boolean in the attribute \"" + name
                    + "\" at " + getLocation();
            throw new ConfigurationException(message);
        }
    }

    private boolean isTrue(final String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("1");
    }

    private boolean isFalse(final String value) {
        return value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off")
                || value.equalsIgnoreCase("0");
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(final String name, final boolean defaultValue) {
        try {
            return getAttributeAsBoolean(name);
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     */
    public String getAttribute(final String name, final String defaultValue) {
        try {
            return getAttribute(name);
        } catch (final ConfigurationException ce) {
            return defaultValue;
        }
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name. If no such child exists, a new one will
     * be created.
     */
    public ConfigurationHelper getChild(final String name) {
        return getChild(name, true);
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name.
     */
    public ConfigurationHelper getChild(final String name, final boolean createNew) {
        final ConfigurationHelper[] children = getChildren(name);
        if (children.length > 0) {
            return children[0];
        } else {
            if (createNew) {
                return new PlainConfigurationHelper(name, "-");
            } else {
                return null;
            }
        }
    }

    /**
     * The toString() operation is used for debugging information. It does not
     * create a deep reproduction of this configuration and all child
     * configurations, instead it displays the name, value, and location.
     * 
     * @return getName() + "::" + getValue() + ":@" + getLocation();
     */
    public String toString() {
        return getName() + "::" + getValue("<no value>") + ":@" + getLocation();
    }
}
