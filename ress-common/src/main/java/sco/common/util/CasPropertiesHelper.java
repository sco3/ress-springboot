package sco.common.util;

import java.util.Properties;

import org.springframework.core.env.Environment;

import sco.common.db.CassandraDatasourceFactory;
import sco.common.CasCommonConstants;

public class CasPropertiesHelper implements CasCommonConstants {

	public static final String CAS_PROPERTIES_CONFIG_FILENAME = "cfg-cas.properties";
	public static final String CAS_DEFAULT_PROPERTIES_CONFIG_FILENAME = CAS_PROPERTIES_CONFIG_FILENAME + ".default";
	public static final String EXTDB_CONNECTION_PROPERTIES_FILE_NAME = "extdb_connection.properties";

	private static Environment environment;

	public static void setEnvironment(Environment env) {
		environment = env;
	}

	public static Properties getCassandraConnectionProperties() {
		if (environment == null) {
			return null;
		}
		Properties props = new Properties();
		setProp(props, CassandraDatasourceFactory.CASSANDRA_HOSTS_COMMA_SEPARATED);
		setProp(props, CassandraDatasourceFactory.CASSANDRA_PORT);
		setProp(props, CassandraDatasourceFactory.CASSANDRA_KEYSPACE);
		setProp(props, CassandraDatasourceFactory.USER_PROPERTY_NAME);
		setProp(props, CassandraDatasourceFactory.PASSWORD_PROPERTY_NAME);
		setProp(props, CassandraDatasourceFactory.CASSANDRA_TRUSTSTORE);
		setProp(props, CassandraDatasourceFactory.CASSANDRA_TRUSTSTORE_PASSWORD);
		setProp(props, CassandraDatasourceFactory.CREATE_KEYSPACE_CQL_PROPERTY_NAME);
		return props;
	}

	private static void setProp(Properties props, String key) {
		String value = environment.getProperty(key);
		if (value != null) {
			props.setProperty(key, value);
		}
	}

}
