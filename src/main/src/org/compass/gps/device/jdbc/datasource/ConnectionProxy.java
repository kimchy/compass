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

package org.compass.gps.device.jdbc.datasource;

import java.sql.Connection;

/**
 * Subinterface of Connection to be implemented by connection proxies. Allows
 * access to the target connection.
 * 
 * <p>
 * Can be checked for when needing to cast to a native Connection like
 * OracleConnection. Spring's NativeJdbcExtractorAdapter automatically detects
 * such proxies before delegating to the actual unwrapping for a specific
 * connection pool.
 * 
 * <p>
 * Taken from Spring.
 * 
 * @author kimchy
 */
public interface ConnectionProxy extends Connection {

    /**
     * Return the target connection of this proxy.
     * <p>
     * This will typically either be the native JDBC Connection or a wrapper
     * from a connection pool.
     */
    Connection getTargetConnection();

}
