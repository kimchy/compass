/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.ibatis;

import java.sql.SQLException;
import java.util.Iterator;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;
import org.compass.gps.device.AbstractGpsDevice;

/**
 * A <code>SqlMapClient</code> device, provides support for iBatis 2 and the
 * <code>index</code> operation. The device holds a list of iBatis select
 * statements ids, executes them, and index the result.
 * <p/>
 * The device must be initialized with a <code>SqlMapClient</code> instance.
 * When indexing the data, a <code>SqlMapSession</code> will be opened, and a
 * transaction will be started. The device will then execute the select
 * statement id, and use the iBatis <code>PaginatedList</code> to index the
 * data.
 * <p/>
 * The page size for the <code>PaginatedList</code> can be controlled using
 * the <code>pageSize</code> property.
 * <p/>
 * The select statment can have a parameter object associated with it. If one of
 * the select statements requires a parameter object, than the
 * <code>statementsParameterObjects</code> property must be set. It must have
 * the same size as the <code>selectStatementsIds</code>, and the matching
 * index of the <code>selectStatementsIds</code> should be set at the
 * <code>statementsParameterObjects</code> property.
 *
 * @author kimchy
 */
public class SqlMapClientGpsDevice extends AbstractGpsDevice {

    private SqlMapClient sqlMapClient;

    private String[] selectStatementsIds;

    private Object[] statementsParameterObjects;

    private int pageSize = 200;

    public SqlMapClientGpsDevice() {

    }

    public SqlMapClientGpsDevice(String deviceName, SqlMapClient sqlMapClient, String[] selectStatementsIds) {
        this(deviceName, sqlMapClient, selectStatementsIds, null);
    }

    public SqlMapClientGpsDevice(String deviceName, SqlMapClient sqlMapClient, String[] selectStatementsIds,
                                 Object[] statementsParameterObjects) {
        setName(deviceName);
        this.sqlMapClient = sqlMapClient;
        this.selectStatementsIds = selectStatementsIds;
        this.statementsParameterObjects = statementsParameterObjects;
    }

    protected void doStart() throws CompassGpsException {
        if (sqlMapClient == null) {
            throw new IllegalArgumentException(buildMessage("Must set sqlMapClaient property"));
        }
        if (selectStatementsIds == null) {
            throw new IllegalArgumentException(buildMessage("Must set selectStatementsIds property"));
        }
        if (selectStatementsIds.length == 0) {
            throw new IllegalArgumentException(
                    buildMessage("selectStatementsIds property must have at least one entry"));
        }
        if (statementsParameterObjects != null && statementsParameterObjects.length != selectStatementsIds.length) {
            throw new IllegalArgumentException(
                    buildMessage("Once the statementsParameterObjects property is set, it must have the same length as the selectStatementsIds property"));
        }
    }

    public SqlMapClient getSqlMapClient() {
        return sqlMapClient;
    }

    public void setSqlMapClient(SqlMapClient sqlMapClient) {
        this.sqlMapClient = sqlMapClient;
    }

    protected void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException {
        // TODO take into account the index plan
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database with page size [" + pageSize + "]"));
        }

        for (int i = 0; i < selectStatementsIds.length; i++) {
            SqlMapSession sqlMapSession = sqlMapClient.openSession();
            try {
                sqlMapSession.startTransaction();
                Object parameterObject = null;
                if (statementsParameterObjects != null) {
                    parameterObject = statementsParameterObjects[i];
                }
                PaginatedList paginatedList = sqlMapSession.queryForPaginatedList(selectStatementsIds[i],
                        parameterObject, pageSize);
                do {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("Indexing select statement id [" + selectStatementsIds[i]
                                + "] page [" + paginatedList.getPageIndex() + "]"));
                    }
                    for (Iterator it = paginatedList.iterator(); it.hasNext();) {
                        session.create(it.next());
                    }
                    session.evictAll();
                } while (paginatedList.nextPage());
                sqlMapSession.commitTransaction();
            } catch (SQLException e) {
                throw new SqlMapGpsDeviceException("Failed to fetch paginated list for statement ["
                        + selectStatementsIds[i] + "]", e);
            } finally {
                try {
                    try {
                        sqlMapSession.endTransaction();
                    } catch (Exception e) {
                        log.warn(buildMessage("Failed to close sqlMap session, ignoring"), e);
                    }
                } finally {
                    sqlMapSession.close();
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Finished indexing the database"));
        }
    }

    public String[] getSelectStatementsIds() {
        return selectStatementsIds;
    }

    public void setSelectStatementsIds(String[] statementsNames) {
        this.selectStatementsIds = statementsNames;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Object[] getStatementsParameterObjects() {
        return statementsParameterObjects;
    }

    public void setStatementsParameterObjects(Object[] statementsParameterObjects) {
        this.statementsParameterObjects = statementsParameterObjects;
    }

}
