package sco.common.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.NettySSLOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.Statement;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import sco.common.util.CassandraHelper;

public class CassandraDatasourceFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDatasourceFactory.class);
	public static final String CREATE_KEYSPACE_CQL_PROPERTY_NAME = "db.cassandra.create.keyspace.cql";
	public static final String CASSANDRA_HOSTS_COMMA_SEPARATED = "cassandra.hosts.comma.separated";
	public static final String CASSANDRA_PORT = "cassandra.port";
	private static final String CASSANDRA_PORT_DEFAULT = "9042";
	public static final String CASSANDRA_KEYSPACE = "cassandra.keyspace";
	public static final String CASSANDRA_TRUSTSTORE = "cassandra.truststore";
	public static final String CASSANDRA_TRUSTSTORE_PASSWORD = "cassandra.truststore.password";
	public static final String USER_PROPERTY_NAME = "cassandra.user";
	public static final String PASSWORD_PROPERTY_NAME = "cassandra.password";

	private static Map<Long, Session> sessionMap = new HashMap<Long, Session>();
	private static boolean lastConnectionSuccessful = true;

	public static Map<Long, Session> getSessionMap() {
		return sessionMap;
	}

	public static KeyspaceMetadata getKeyspaceMetadata(Properties properties) throws Exception {
		return getSession(properties).getCluster().getMetadata().getKeyspace(getKeyspaceName(properties));
	}

	public static Cluster getCluster(Properties cassandraConnectionProperties) throws Exception {
		return getSession(cassandraConnectionProperties).getCluster();
	}

	public static Session getSession(Properties cassandraConnectionProperties) throws Exception {
		Long key = getSessionMapKey(cassandraConnectionProperties);
		Session session = null;
		while (true) {
			session = getSessionMap().get(key);
			if (session == null) {
				synchronized (getSessionMap()) {
					session = getSessionMap().get(key);
					if (session == null) {
						List<InetSocketAddress> addresses = extractContactPointsWithPorts(
								cassandraConnectionProperties);
						String keyspaceName = getKeyspaceName(cassandraConnectionProperties);
						LOGGER.info("{}={}", CASSANDRA_KEYSPACE, keyspaceName);
						String username = cassandraConnectionProperties.getProperty( //
								USER_PROPERTY_NAME);
						LOGGER.info("{}={}", USER_PROPERTY_NAME, username);
						String password = cassandraConnectionProperties.getProperty( //
								PASSWORD_PROPERTY_NAME);
						LOGGER.info("{}={}", PASSWORD_PROPERTY_NAME, "...");
						Builder bld = Cluster.builder() //
								.addContactPointsWithPorts(addresses) //
								.withSocketOptions(new SocketOptions() //
										.setReadTimeoutMillis(SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS * 10))
								.withCredentials(username, password);
						String tStore = cassandraConnectionProperties.getProperty( //
								CASSANDRA_TRUSTSTORE //
						);
						String tPassw = cassandraConnectionProperties.getProperty( //
								CASSANDRA_TRUSTSTORE_PASSWORD //
						);
						if (tStore != null && tPassw != null) {
							addSSL(bld, tStore, tPassw);
						}
						Cluster cluster = bld.build();
						session = cluster.connect();
						if (!checkKeyspaceExists(keyspaceName, session)) {
							LOGGER.warn("Keyspace '{}' does not exist, trying to create...", keyspaceName);
							String createKeyspaceCql = cassandraConnectionProperties
									.getProperty(CREATE_KEYSPACE_CQL_PROPERTY_NAME);
							if (createKeyspaceCql != null) {
								LOGGER.info(createKeyspaceCql);
								Statement statement = new SimpleStatement(createKeyspaceCql);
								CassandraHelper.execute(statement, session);
							} else {
								LOGGER.error("Configuration property '{}' not found",
										CREATE_KEYSPACE_CQL_PROPERTY_NAME);
								cluster.close();
								break;
							}
						}
						useKeyspace(keyspaceName, session);
						getSessionMap().put(key, session);
					}
				}
			}
			Cluster cluster = session.getCluster();
			if (!cluster.isClosed()) {
				if (session.isClosed()) {
					session = cluster.connect();
					String keyspaceName = getKeyspaceName(cassandraConnectionProperties);
					useKeyspace(keyspaceName, session);
					getSessionMap().put(key, session);
				}
				break;
			}
			cluster.close();
			synchronized (getSessionMap()) {
				getSessionMap().remove(key);
			}
		}
		return session;
	}

	private static void addSSL(Builder bld, String tStore, String tPasswd) {
		try (InputStream is = new FileInputStream(tStore)) {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(is, tPasswd.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( //
					TrustManagerFactory.getDefaultAlgorithm()//
			);
			tmf.init(ks);

			SslContextBuilder builder = SslContextBuilder//
					.forClient()//
					.sslProvider(SslProvider.JDK)//
					.trustManager(tmf);

			SSLOptions opts = new NettySSLOptions(builder.build());
			bld.withSSL(opts);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		// System.setProperty("javax.net.ssl.trustStore", tStore);
		// System.setProperty("javax.net.ssl.trustStorePassword", tPasswd);
		// bld.withSSL();
	}

	private static void useKeyspace(String keyspaceName, Session session) throws Exception {
		if (keyspaceName == null || keyspaceName.isEmpty()) {
			return;
		}
		Statement statement = new SimpleStatement("USE " + keyspaceName);
		CassandraHelper.execute(statement, session);
	}

	public static long getSessionMapKey(Properties cassandraConnectionProperties) {
		String hosts = cassandraConnectionProperties.getProperty(CASSANDRA_HOSTS_COMMA_SEPARATED);
		String port = cassandraConnectionProperties.getProperty(CASSANDRA_PORT, CASSANDRA_PORT_DEFAULT);
		String keyspace = cassandraConnectionProperties.getProperty(CASSANDRA_KEYSPACE);
		String user = cassandraConnectionProperties.getProperty(USER_PROPERTY_NAME);
		String password = cassandraConnectionProperties.getProperty(PASSWORD_PROPERTY_NAME);
		String trsStore = cassandraConnectionProperties.getProperty(CASSANDRA_TRUSTSTORE);
		String trsPassw = cassandraConnectionProperties.getProperty(CASSANDRA_TRUSTSTORE_PASSWORD);
		CRC32 crc = new CRC32();
		crc.update(hosts.getBytes());
		crc.update(port.getBytes());
		if (keyspace != null) {
			crc.update(keyspace.getBytes());
		}
		if (user != null) {
			crc.update(user.getBytes());
		}
		if (password != null) {
			crc.update(password.getBytes());
		}
		if (trsStore != null) {
			crc.update(trsStore.getBytes());
		}
		if (trsPassw != null) {
			crc.update(trsPassw.getBytes());
		}
		return crc.getValue();
	}

	static List<InetSocketAddress> extractContactPointsWithPorts(Properties cassandraConnectionProperties) {
		String hostsStr = cassandraConnectionProperties.getProperty(CASSANDRA_HOSTS_COMMA_SEPARATED);
		LOGGER.info("{}={}", CASSANDRA_HOSTS_COMMA_SEPARATED, hostsStr);
		int port = Integer.parseInt(cassandraConnectionProperties.getProperty(CASSANDRA_PORT, CASSANDRA_PORT_DEFAULT));
		LOGGER.info("{}={}", CASSANDRA_PORT, port);

		String[] hosts = hostsStr.split(Pattern.quote(","));
		List<InetSocketAddress> result = new ArrayList<InetSocketAddress>();
		for (String hostname : hosts) {
			result.add(new InetSocketAddress(hostname, port));
		}
		return result;
	}

	public static String getKeyspaceName(Properties cassandraConnectionProperties) {
		String value = cassandraConnectionProperties.getProperty(CASSANDRA_KEYSPACE);
		if (value != null) {
			return value.toLowerCase();
		}
		return value;
	}

	private static boolean checkKeyspaceExists(String keyspaceName, Session session) throws Exception {
		if (keyspaceName == null || keyspaceName.isEmpty()) {
			return true;
		}
		String cql = "select keyspace_name from system_schema.keyspaces";
		LOGGER.debug(cql);
		Statement statement = new SimpleStatement(cql);
		ResultSet rs = CassandraHelper.execute(statement, session);
		for (Row row : rs) {
			String name = row.getString("keyspace_name");
			if (name.equalsIgnoreCase(keyspaceName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isLastConnectionSuccessful() {
		return lastConnectionSuccessful;
	}

	public static void setLastConnectionSuccessful(boolean lastConnectionSuccessful) {
		CassandraDatasourceFactory.lastConnectionSuccessful = lastConnectionSuccessful;
	}
}
