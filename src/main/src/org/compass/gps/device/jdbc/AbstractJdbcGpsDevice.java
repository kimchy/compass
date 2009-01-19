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

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.device.AbstractGpsDevice;
import org.compass.gps.device.jdbc.dialect.DialectResolver;
import org.compass.gps.device.jdbc.dialect.JdbcDialect;

/**
 * A helper base class for Jdbc Gps Device. Provides supprot for
 * <code>DataSource</code> and
 * {@link org.compass.gps.device.jdbc.dialect.JdbcDialect}. Also
 * provides template like support for processing database indexing using the
 * <code>IndexExecution</code> object hint, and a set of callback methods:
 * {@link #processResultSet(Object, ResultSet, CompassSession)},
 * {@link #processRow(Object, ResultSet, CompassSession)}, and
 * {@link #processRowValue(Object, ResultSet, CompassSession)}. One of the
 * callback mehtods should be overriden by the derived class otherwize the class
 * won't index anyhting.
 *
 * @author kimchy
 */
public abstract class AbstractJdbcGpsDevice extends AbstractGpsDevice implements JdbcGpsDevice {

    protected Log log = LogFactory.getLog(getClass());

    /**
     * A hint object which provides the statement query to execute or the actual
     * <code>PreparedStatement</code>. It also provides a general data holder
     * called <code>description</code>.
     *
     * @author kimchy
     */
    public static class IndexExecution {
        private Object description;

        private PreparedStatement statement;

        private String statementQuery;

        public IndexExecution(Object description) {
            this.description = description;
        }

        public IndexExecution(Object description, String statementQuery) {
            this.description = description;
            this.statementQuery = statementQuery;
        }

        public IndexExecution(Object description, PreparedStatement statement) {
            this.description = description;
            this.statement = statement;
        }

        public Object getDescription() {
            return description;
        }

        public PreparedStatement getStatement() {
            return statement;
        }

        public String getStatementQuery() {
            return statementQuery;
        }

        public void setStatementQuery(String statementQuery) {
            this.statementQuery = statementQuery;
        }

        public void setStatement(PreparedStatement statement) {
            this.statement = statement;
        }

        public void setDescription(Object description) {
            this.description = description;
        }
    }

    protected DataSource dataSource;

    protected JdbcDialect dialect;

    protected void doStart() throws CompassGpsException {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource property must be set");
        }

        try {
            this.dialect = new DialectResolver(true).getDialect(dataSource);
        } catch (Exception e) {
            log.warn("Failed to detect database dialect", e);
            throw new JdbcGpsDeviceException("Failed to detect database dialect", e);
        }
    }

    /**
     * If this variable is set to a non-zero value, it will be used for setting
     * the fetchSize property on statements used for query processing.
     */
    private int fetchSize = 0;

    /**
     * Performs the indexing operation.
     * <p/>
     * Calls the abstract {@link #doGetIndexExecutions(Connection)} method with
     * an open connection to get the list of {@link IndexExecution} to perform.
     * <p/>
     * For each {@link IndexExecution}, executes the select query, and calls
     * the {@link #processResultSet(Object, ResultSet, CompassSession)} for the
     * returned <code>ResultSet</code>.
     */
    protected void doIndex(CompassSession session) throws CompassGpsException {
        if (log.isInfoEnabled()) {
            log.info("{" + getName() + "}: Indexing the database with fetch size [" + fetchSize + "]");
        }
        Connection connection = JdbcUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            IndexExecution[] indexExecutions = doGetIndexExecutions(connection);
            for (IndexExecution indexExecution : indexExecutions) {
                if (!isRunning()) {
                    return;
                }
                ps = indexExecution.getStatement();
                if (ps == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("{" + getName() + "} Executing select query ["
                                + indexExecution.getStatementQuery() + "]");
                    }
                    ps = connection.prepareStatement(indexExecution.getStatementQuery());
                }
                if (getFetchSize() > 0) {
                    ps.setFetchSize(getFetchSize());
                }
                rs = ps.executeQuery();
                processResultSet(indexExecution.getDescription(), rs, session);
            }
        } catch (CompassException e) {
            log.error("Failed to index database", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to index database", e);
            throw new JdbcGpsDeviceException("Failed to index database", e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(connection);
        }

        if (log.isInfoEnabled()) {
            log.info("{" + getName() + "}: Finished indexing the database");
        }
    }

    /**
     * Called for each {@link IndexExecution} returned from the
     * {@link #doGetIndexExecutions(Connection)} with the <code>ResultSet</code>.
     * Can be override by derived classes, if not override, than iterates threw
     * the <code>ResultSet</code> and calls
     * {@link #processRow(Object, ResultSet, CompassSession)} for each row.
     */
    protected void processResultSet(Object description, ResultSet rs, CompassSession session) throws SQLException,
            CompassException {
        while (rs.next()) {
            processRow(description, rs, session);
        }
    }

    /**
     * Called for each row in the <code>ResultSet</code> which maps to an
     * {@link IndexExecution}. Can be override by derived classes, if not
     * override, than calls
     * {@link #processRowValue(Object, ResultSet, CompassSession)} and uses it's
     * return value to save it in the <code>CompassSession</code>. The return
     * value can be an OSEM enables object, a <code>Resource</code>, or an
     * array of one of them.
     */
    protected void processRow(Object description, ResultSet rs, CompassSession session) throws SQLException,
            CompassException {
        if (!isRunning()) {
            return;
        }
        Object value = processRowValue(description, rs, session);
        if (value != null) {
            if (value instanceof Object[]) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object value1 = Array.get(value, i);
                    session.create(value1);
                }
            } else {
                session.create(value);
            }
        }
    }

    /**
     * Called for each row in the <code>ResultSet</code> which maps to an
     * {@link IndexExecution}. Can be override by derived classes, and should
     * return the actual data to be saved using the <code>CompassSession</code>.
     * The return value can be either an OSEM enables object, a
     * <code>Resource</code>, or an array of one of them.
     */
    protected Object processRowValue(Object description, ResultSet rs, CompassSession session) throws SQLException,
            CompassException {
        return null;
    }

    /**
     * Returns an array of the {@link IndexExecution} that should be executed
     * it's respective <code>ResultSet</code> should be indexed.
     */
    protected abstract IndexExecution[] doGetIndexExecutions(Connection connection) throws SQLException,
            JdbcGpsDeviceException;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public JdbcDialect getDialect() {
        return dialect;
    }

    public void setDialect(JdbcDialect dialect) {
        this.dialect = dialect;
    }
}
