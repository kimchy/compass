package org.compass.core.mapping.osem.builder;

import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.osem.ReferenceMapping;

/**
 * @author kimchy
 */
public class ClassReferenceMappingBuilder {

    final ReferenceMapping mapping;

    public ClassReferenceMappingBuilder(String name) {
        mapping = new ReferenceMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    public ClassReferenceMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    public ClassReferenceMappingBuilder refCompoenntAlias(String alias) {
        mapping.setRefCompAlias(alias);
        return this;
    }

    public ClassReferenceMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public ClassReferenceMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }

    /**
     * This reference mapping (only in case of collection) will be lazy  or not. By default
     * will be set by the global setting {@link org.compass.core.config.CompassEnvironment.Osem#LAZY_REFERNCE}.
     */
    public ClassReferenceMappingBuilder lazy(boolean lazy) {
        mapping.setLazy(lazy);
        return this;
    }
}
