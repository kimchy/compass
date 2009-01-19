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

package org.compass.core.marshall;

import org.compass.core.Resource;
import org.compass.core.mapping.ResourceMapping;

/**
 * Responsible for marhslling and unmarashlling high level objects (a.k.a root object)
 * to and from {@link Resource}.
 *
 * @author kimchy
 */
public interface MarshallingStrategy {

    /**
     * Unmarshalls the given resource to an Object based on the {@link ResourceMapping}
     * regsitered under the {@link Resource} alias.
     *
     * @param resource The resource to unmarshall from
     * @return The object unmarshalled from the resource
     */
    Object unmarshall(Resource resource);

    /**
     * Unmarshalls the given resource to an Object based on the {@link ResourceMapping}
     * regsitered under the {@link Resource} alias WITHIN the given marshalling context.
     *
     * @param resource The resource to unmarshall from
     * @param context  The context to unmarshall the resource within
     * @return The object unmarshalled from the resource
     */
    Object unmarshall(Resource resource, MarshallingContext context);

    /**
     * Marshalls the given Object into a {@link Resource} based on the {@link ResourceMapping}
     * associated with the provided alias. Returns <code>null</code> if there are no mappings.
     *
     * @param alias The alias to look up the {@link ResourceMapping}
     * @param root  The object to marshall into the resource
     * @return The resource result of marshalling the object or <code>null</code> if has no mapping
     */
    Resource marshall(String alias, Object root);

    /**
     * Marshalls the given Object into a {@link Resource} based on the {@link ResourceMapping}
     * associated with the provided object. If the object implements {@link org.compass.core.spi.AliasedObject},
     * the alias will be used to look up the {@link ResourceMapping}, otherwise, the object class will be used.
     *
     * @param root The object to marshall into a resource
     * @return The resource result of marshalling the object
     */
    Resource marshall(Object root);

    /**
     * <p>Marshalls the given id object into a Resource (a resource having only its ids set).
     * Note, that the id can be several types, depending on the mapping. For example, for
     * class mapping, it can be the root Object itself (with its ids set), an array of ids,
     * or if a single id, the actual id object.
     *
     * <p>The {@link ResourceMapping} are looked up based on the given object.
     *
     * <p>Will return <code>null</code> if no mappins are found
     *
     * @param id The id to marshall into a {@link Resource}
     * @return A resource having its id properties set
     */
    Resource marshallIds(Object id);

    /**
     * <p>Marshalls the give id object into a Resource (a resource having only its ids set).
     * Note, that the id can be several types, depending on the mapping. For example, for
     * class mapping, it can be the root Object itself (with its ids set), an array of ids,
     * or if a single id, the actual id object.
     *
     * <p>The {@link ResourceMapping} are looked up based on the given alias.
     *
     * <p>Will return <code>null</code> if no mappins are found
     *
     * @param alias The alias to look up the {@link ResourceMapping} based
     * @param id    The id to marshall into a {@link Resource}
     * @return A resource having its id properties set
     */
    Resource marshallIds(String alias, Object id);

    /**
     * <p>Marshalls the give id object into a Resource (a resource having only its ids set).
     * Note, that the id can be several types, depending on the mapping. For example, for
     * class mapping, it can be the root Object itself (with its ids set), an array of ids,
     * or if a single id, the actual id object.
     *
     * <p>The {@link ResourceMapping} are looked up based on the given class.
     *
     * <p>Will return <code>null</code> if no mappins are found
     *
     * @param clazz The class to look up the {@link ResourceMapping} based
     * @param id    The id to marshall into a {@link Resource}
     * @return A resource having its id properties set
     */
    Resource marshallIds(Class clazz, Object id);

    /**
     * Marshalls the give id object into a Resource (a resource having only its ids set).
     * Note, that the id can be several types, depending on the mapping. For example, for
     * class mapping, it can be the root Object itself (with its ids set), an array of ids,
     * or if a single id, the actual id object.
     *
     * @param resourceMapping The resource mapping holding how to marhsall the ids
     * @param id              The id to marshall into a {@link Resource}
     * @return A resource having its id properties set
     */
    Resource marshallIds(ResourceMapping resourceMapping, Object id);

    /**
     * Marshalls the give id object into the provided Resource (a resource having only its ids set).
     * Note, that the id can be several types, depending on the mapping. For example, for
     * class mapping, it can be the root Object itself (with its ids set), an array of ids,
     * or if a single id, the actual id object.
     *
     * @param resource        The resource to marhsll the ids into
     * @param resourceMapping The resource mapping holding how to marhsall the ids
     * @param id              The id to marshall into a {@link Resource}
     * @return <code>true</code> if stored properties were added to the {@link Resource}.
     */
    boolean marshallIds(Resource resource, ResourceMapping resourceMapping, Object id, MarshallingContext context);

    /**
     * Marhsalls the give id into the actual object. Kindda hacky... .
     *
     * @param root The object to marshall the ids into
     * @param id   The id to marshall into the root object
     */
    void marshallIds(Object root, Object id);

    /**
     * Marhsalls the give id into the actual object. Kindda hacky... .
     *
     * @param resourceMapping The resource mapping for the given object
     * @param root            The object to marshall the ids into
     * @param id              The id to marshall into the root object
     */
    void marshallIds(ResourceMapping resourceMapping, Object root, Object id);

    /**
     * Unmarshalls the given id object into an array of all the id values. The results depends
     * on the type of the mappings (raw resource/class).
     * <p/>
     * The unmarshalling is performed based on {@link ResourceMapping} associated with the given
     * alias.
     *
     * @param alias The alias to lookup the {@link ResourceMapping}
     * @param id    The id to unmarshall
     * @return An array of all the ids
     */
    Object[] unmarshallIds(String alias, Object id);

    /**
     * Unmarshalls the given id object into an array of all the id values. The results depends
     * on the type of the mappings (raw resource/class).
     * <p/>
     * The unmarshalling is performed based on {@link ResourceMapping} associated with the given
     * class.
     *
     * @param clazz The class to lookup the {@link ResourceMapping}
     * @param id    The id to unmarshall
     * @return An array of all the ids
     */
    Object[] unmarshallIds(Class clazz, Object id);

    /**
     * Unmarshalls the given id object into an array of all the id values. The results depends
     * on the type of the mappings (raw resource/class).
     * <p/>
     * The unmarshalling is performed based on {@link ResourceMapping} provided.
     *
     * @param resourceMapping The resource to perform the unmarshalling based on
     * @param id              The id to unmarshall
     * @return An array of all the ids
     */
    Object[] unmarshallIds(ResourceMapping resourceMapping, Object id, MarshallingContext context);

}
