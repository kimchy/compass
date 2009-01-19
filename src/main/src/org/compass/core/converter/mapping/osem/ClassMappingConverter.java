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

package org.compass.core.converter.mapping.osem;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.CollectionResourceWrapper;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.mapping.osem.OsemMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.proxy.extractor.ProxyExtractorHelper;
import org.compass.core.util.reflection.ReflectionConstructor;
import org.compass.core.util.reflection.ReflectionFactory;

/**
 * @author kimchy
 */
public class ClassMappingConverter implements ResourceMappingConverter, CompassConfigurable {

    /**
     * Under this key within the context the root class mapping can be found.
     */
    public static final String ROOT_CLASS_MAPPING_KEY = "$rcmk";

    /**
     * Disable internal mappings is a marker within the context if internal mappings should not
     * be marshalled.
     *
     * <p>Internal mappings are disabled for inner components (not root classes) when support
     * unmarshall is set to <code>false</code>.
     */
    public static final String DISABLE_INTERNAL_MAPPINGS = "$dim";

    private static final Object DISABLE_INTERNAL_MAPPINGS_MARK = new Object();

    public static final String DISABLE_UID_MARSHALLING = "$disableUID";

    private Map<String, ReflectionConstructor> cachedConstructors = new ConcurrentHashMap<String, ReflectionConstructor>();

    private ProxyExtractorHelper proxyExtractorHelper;

    public void configure(CompassSettings settings) throws CompassException {
        proxyExtractorHelper = new ProxyExtractorHelper();
        proxyExtractorHelper.configure(settings);
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        if (root instanceof Resource) {
            Resource rootResource = (Resource) root;
            resource.copy(rootResource);
            ((InternalResource) resource).addUID();
            return true;
        }

        // first store some important original context
        Object disableInternalMappings = context.getAttribute(DISABLE_INTERNAL_MAPPINGS);

        // perform the unmarshalling
        boolean store = doMarshall(resource, root, mapping, context);

        // restore the context
        context.setAttribute(DISABLE_INTERNAL_MAPPINGS, disableInternalMappings);

        return store;
    }

    protected boolean doMarshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        ClassMapping classMapping = (ClassMapping) mapping;

        root = proxyExtractorHelper.initializeProxy(root);

        Object disableInternalMapings = context.getAttribute(DISABLE_INTERNAL_MAPPINGS);

        // Note that even if a component is root, it will not be root when
        // treated as a component (the binding part of the configuration takes
        // care and "unroots" it)
        if (classMapping.isRoot()) {
            doSetBoost(resource, root, classMapping, context);
            context.setAttribute(ROOT_CLASS_MAPPING_KEY, classMapping);
            context.setAttribute(DISABLE_INTERNAL_MAPPINGS, null);
        } else {
            if (!classMapping.isSupportUnmarshall()) {
                context.setAttribute(DISABLE_INTERNAL_MAPPINGS, DISABLE_INTERNAL_MAPPINGS_MARK);
            }
        }

        // handle null values
        if (root == null) {
            if (!classMapping.isSupportUnmarshall()) {
                return false;
            }
            if (!context.handleNulls()) {
                return false;
            }
            if (classMapping.getIdMappings().length == 0) {
                throw new ConversionException("Component mapping [" + classMapping.getAlias() +
                        "] used within a collection/array and has null value, in such cases please define at least one id mapping on it");
            }
            // go over all the ids and put a null value in it (just so we keep the order)
            boolean store = false;
            for (Mapping id : classMapping.getResourceIdMappings()) {
                store |= id.getConverter().marshall(resource, context.getResourceFactory().getNullValue(), id, context);
            }
            return store;
        }

        if (classMapping.isPoly() && classMapping.getPolyClass() == null) {
            // store the poly class only for root mappings when we don't support unmarshalling
            // and for all classes when we do support unmarshalling
            if (classMapping.isSupportUnmarshall() || classMapping.isRoot()) {
                storePolyClass(resource, root, classMapping, context);
            }
        }

        if (classMapping.isSupportUnmarshall() && root.getClass().isEnum()) {
            storeEnumName(resource, root, classMapping, context);
        }

        // check if we already marshalled this objecy under this alias
        // if we did, there is no need to completly marhsall it again

        // When we support *do* unmarshall, it is important that theses will have ids, since based
        // on them we will unmarshall correctly

        // When we *do not* support unmarshall, we don't care about ids. Therefore, we can also mark
        // marshalled based on object identity and support cyclic support for components without ids.
        if (classMapping.getIdMappings().length > 0) {
            IdsAliasesObjectKey idObjKey = new IdsAliasesObjectKey(classMapping, root);
            if (!idObjKey.hasNullId) {
                Object marshalled = context.getMarshalled(idObjKey);
                if (marshalled != null) {
                    // we already marshalled this object, if we don't support unmarhsall, just return
                    // otherwise only marshall its ids and return
                    if (!classMapping.isSupportUnmarshall()) {
                        return true;
                    }
                    if (classMapping.isFilterDuplicates()) {
                        Mapping[] ids = classMapping.getIdMappings();
                        boolean store = false;
                        for (int i = 0; i < ids.length; i++) {
                            store |= ids[i].getConverter().marshall(resource, idObjKey.idsValues[i], ids[i], context);
                        }
                        return store;
                    }
                } else {
                    // we did not marshall this object, cache it for later checks
                    context.setMarshalled(idObjKey, root);
                    if (!classMapping.isSupportUnmarshall()) {
                        context.setMarshalled(new IdentityAliasedObjectKey(classMapping.getAlias(), root), root);
                    }
                }
            }
        } else if (!classMapping.isSupportUnmarshall()) {
            IdentityAliasedObjectKey key = new IdentityAliasedObjectKey(classMapping.getAlias(), root);
            Object marshalled = context.getMarshalled(key);
            if (marshalled != null) {
                return true;
            }
            context.setMarshalled(key, root);
        }

        // perform full marshalling of the object into the resource
        boolean store = false;
        for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, root);
            OsemMapping m = (OsemMapping) mappingsIt.next();
            Object value;
            if (m.hasAccessors()) {
                Getter getter = ((ObjectMapping) m).getGetter();
                value = getter.get(root);
            } else {
                value = root;
            }
            store |= m.getConverter().marshall(resource, value, m, context);
        }

        // marshall the uid last
        if (classMapping.isRoot() && !context.hasAttribute(DISABLE_UID_MARSHALLING)) {
            ((InternalResource) resource).addUID();
        }

        // resotore the disable intenral mappings flag
        context.setAttribute(DISABLE_INTERNAL_MAPPINGS, disableInternalMapings);

        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ClassMapping classMapping = (ClassMapping) mapping;
        ResourceKey resourceKey = null;
        // handle a cache of all the unmarshalled objects already, used for
        // cyclic references when using reference mappings or component mappings
        // with ids
        if (classMapping.isRoot()) {
            if (!classMapping.isSupportUnmarshall()) {
                // we don't support unmarshalling, try and unmarshall just the object with its
                // ids set.
                Object obj = constructObjectForUnmarshalling(classMapping, resource, context);
                for (Mapping id : classMapping.getIdMappings()) {
                    Object idValue = id.getConverter().unmarshall(resource, id, context);
                    if (idValue == null) {
                        // was not marshalled, simply return null
                        return null;
                    }
                    if (((ObjectMapping) id).getSetter() != null) {
                        ((ObjectMapping) id).getSetter().set(obj, idValue);
                    }
                }
                return obj;
            }
            resourceKey = ((InternalResource) resource).getResourceKey();
            // if it is cached, return the cached object
            Object cached = context.getUnmarshalled(resourceKey);
            if (cached != null) {
                return cached;
            }
        } else if (classMapping.getIdMappings().length > 0) {
            // if the class mapping has ids, try and get it from the resource.
            Property[] propIds = ResourceHelper.toIds(resource, classMapping, false);
            if (propIds != null) {
                resourceKey = new ResourceKey(classMapping, propIds);
                // if it is cached, return the cached object
                Object cached = context.getUnmarshalled(resourceKey);
                if (cached != null) {
                    return cached;
                }
                // if we do have values in the ids, but all of them are null, it means that we
                // marked a null object
                boolean nullClass = true;
                for (Property propId : propIds) {
                    if (!context.getResourceFactory().isNullValue(propId.getStringValue())) {
                        nullClass = false;
                    }
                }
                if (nullClass) {
                    return null;
                }
                // if it is not cached, we need to rollback the fact that we read
                // the ids, so the rest of the unmarshalling process will work
                if (resource instanceof CollectionResourceWrapper) {
                    CollectionResourceWrapper colWrapper = (CollectionResourceWrapper) resource;
                    for (Mapping id : classMapping.getResourceIdMappings()) {
                        colWrapper.rollbackGetProperty(id.getPath().getPath());
                    }
                }
            }
        }
        // not root class mapping and does not support unamrhsalling, simply return null
        if (!classMapping.isSupportUnmarshall()) {
            return null;
        }

        Object obj = constructObjectForUnmarshalling(classMapping, resource, context);
        context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, obj);
        // we will set here the object, even though no ids have been set,
        // since the ids are the first mappings that will be unmarshalled,
        // and it's all we need to handle cyclic refernces in case of
        // references or components with ids
        if (resourceKey != null) {
            context.setUnmarshalled(resourceKey, obj);
            if (classMapping.isRoot()) {
                context.getSession().getFirstLevelCache().set(resourceKey, obj);
            }
        }

        boolean isNullClass = true;
        for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, obj);
            OsemMapping m = (OsemMapping) mappingsIt.next();
            if (m.hasAccessors()) {
                Setter setter = ((ObjectMapping) m).getSetter();
                if (setter == null) {
                    continue;
                }
                Object value = m.getConverter().unmarshall(resource, m, context);
                if (value == null) {
                    continue;
                }
                setter.set(obj, value);
                if (m.controlsObjectNullability()) {
                    isNullClass = false;
                }
            } else {
                m.getConverter().unmarshall(resource, m, context);
            }
        }
        if (isNullClass) {
            return null;
        }
        return obj;
    }

    /**
     * Constructs the object used for unmarshalling (no properties are set/unmarshalled) on it.
     * <code>null</code> return value denotes no un-marshalling should be performed.
     */
    protected Object constructObjectForUnmarshalling(ClassMapping classMapping, Resource resource, MarshallingContext context) throws ConversionException {
        // resolve the actual class and constructor
        Class clazz = classMapping.getClazz();
        ReflectionConstructor constructor = classMapping.getConstructor();
        if (classMapping.isPoly()) {
            if (classMapping.getPolyClass() != null) {
                clazz = classMapping.getPolyClass();
                constructor = classMapping.getPolyConstructor();
            } else {
                Property pClassName = resource.getProperty(classMapping.getClassPath().getPath());
                if (pClassName == null) {
                    // if not poly class is stored, this means that it is probably a null class stored.
                    return null;
                }
                String className = pClassName.getStringValue();
                if (className == null) {
                    // if not poly class is stored, this means that it is probably a null class stored.
                    return null;
                }
                constructor = cachedConstructors.get(className);
                if (constructor == null) {
                    try {
                        clazz = ClassUtils.forName(className, context.getSession().getCompass().getSettings().getClassLoader());
                    } catch (ClassNotFoundException e) {
                        throw new ConversionException("Failed to create class [" + className + "] for unmarshalling", e);
                    }
                    constructor = ReflectionFactory.getDefaultConstructor(context.getSession().getCompass().getSettings(), clazz);
                    cachedConstructors.put(className, constructor);
                }
            }
        }

        // create the object
        Object obj;
        if (clazz.isEnum()) {
            Property pEnumName = resource.getProperty(classMapping.getEnumNamePath().getPath());
            if (pEnumName == null) {
                return null;
            }
            String name = pEnumName.getStringValue();
            if (name == null) {
                return null;
            }
            obj = Enum.valueOf(clazz, name);
        } else {
            try {
                obj = constructor.newInstance();
            } catch (Exception e) {
                throw new ConversionException("Failed to create class [" + clazz.getName() + "] for unmarshalling", e);
            }
        }
        return obj;
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        ClassMapping classMapping = (ClassMapping) resourceMapping;
        boolean stored = false;
        Mapping[] ids = classMapping.getIdMappings();
        if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (Mapping rpId : ids) {
                ObjectMapping objectMapping = (ObjectMapping) rpId;
                stored |= convertId(classMapping, idResource, objectMapping.getGetter().get(id), rpId, context);
            }
        } else if (id instanceof Object[]) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load class with [" + Array.getLength(id)
                        + "] mappings while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                stored |= convertId(classMapping, idResource, Array.get(id, i), ids[i], context);
            }
        } else if (ids.length == 1) {
            stored = convertId(classMapping, idResource, id, ids[0], context);
        } else {
            String type = id.getClass().getName();
            throw new ConversionException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }

        if (!context.hasAttribute(DISABLE_UID_MARSHALLING)) {
            ((InternalResource) idResource).addUID();
        }


        return stored;
    }

    private boolean convertId(ResourceMapping resourceMapping, Resource resource, Object root, Mapping mapping, MarshallingContext context) {
        if (root == null) {
            throw new ConversionException("Trying to marshall a null id [" + mapping.getName() + "] for alias [" + resourceMapping.getAlias() + "]");
        }
        if ((root instanceof String) && (mapping instanceof ResourcePropertyMapping) && (mapping.getConverter() instanceof ResourcePropertyConverter)) {
            ResourcePropertyConverter converter = (ResourcePropertyConverter) mapping.getConverter();
            if (converter.canNormalize()) {
                root = converter.fromString((String) root, (ResourcePropertyMapping) mapping);
            }
        }
        return mapping.getConverter().marshall(resource, root, mapping, context);
    }

    public Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        ClassMapping classMapping = (ClassMapping) resourceMapping;
        Mapping[] ids = classMapping.getIdMappings();
        Object[] idsValues = new Object[ids.length];
        if (id instanceof Resource) {
            Resource resource = (Resource) id;
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = ids[i].getConverter().unmarshall(resource, ids[i], context);
                if (idsValues[i] == null) {
                    // the reference was not marshalled
                    return null;
                }
            }
        } else if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (int i = 0; i < ids.length; i++) {
                ObjectMapping objectMapping = (ObjectMapping) ids[i];
                idsValues[i] = objectMapping.getGetter().get(id);
            }
        } else if (id instanceof Object[]) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load class with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = Array.get(id, i);
            }
        } else if (ids.length == 1) {
            idsValues[0] = id;
        } else {
            String type = id.getClass().getName();
            throw new ConversionException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }
        return idsValues;
    }

    /**
     * A simple extension point that allows to set the boost value for the created {@link Resource}.
     * <p/>
     * The default implemenation uses the statically defined boost value in the mapping definition
     * ({@link org.compass.core.mapping.osem.ClassMapping#getBoost()}) to set the boost level
     * using {@link Resource#setBoost(float)}
     * <p/>
     * Note, that this method will only be called on a root level (root=true) mapping.
     *
     * @param resource     The resource to set the boost on
     * @param root         The Object that is marshalled into the respective Resource
     * @param classMapping The Class Mapping deifnition
     * @param context      The marshalling context
     * @throws ConversionException
     */
    protected void doSetBoost(Resource resource, Object root, ClassMapping classMapping,
                              MarshallingContext context) throws ConversionException {
        resource.setBoost(classMapping.getBoost());
    }

    /**
     * Stores the poly class name callback. Uses {@link #getPolyClassName(Object)} in order to get
     * the poly class and store it.
     */
    protected void storePolyClass(Resource resource, Object root, ClassMapping classMapping, MarshallingContext context) {
        String className = getPolyClassName(root);
        Property p = context.getResourceFactory().createProperty(classMapping.getClassPath().getPath(), className, Property.Store.YES,
                Property.Index.NOT_ANALYZED);
        p.setOmitNorms(true);
        p.setOmitTf(true);
        resource.addProperty(p);
    }

    /**
     * Stores the {@link Enum#name()} in order to construct it afterwards.
     */
    protected void storeEnumName(Resource resource, Object root, ClassMapping classMapping, MarshallingContext context) {
        String name = ((Enum) root).name();
        Property p = context.getResourceFactory().createProperty(classMapping.getEnumNamePath().getPath(), name, Property.Store.YES,
                Property.Index.NOT_ANALYZED);
        p.setOmitNorms(true);
        p.setOmitTf(true);
        resource.addProperty(p);
    }

    /**
     * An extension point allowing to get the poly class name if need to be stored.
     * By defaults uses {@link org.compass.core.util.proxy.extractor.ProxyExtractorHelper#getTargetClass(Object)}.
     */
    protected String getPolyClassName(Object root) {
        return proxyExtractorHelper.getTargetClass(root).getName();
    }

    /**
     * An object key based on the alias and the object identity hash code
     */
    protected static final class IdentityAliasedObjectKey {

        private String alias;

        private Integer objHashCode;

        private Object value;

        private int hashCode = Integer.MIN_VALUE;

        public IdentityAliasedObjectKey(String alias, Object value) {
            this.alias = alias;
            this.value = value;
            this.objHashCode = System.identityHashCode(value);
        }


        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof IdentityAliasedObjectKey)) {
                return false;
            }
            final IdentityAliasedObjectKey idObjKey = (IdentityAliasedObjectKey) other;
            return idObjKey.value == value && idObjKey.alias.equals(this.alias);
        }


        public int hashCode() {
            if (hashCode == Integer.MIN_VALUE) {
                hashCode = 13 * objHashCode.hashCode() + alias.hashCode();
            }
            return hashCode;
        }
    }

    /**
     * An object key based on the alias and its ids values
     */
    protected static final class IdsAliasesObjectKey {

        private String alias;

        private Object[] idsValues;

        private boolean hasNullId;

        private int hashCode = Integer.MIN_VALUE;

        public IdsAliasesObjectKey(ClassMapping classMapping, Object value) {
            this.alias = classMapping.getAlias();
            Mapping[] ids = classMapping.getIdMappings();
            idsValues = new Object[ids.length];
            for (int i = 0; i < ids.length; i++) {
                OsemMapping m = (OsemMapping) ids[i];
                if (m.hasAccessors()) {
                    idsValues[i] = ((ObjectMapping) m).getGetter().get(value);
                } else {
                    idsValues[i] = value;
                }
                if (idsValues[i] == null) {
                    hasNullId = true;
                }
            }
        }

        public Object[] getIdsValues() {
            return this.idsValues;
        }

        public boolean equals(Object other) {
            if (this == other)
                return true;

            if (!(other instanceof IdsAliasesObjectKey))
                return false;

            final IdsAliasesObjectKey key = (IdsAliasesObjectKey) other;
            if (!key.alias.equals(alias)) {
                return false;
            }

            for (int i = 0; i < idsValues.length; i++) {
                if (!key.idsValues[i].equals(idsValues[i])) {
                    return false;
                }
            }

            return true;
        }

        public int hashCode() {
            if (hashCode == Integer.MIN_VALUE) {
                hashCode = getHashCode();
            }
            return hashCode;
        }

        private int getHashCode() {
            int result = alias.hashCode();
            for (Object idValue : idsValues) {
                result = 29 * result + idValue.hashCode();
            }
            return result;
        }

        public boolean isHasNullId() {
            return hasNullId;
        }
    }
}
