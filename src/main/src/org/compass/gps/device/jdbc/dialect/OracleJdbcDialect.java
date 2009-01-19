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

package org.compass.gps.device.jdbc.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DefaultJdbcDialect with the setParameter method compatible with Oracle JDBC Drivers.
 */
public class OracleJdbcDialect extends DefaultJdbcDialect {

    /**
     * @see org.compass.gps.device.jdbc.dialect.DefaultJdbcDialect#setParameter(java.sql.PreparedStatement, int,
     *      java.lang.String)
     */
    @Override
    public void setParameter(final PreparedStatement ps, final int paramIndex, final String value) throws SQLException {
        if (value != null) {
            ps.setObject(paramIndex, value);
        } else {
            throw new IllegalArgumentException("Failed to set parameter with index [" + paramIndex + "] and value ["
                    + value + "] , not supported");
        }
    }
}
