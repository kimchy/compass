package org.compass.core.engine.naming;

/**
 * A naming strategy that uses {@link DynamicPropertyPath} when building
 * {@link PropertyPath}.
 *
 * @author kimchy
 * @author lexi
 * @see PropertyPath
 * @see DynamicPropertyPath
 * @see PropertyNamingStrategyFactory
 * @see DefaultPropertyNamingStrategyFactory
 */
public class DynamicPropertyNamingStrategy implements PropertyNamingStrategy {

    public boolean isInternal(String name) {
        return name.charAt(0) == '$';
    }

    public PropertyPath getRootPath() {
        return new StaticPropertyPath("$");
    }

    public PropertyPath buildPath(PropertyPath root, String name) {
        return new DynamicPropertyPath(root, name);
    }
}
