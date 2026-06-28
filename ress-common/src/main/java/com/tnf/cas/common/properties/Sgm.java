package com.tnf.cas.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tnf.udf.CommonBaseSgm;

public class Sgm extends CommonBaseSgm {

	private static final Logger LOGGER = LoggerFactory.getLogger(Sgm.class);

	private static Sgm INSTANCE;

	public static Sgm singleton() {
		if (INSTANCE == null) {
			INSTANCE = new Sgm();
		}
		return INSTANCE;
	}

	public static String getSgm(String imsi) {
		return singleton().calculateSgm(imsi);
	}

	private String calculateSgm(String imsi) {
		int sgm = -1;
		try {
			sgm = sgm(imsi);
		} catch (RuntimeException ex) {
			LOGGER.error(ex.toString());
		}
		return String.valueOf(sgm);
	}

}
