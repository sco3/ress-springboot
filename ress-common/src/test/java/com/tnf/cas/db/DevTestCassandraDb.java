package com.tnf.cas.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.tnf.bis.common.db.CassandraDatasourceFactory;

public class DevTestCassandraDb {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevTestCassandraDb.class);
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
    public void testUpdateCreateTableCqlFiles() throws Exception {
        try {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("CREATE TABLE test (id text PRIMARY KEY)");
            Path directory = Paths.get(System.getProperty("user.dir"), "target");
            Path file = directory.resolve("test.cql");
            Files.deleteIfExists(file);
            Set<String> tableNames = new HashSet<String>();
            tableNames.add("test");
            CassandraDbHelper.updateCreateTableCqlFiles(connectionProperties, tableNames, directory);
            Assert.assertTrue(Files.exists(file));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("DROP TABLE IF EXISTS test");
        }
    }

    @Test
    public void testWriteReadProperty() throws Exception {
        try {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("CREATE TABLE cas_properties (name text PRIMARY KEY, value text)");

            String key = "abc.123";
            String dbValue = CassandraDbHelper.readProperty(connectionProperties, key);
            Assert.assertNull(dbValue);
            //
            String value = "qwer56789";
            CassandraDbHelper.writeProperty(connectionProperties, key, value, 60, LOGGER);
            //
            dbValue = CassandraDbHelper.readProperty(connectionProperties, key);
            Assert.assertNotNull(dbValue);
            Assert.assertEquals(value, dbValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("DROP TABLE IF EXISTS cas_properties");
        }
    }

    @Test
    public void testUpdateDefaultTtl() throws Exception {
        try {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("CREATE TABLE test (name text PRIMARY KEY, value text)");

            String tableName = "test";
            int ttl = 12345;
            //
            CassandraDbHelper.updateTableDefaultTtl(connectionProperties, tableName, ttl, LOGGER);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            session.execute("DROP TABLE IF EXISTS test");
        }
    }
}
