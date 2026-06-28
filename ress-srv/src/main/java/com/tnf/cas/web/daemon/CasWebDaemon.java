package com.tnf.cas.web.daemon;

import static sco.common.db.BlobberRegistry.getBlobber;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.tnf.cas.web.WebServerConstants;

import sco.common.db.CassandraDatasourceFactory;
import sco.common.properties.BisPropertiesHelper;
import sco.common.properties.Dt;
import sco.common.properties.Sgm;
import sco.common.util.CasPropertiesHelper;

@Component
public class CasWebDaemon implements WebServerConstants, CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasWebDaemon.class);

    @Override
    public void run(String... args) throws Exception {
        try {
            LOGGER.info("Checking config files");
            LOGGER.info(//
                    "Sgm init: {} -> {}", "000000000000000", Sgm.getSgm("000000000000000")//
            );

            LOGGER.info("Starting Cassandra data source");
            try {
                Properties cassandraConnectionProperties = CasPropertiesHelper.getCassandraConnectionProperties();
                CassandraDatasourceFactory.getCluster(cassandraConnectionProperties);
            } catch (Exception ex) {
                LOGGER.warn("Cannot get connection to Cassandra DB: {}", ex.toString());
            }

            Sgm.getSgm("");// init()
            Dt.getDt("", null);// init()
            LOGGER.info("Blob registry: {}", getBlobber().info());
            LOGGER.info("Init() completed");
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        LOGGER.info("Application started");
    }

    public void shutdown() {
        try {
            LOGGER.info("Shutdowning Cassandra data source");
            Properties cassandraConnectionProperties = CasPropertiesHelper.getCassandraConnectionProperties();
            Cluster cassandraCluster = CassandraDatasourceFactory.getCluster(cassandraConnectionProperties);
            if (cassandraCluster != null) {
                cassandraCluster.close();
            }
        } catch (Exception ex) {
            LOGGER.warn(ex.toString());
        }
        LOGGER.info("Shutdown() completed");
    }
}
