package com.tnf.cas.web.daemon;

import static sco.common.db.BlobberRegistry.getBlobber;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.tnf.cas.web.WebServerConstants;
import com.tnf.cas.webserver.WebServer;

import sco.common.db.CassandraDatasourceFactory;
import sco.common.properties.BisPropertiesHelper;
import sco.common.properties.Dt;
import sco.common.properties.Sgm;
import sco.common.util.CasPropertiesHelper;

public class CasWebDaemon implements WebServerConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(CasWebDaemon.class);

	private WebServer webServer;

	public static void main(String[] args) throws Exception {
		new CasWebDaemon().init();
	}

	public void init() throws Exception {
		try {

			waitCassandraConnectionPropertiesFile();

			LOGGER.info("Checking config files");
			LOGGER.info(//
					"Sgm init: {} -> {}", "000000000000000", Sgm.getSgm("000000000000000")//
			);
			Path pathToUserConfigDir = Paths.get( //
					System.getProperty("user.dir"), PATH_TO_USER_CONFIG_DIR).normalize();
			
			

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
		LOGGER.info("Starting web server");
		webServer = new WebServer();
		webServer.init();

	}

	public void shutdown() {
		try {
			if (webServer != null) {
				LOGGER.info("Shutdowning web server");
				webServer.shutdown();
			}
		} catch (Exception ex) {
			LOGGER.warn(ex.toString());
		}
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

	private void waitCassandraConnectionPropertiesFile() throws InterruptedException {
		String propertiesFileName = BisPropertiesHelper.CASSANDRA_CONNECTION_PROPERTIES_FILE_NAME;
		while (true) {
			URL url = Thread.currentThread().getContextClassLoader().getResource(propertiesFileName);
			if (url != null) {
				break;
			}
			LOGGER.warn("File {} not found in the classpath. Waiting for 15 seconds...", propertiesFileName);
			Thread.sleep(15000);
		}
	}
}
