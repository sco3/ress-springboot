package com.tnf.cas.common.conf;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tnf.cas.common.util.CasPropertiesHelper;

public class CasCfgPropertiesConfigReader {

	static Logger LOGGER = LoggerFactory.getLogger(CasCfgPropertiesConfigReader.class);

	public static Properties getCasCfgProperties() {
		InputStream in = CasCfgPropertiesConfigReader.class
				.getResourceAsStream(CasPropertiesHelper.CAS_PROPERTIES_CONFIG_FILENAME);

		Properties p = new Properties();
		try {
			p.load(in);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}

		return p;
	}

	protected Properties readConfigContent(String configFileAsString) throws Exception {
		InputStream in = CasCfgPropertiesConfigReader.class
				.getResourceAsStream(CasPropertiesHelper.CAS_DEFAULT_PROPERTIES_CONFIG_FILENAME);

		Properties p = new Properties();
		try {
			p.load(in);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}

		return p;

	}
}
