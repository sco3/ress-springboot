package sco.common.util;

import java.util.Properties;

import com.tnf.cas.common.CasCommonConstants;

public class CasPropertiesHelper implements CasCommonConstants {

	public static final String CAS_PROPERTIES_CONFIG_FILENAME = "cfg-cas.properties";
	public static final String CAS_DEFAULT_PROPERTIES_CONFIG_FILENAME = CAS_PROPERTIES_CONFIG_FILENAME + ".default";
	public static final String EXTDB_CONNECTION_PROPERTIES_FILE_NAME = "extdb_connection.properties";

	public static Properties getCassandraConnectionProperties() {
		return null;
	}

}
