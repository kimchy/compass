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

package org.compass.core;

/**
 * The query builder is used to construct
 * {@link CompassQueryFilter} programmatically. Simple
 * queries, like {@link #le(String, Object)}, will generate a
 * {@link CompassQueryFilter}. More complex ones, will
 * return their repective builder to continue and bulid them (like
 * {@link #bool()}).
 * <p/>
 * The generated filter can be set on the query itself using {@link CompassQuery#setFilter(CompassQueryFilter)}.
 *
 * @author kimchy
 */
public interface CompassQueryFilterBuilder {

    /**
     * A general interface for internal builders that will create a
     * {@link CompassQueryFilter}.
     *
     * @author kimchy
     */
    public static interface ToCompassQueryFilter {

        CompassQueryFilter toFilter();
    }

    /**
     * A boolean query filter builder. Used to construct query filter that will return hits
     * that are the matching boolean combinations of other queries.
     *
     * @author kimchy
     */
    public static interface CompassBooleanQueryFilterBuilder extends ToCompassQueryFilter {

        CompassBooleanQueryFilterBuilder and(CompassQueryFilter filter);

        CompassBooleanQueryFilterBuilder or(CompassQueryFilter filter);

        CompassBooleanQueryFilterBuilder andNot(CompassQueryFilter filter);

        CompassBooleanQueryFilterBuilder xor(CompassQueryFilter filter);
    }

    /**
     * Should Compass use a converter for value passed even if there is no specific direct dot
     * path notation to it. It will try and derive the best converter to use. Defaults to <code>false</code>.
     *
     * @see org.compass.core.mapping.ResourcePropertyLookup#setConvertOnlyWithDotPath(boolean)
     */
    CompassQueryFilterBuilder convertOnlyWithDotPath(boolean convertOnlyWithDotPath);

    /**
     * Creates a query filter where the resource proeprty is between the given values.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name        The resource property name
     * @param low         The low value limit
     * @param high        The high value limit
     * @param includeLow  Include the lower value
     * @param includeHigh Include the high value
     * @return The query filter
     */
    CompassQueryFilter between(String name, Object low, Object high, boolean includeLow, boolean includeHigh);

    /**
     * Creates a query filter where the resource proeprty is less than (<) the given
     * value.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The high limit value
     * @return The generated query filter
     */
    CompassQueryFilter lt(String name, Object value);

    /**
     * Creates a query filter where the resource proeprty is less or equal (<=) to the
     * given value.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The high limit value
     * @return The generated query filter
     */
    CompassQueryFilter le(String name, Object value);

    /**
     * Creates a query filter where the resource proeprty is greater than (>) to the
     * given value.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The low limit value
     * @return The generated query filter
     */
    CompassQueryFilter gt(String name, Object value);

    /**
     * Creates a query filter where the resource proeprty is greater or equal (>=) to
     * the given value.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The low limit value
     * @return The generated query filter
     */
    CompassQueryFilter ge(String name, Object value);

    /**
     * Creates a query filter that is based on the given compass query.
     *
     * @param query The query to filter by.
     * @return The query filter.
     */
    CompassQueryFilter query(CompassQuery query);

    /**
     * Creates a boolean query filter builder.
     *
     * @return The boolean query filter builder
     */
    CompassBooleanQueryFilterBuilder bool();
}
