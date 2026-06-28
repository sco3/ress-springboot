package com.tnf.bis.common;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface CommonConstants {

	static final String TIMEID_COLUMN = "timeid";
	static final String DIM_PARTITION_COLUMN = "dim";
	static final String DIM_PARTITION_FOLDER_PREFIX = DIM_PARTITION_COLUMN + "=";
	static final String METRIC_GROUP_ID_COLUMN = "metric_group_id";
	static final String METRIC_ID_COLUMN = "metric_id";
	static final String IMSI_COLUMN = "imsi";
	static final String APPLICATION_COLUMN = "APPLICATION";
	static final String DT_PARTITION_TIME_GRANULARITY_PROPERTY = "timeGranularity";
	static final String VIEW_SOURCE_TABLE_PROPERTY = "viewSourceTable";

	static final String AGGVIEW_TABLES_POPULATED_BY_MEDIATION_PROPERTY = "aggview.tables.populated.by.mediation";
	static final String AGGVIEW_TABLES_POPULATED_BY_MEDIATION_DEFAULT = "true";

	static final String DB_SCHEMA_NAME_PROPERTY = "db.schema.name";
	static final String DB_SCHEMA_NAME_DEFAULT = "tnf";
	static final String DB_SCHEMA_LOCATION_PROPERTY = "db.schema.location";
	static final String SEGMENTS_BY_IMSI_PROPERTY = "segments_by_imsi";
	static final String TEMP_DIR_LOCATION_PROPERTY = "bis.tmpdir";

	static final String DEFAULT_CONNECTION_PROPERTIES_FOR_AGGREGATION_JOBS_PROPERTIES = "default.connection.properties.for.aggregation.jobs";

	static final String TIMEZONE_PROPERTY = "TIMEZONE";
	static final String LOCALE_PROPERTY = "LOCALE";

	static final String BIS_UDF_PROPERTIES_FILE_NAME = "bis-udf.properties";
	static final String PARTITIONING_CONFIG_FILENAME = "partitionedTables.xml";
	static final String DICTIONARY_CONFIG_FILENAME = "dictionaryTables.xml";
	static final String AGGREGATION_CONFIG_FILENAME = "aggregationJobs.xml";
	static final String AGGVIEW_DEFINITIONS_FILENAME = "aggViewDefinitions.json";
	static final String METRIC_DEFINITIONS_FILENAME = "metricDefinitions.json";

	static final String PATH_TO_USER_CONFIG_DIR = Paths.get( //
			"..", "..", "bis-main-var", "bis-demon", "cfg").toString();
	static final String HIVE_NULL = "\\N";

	static final Path HDFS_WATCHER_TEMPORARY_DIR = Paths.get( //
			"..", "..", "bis-main-var", "bis-demon", "tmp");

	static final String DEFAULT_FS_PERMISSIONS_UMASK_KEY = "027"; // mode = 750
																	// = rwxr.x...

	static final String DEFAULT_EXECUTION_ENGINE = "hive.execution.engine";

	static final String START_DT_VARIABLE = "startDt";
	static final String END_DT_VARIABLE = "endDt";

	static final int ADDITIONAL_DELAY_OF_AGGREGATION_EVENT_COLLECTOR = 0; // =
																			// 1;
	static final String CHILD_OS_PROCESS_TIMEOUT_HOURS_PROPERTY = "child.os.process.timeout.hours";
	static final String CHILD_OS_PROCESS_TIMEOUT_HOURS_DEFAULT = "3";
}
