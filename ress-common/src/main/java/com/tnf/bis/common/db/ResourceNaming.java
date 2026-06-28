package com.tnf.bis.common.db;

import com.tnf.bis.common.CommonConstants;

public interface ResourceNaming extends CommonConstants {
	static final String HIVE_STORAGE_PROPERTIES_FILE_NAME = "hive-storage.properties";

	static final String DIMENSION_TABLE_FORMAT = "dimension.table.format";
	static final String DIMENSION_TABLE_LOCATION = "db.dim.location";
	static final String DIMENSION_TABLE_PROPERTES = "dimension.table.properties";

	static final String HIVE_CONFIG="hive.config";
}
