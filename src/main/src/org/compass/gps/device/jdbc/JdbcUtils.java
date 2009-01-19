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

package org.compass.gps.device.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A set of Jdbc utilities.
 * 
 * @author kimchy
 * 
 */
public abstract class JdbcUtils {

    private static Log log = LogFactory.getLog(JdbcUtils.class);

    /**
     * Returns a jdbc connection, and in case of failure, wraps the sql
     * exception with a Jdbc device exception.
     * 
     * @param dataSource
     * @return A connection from the datasource.
     * @throws JdbcGpsDeviceException
     */
    public static Connection getConnection(DataSource dataSource) throws JdbcGpsDeviceException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new JdbcGpsDeviceException("Failed to open jdbc connection", e);
        }
    }

    /**
     * Close the given JDBC connection and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * 
     * @param con The JDBC Connection to close
     */
    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                log.warn("Could not close JDBC Connection", ex);
            } catch (RuntimeException ex) {
                log.error("Unexpected exception on closing JDBC Connection", ex);
            }
        }
    }

    /**
     * Close the given JDBC Statement and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * 
     * @param stmt The JDBC Statement to close
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                log.warn("Could not close JDBC Statement", ex);
            } catch (RuntimeException ex) {
                log.error("Unexpected exception on closing JDBC Statement", ex);
            }
        }
    }

    /**
     * Close the given JDBC ResultSet and ignore any thrown exception. This is
     * useful for typical finally blocks in manual JDBC code.
     * 
     * @param rs the JDBC ResultSet to close
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                log.warn("Could not close JDBC ResultSet", ex);
            } catch (RuntimeException ex) {
                log.error("Unexpected exception on closing JDBC ResultSet", ex);
            }
        }
    }

    /**
     * Returns the column index for the guven column name. Note that if there
     * are two columns with the same name, the first onde index will be
     * returned.
     * <p>
     * <code>-1</code> is returned if none is found.
     */
    public static int getColumnIndexFromColumnName(ResultSetMetaData metaData, String columnName) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String tmpName = metaData.getColumnLabel(i);
            if (tmpName.equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
