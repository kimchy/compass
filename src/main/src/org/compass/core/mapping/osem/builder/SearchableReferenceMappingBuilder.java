package org.compass.core.mapping.osem.builder;

import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.osem.ReferenceMapping;

/**
 * @author kimchy
 */
public class SearchableReferenceMappingBuilder {

    final ReferenceMapping mapping;

    public SearchableReferenceMappingBuilder(String name) {
        mapping = new ReferenceMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    public SearchableReferenceMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    public SearchableReferenceMappingBuilder refCompoenntAlias(String alias) {
        mapping.setRefCompAlias(alias);
        return this;
    }

    public SearchableReferenceMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    public SearchableReferenceMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public SearchableReferenceMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }

    /**
     * This reference mapping (only in case of collection) will be lazy  or not. By default
     * will be set by the global setting {@link org.compass.core.config.CompassEnvironment.Osem#LAZY_REFERNCE}.
     */
    public SearchableReferenceMappingBuilder lazy(boolean lazy) {
        mapping.setLazy(lazy);
        return this;
    }
}
