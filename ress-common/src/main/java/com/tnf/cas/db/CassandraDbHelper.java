package com.tnf.cas.db;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.TableMetadata;
import com.tnf.bis.common.db.CassandraDatasourceFactory;
import com.tnf.bis.common.util.CassandraHelper;
import com.tnf.cas.common.CasCommonConstants;
import com.tnf.cas.common.conf.CasCfgPropertiesConfigReader;
import com.tnf.cas.common.helper.CasFileHelper;

public class CassandraDbHelper implements CasCommonConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDbHelper.class);
    public static final String CAS_PROPERTIES_TABLE = "cas_properties";

    public static void updateCreateTableCqlFiles( //
            Properties connectionProperties, //
            Set<String> tableNames, //
            Path directory //
    ) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
            }
            KeyspaceMetadata keyspaceMetadata = CassandraDatasourceFactory.getKeyspaceMetadata( //
                    connectionProperties);
            for (String tableName : tableNames) {
                TableMetadata tableMetadata = keyspaceMetadata.getTable(tableName);
                if (tableMetadata != null) {
                    String cql = tableMetadata.exportAsString();
                    Path tmpDir = CasFileHelper.getTempDir();
                    Path tmpFile = Files.createTempFile(tmpDir, null, null);
                    try {
                        try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
                            writer.write(cql);
                        }
                        Path outputFile = directory.resolve(tableName + ".cql");
                        Files.move(tmpFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
                    } finally {
                        Files.deleteIfExists(tmpFile);
                    }
                } else {
                    LOGGER.error("Table [{}] not found in keyspace [{}]", tableName, keyspaceMetadata.getName());
                }

            }
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }

    public static String readProperty( //
            Properties connectionProperties, //
            String key //
    ) {
        String result = null;
        try {
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            SimpleStatement statement = new SimpleStatement(
                    "SELECT value FROM " + CAS_PROPERTIES_TABLE + " WHERE name=?", key);
            ResultSet rs = CassandraHelper.execute(statement, session);
            Row row = rs.one();
            if (row != null) {
                result = row.getString(0);
            }
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
        return result;
    }

    public static void writeProperty( //
            Properties connectionProperties, //
            String key, //
            String value, //
            int ttl, //
            Logger logger //
    ) {
        try {
            if (ttl > 0) {
                Properties casCfgProperties = CasCfgPropertiesConfigReader.getCasCfgProperties();
                String propertyValue = casCfgProperties.getProperty(TTL_ENABLE_PROPERTY, TTL_ENABLE_DEFAULTS);
                boolean ttlEnable = Boolean.parseBoolean(propertyValue);
                if (ttlEnable == false) {
                    ttl = 0;
                }
            }
            Session session = CassandraDatasourceFactory.getSession(connectionProperties);
            SimpleStatement statement = new SimpleStatement(
                    "INSERT INTO " + CAS_PROPERTIES_TABLE + " (name, value) VALUES(?,?) USING TTL ?", key, value, ttl);
            CassandraHelper.execute(statement, session);
            logger.info("Property updated in {} table: {}={}", //
                    CassandraDbHelper.CAS_PROPERTIES_TABLE, //
                    key, //
                    value);
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    public static void writeProperty( //
            Properties connectionProperties, //
            String key, //
            String value, //
            Logger logger //
    ) {
        writeProperty(connectionProperties, key, value, 0, logger);
    }

    public static void updateTableDefaultTtl( //
            Properties connectionProperties, //
            String tableName, //
            int ttl, //
            Logger logger //
    ) {
        try {
            if (ttl > 0) {
                Properties casCfgProperties = CasCfgPropertiesConfigReader.getCasCfgProperties();
                String propertyValue = casCfgProperties.getProperty(TTL_ENABLE_PROPERTY, TTL_ENABLE_DEFAULTS);
                boolean ttlEnable = Boolean.parseBoolean(propertyValue);
                if (ttlEnable == false) {
                    ttl = 0;
                }
				Session session = CassandraDatasourceFactory.getSession(connectionProperties);
				String cql = "ALTER TABLE " + tableName + " WITH default_time_to_live=" + ttl;
				logger.info(cql);
				SimpleStatement statement = new SimpleStatement(cql);
				CassandraHelper.execute(statement, session);
            } else {
            	logger.warn("Negative TTL={} for table {}", ttl, tableName);
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    public static void ping(Properties connectionProperties) throws Exception {
        Session session = CassandraDatasourceFactory.getSession(connectionProperties);
        SimpleStatement statement = new SimpleStatement(
                "SELECT table_name FROM " + CassandraHelper.FLYWAY_TABLE + " LIMIT 1");
        CassandraHelper.execute(statement, session);
    }
}
