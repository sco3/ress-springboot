package com.tnf.cas.db;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.tnf.bis.common.db.CassandraDatasourceFactory;

public class DevTestCassandraDatasourceFactory {
    private static Properties connectionProperties;

    @BeforeClass
    public static void startUpClass() throws Exception {
        connectionProperties = new Properties();
        connectionProperties.put(CassandraDatasourceFactory.CASSANDRA_HOSTS_COMMA_SEPARATED, "192.168.56.106");
        connectionProperties.put(CassandraDatasourceFactory.CASSANDRA_KEYSPACE, "test_keyspace");
        connectionProperties.put(CassandraDatasourceFactory.CREATE_KEYSPACE_CQL_PROPERTY_NAME,
                "CREATE KEYSPACE test_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Session session = CassandraDatasourceFactory.getSession(connectionProperties);
        session.execute("DROP KEYSPACE IF EXISTS test_keyspace");
        Cluster cluster = CassandraDatasourceFactory.getCluster(connectionProperties);
        cluster.close();
        cluster.close();
    }

    @Before
    public void startUp() throws Exception {
        tearDown();
    }

    @After
    public void tearDown() throws Exception {
        Session session = CassandraDatasourceFactory.getSession(connectionProperties);
        session.execute("DROP TABLE IF EXISTS test");
    }

    @Test
    public void testCassandraDatasourceFactory() throws Exception {
        try {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            String keyspaceName = CassandraDatasourceFactory.getKeyspaceName(connectionProperties);
            Row found = session.execute("select id from system_schema.tables where keyspace_name='" + keyspaceName
                    + "' and table_name='test'").one();
            if (found == null) {
                session.execute("CREATE TABLE test (id TIMEUUID PRIMARY KEY)");
            }
            session.execute("INSERT INTO test (id) VALUES(now())");
            session.execute("INSERT INTO test (id) VALUES(now())");
            session.close();
            session = CassandraDatasourceFactory.getSession(connectionProperties);
            ResultSet rs = session.execute("SELECT id FROM test");
            for (Row row : rs) {
                System.out.println(row.getUUID("id"));
            }
            session.execute("INSERT INTO test (id) VALUES(now())");
            System.out.println();
            rs = session.execute("SELECT id FROM test");
            for (Row row : rs) {
                System.out.println(row.getUUID("id"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("DROP TABLE IF EXISTS test");
            session.execute("DROP KEYSPACE IF EXISTS test_keyspace");
        }
    }
}
