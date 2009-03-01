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

package org.compass.gps.device.ibatis;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.device.jdbc.datasource.SingleConnectionDataSource;
import org.compass.gps.impl.SingleCompassGps;

/**
 * 
 * @author kimchy
 * 
 */
public class SqlMapClientTests extends TestCase {

    private static final String DB_SETUP = ""
            + "CREATE TABLE contact (contactid INTEGER NOT NULL IDENTITY PRIMARY KEY,  firstname VARCHAR(30), lastname VARCHAR(30));";

    private static final String[] DB_DATA = { "INSERT INTO contact VALUES (1, 'first 1', 'last 1');",
            "INSERT INTO contact VALUES (2, 'first 2', 'last 2');",
            "INSERT INTO contact VALUES (3, 'first 3', 'last 3');",
            "INSERT INTO contact VALUES (4, 'first 4', 'last 4');" };

    private static final String DB_TEARDOWN = "DROP TABLE contact;";

    protected SingleConnectionDataSource dataSource;

    private SqlMapClient sqlMapClient;

    private SingleCompassGps compassGps;

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    protected void setUp() throws Exception {
        dataSource = new SingleConnectionDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:test", "sa", "", true);

        setUpDB();
        setUpDBData();

        Reader configReader = Resources.getResourceAsReader("org/compass/gps/device/ibatis/SqlMapConfig.xml");
        sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(configReader);

        ExternalTransactionConfig transactionConfig = new ExternalTransactionConfig();
        transactionConfig.setDataSource(dataSource);

        ExtendedSqlMapClient extendedClient = (ExtendedSqlMapClient) this.sqlMapClient;
        transactionConfig.setMaximumConcurrentTransactions(extendedClient.getDelegate().getMaxTransactions());
        extendedClient.getDelegate().setTxManager(new TransactionManager(transactionConfig));

        CompassConfiguration cpConf = new CompassConfiguration()
                .configure("/org/compass/gps/device/ibatis/compass.cfg.xml");
        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassGps = new SingleCompassGps(compass);
        SqlMapClientGpsDevice gpsDevice = new SqlMapClientGpsDevice("sqlMap", sqlMapClient, "getContacts");
        compassGps.addGpsDevice(gpsDevice);
        compassGps.start();
    }

    protected void tearDown() throws Exception {
        compassGps.stop();
        compass.close();
        fileHandlerMonitor.verifyNoHandlers();
        tearDownDB();
        dataSource.destroy();
    }

    protected void setUpDB() throws SQLException {
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(DB_SETUP);
        ps.execute();
        ps.close();
        con.close();
    }

    protected void tearDownDB() throws SQLException {
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(DB_TEARDOWN);
        ps.execute();
        ps.close();
        con.close();
    }

    protected void setUpDBData() throws SQLException {
        Connection con = dataSource.getConnection();
        Statement stmt = con.createStatement();
        for (int i = 0; i < DB_DATA.length; i++) {
            stmt.addBatch(DB_DATA[i]);
        }
        stmt.executeBatch();
        stmt.close();
        con.close();
    }

    public void testSqlMapClient() throws Exception {
        compassGps.index();
        
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        
        CompassHits hits = session.find("first");
        assertEquals(4, hits.length());
        
        tr.commit();
        session.close();
    }
}
