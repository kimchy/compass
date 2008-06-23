package org.compass.core.mapping;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ResourcePropertyConverter;

/**
 * A simple lookup class, for a given path, will provide simple access to
 * it's path and value converter. Also supports path escaping ('a.b' or will
 * result in a.b and not alias a and resource property b).
 */
public final class ResourcePropertyLookup {

    private AliasMapping aliasMapping;

    private ResourcePropertyMapping resourcePropertyMapping;

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private String lookupName;

    private String path;

    private String dotPathAlias;

    private boolean convertOnlyWithDotPath = true;
    
    private CompassMapping compassMapping;

    public ResourcePropertyLookup(CompassMapping compassMapping, String name) {
        this.compassMapping = compassMapping;
        this.lookupName = name;
        // the path is escaped, so don't try to look it up
        if (name.charAt(0) == '\'' && name.charAt(name.length() - 1) == '\'') {
            path = name.substring(1, name.length() - 1);
        } else {
            int dotIndex = name.indexOf('.');
            if (dotIndex != -1) {
                dotPathAlias = name.substring(0, dotIndex);
                aliasMapping = compassMapping.getAliasMapping(dotPathAlias);
            }
            this.resourcePropertyMapping = compassMapping.getResourcePropertyMappingByPath(name);
            if (resourcePropertyMapping == null) {
                path = name;
            } else {
                path = resourcePropertyMapping.getPath().getPath();
            }
            resourcePropertyMappings = compassMapping.getResourcePropertyMappingsByPath(path);
            // did not find the resource mapping using "dot path", try and see if we can find a global one
            if (resourcePropertyMappings != null && resourcePropertyMapping == null) {
                resourcePropertyMapping = resourcePropertyMappings[0];
            }
        }
    }

    /**
     * Perform specialized convert only when dot path is used. Defaults to <code>true</code>.
     *
     * <p>Sometimes, several meta-data names are used with different converteres. For example
     * map to title both a pure String value and also a numeric value. If using dot path
     * notation, Compass will narrow down to the specfic converter (for example a.title.title).
     * When not using dot path notation, Compass now has two options for conversion. If this
     * flag is set to true (and not using dot path notation), Compass will use a converter based
     * on the object type. If this flag is set to false, the first mapping is used to convert.
     */
    public void setConvertOnlyWithDotPath(boolean convertOnlyWithDotPath) {
        this.convertOnlyWithDotPath = convertOnlyWithDotPath;
    }

    /**
     * Returns the analyzer associated with the resource property. <code>null</code> if none
     * is configured on the resource property or resource level.
     */
    public String getAnalyzer() {
        if (resourcePropertyMapping != null) {
            if (resourcePropertyMapping.getAnalyzer() != null) {
                return resourcePropertyMapping.getAnalyzer();
            }
        }
        return null;
    }

    /**
     * Returns the lookup name used in order to find the meta-data/property name.
     */
    public String getLookupName() {
        return lookupName;
    }

    /**
     * Returns the alias used if using dot path notation. Returns <code>null</code> if dot path notation
     * was not used.
     */
    public String getDotPathAlias() {
        return dotPathAlias;
    }

    /**
     * Returns the alias mapping if using dot path notation. Returns <code>null</code> if dot path notation
     * was not used.
     */
    public AliasMapping getAliasMapping() {
        return aliasMapping;
    }

    /**
     * Returns the path matching the provided name. The path is the actual name used to store in
     * the index.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the property mapping for the provided name. If not using dot path notation, will
     * return the first one that match the "meta-data" name within all of Compass mappings.
     */
    public ResourcePropertyMapping getResourcePropertyMapping() {
        return resourcePropertyMapping;
    }

    /**
     * Returns a list of property mappings for the provided name. When not using "dot path" which
     * allows to narrows down to a specific property mapping, and a general meta-data name is used
     * (such as title), will return all the property mappings for it within Compass mappings.
     */
    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        if (resourcePropertyMappings == null && resourcePropertyMapping != null) {
            resourcePropertyMappings = new ResourcePropertyMapping[]{resourcePropertyMapping};
        }
        return resourcePropertyMappings;
    }

    /**
     * Returns <code>true</code> if there is a specific converter that can be used to convert the
     * value.
     *
     * <p>Note, when {@link #setConvertOnlyWithDotPath(boolean)} is set the <code>true</code>, and
     * the name passed to the lookup does not contain "dot notation", <code>false</code> will be returned.
     */
    public boolean hasSpecificConverter() {
        if (dotPathAlias == null && convertOnlyWithDotPath) {
            return false;
        }
        return resourcePropertyMapping != null && resourcePropertyMapping.getConverter() != null;
    }

    /**
     * Returns the String representation of the provided value Object. If {@link #hasSpecificConverter()}
     * return <code>true</code>, will use the first mapping definition for the given name in order to
     * convert it from Object to String. If it returns false, will use a Converter assigned to the given
     * parameter class. If a <code>String</code> is passed, will normalize it using {@link #normalizeString(String)}.
     *
     * @see org.compass.core.converter.mapping.ResourcePropertyConverter
     */
    public String getValue(Object value) {
        if (value instanceof String) {
            return normalizeString((String) value);
        }
        ResourcePropertyConverter converter = null;
        if (hasSpecificConverter()) {
            converter = resourcePropertyMapping.getResourcePropertyConverter();
        }
        if (converter == null) {
            converter = (ResourcePropertyConverter) compassMapping.getConverterLookup().lookupConverter(value.getClass());
        }
        return converter.toString(value, resourcePropertyMapping);
    }

    /**
     * Returns the Object converted from the String value. If the {@link #hasSpecificConverter()} returns
     * <code>true</code>, will use the first mapping definition for the given name in order to conver it
     * from String to Object. If it returns <code>false</code>, will use a Converter assigned to the
     * given parameter class.
     *
     * @see org.compass.core.converter.mapping.ResourcePropertyConverter
     */
    public Object fromString(String value) {
        ResourcePropertyConverter converter;
        if (hasSpecificConverter()) {
            converter = resourcePropertyMapping.getResourcePropertyConverter();
        } else {
            converter = (ResourcePropertyConverter) compassMapping.getConverterLookup().lookupConverter(value.getClass());
        }
        return converter.fromString(value, resourcePropertyMapping);
    }

    /**
     * Tries to normalize the string using {@link #normalizeString(String)}, and if it fails, will
     * return the original value.
     */
    public String attemptNormalizeString(String value) {
        try {
            return normalizeString(value);
        } catch (ConversionException e) {
            return value;
        }
    }

    /**
     * Normalizes a given String value to a (hopefully) String value that mathces the one stored in the
     * index.
     *
     * <p>If {@link #hasSpecificConverter()} return <code>false</code> (note {@link #setConvertOnlyWithDotPath(boolean)})
     * will simply return the given value.
     *
     * <p>If the {@link org.compass.core.converter.mapping.ResourcePropertyConverter} states that it should not be used
     * for normalization ({@link org.compass.core.converter.mapping.ResourcePropertyConverter#canNormalize()} returns
     * <code>false</code>), the provided value will be returned.
     *
     * <p>If none of the above happens, will convert it
     * {@link org.compass.core.converter.mapping.ResourcePropertyConverter#fromString(String, org.compass.core.mapping.ResourcePropertyMapping)}
     * and then {@link org.compass.core.converter.mapping.ResourcePropertyConverter#toString(Object, org.compass.core.mapping.ResourcePropertyMapping)}.
     */
    public String normalizeString(String value) throws ConversionException {
        if (!hasSpecificConverter()) {
            return value;
        }
        ResourcePropertyConverter converter = resourcePropertyMapping.getResourcePropertyConverter();
        if (converter == null) {
            return value;
        }
        if (!converter.canNormalize()) {
            return value;
        }
        return converter.toString(converter.fromString(value, resourcePropertyMapping), resourcePropertyMapping);
    }
}
