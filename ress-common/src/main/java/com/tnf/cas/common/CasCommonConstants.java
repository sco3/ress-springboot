package com.tnf.cas.common;

public interface CasCommonConstants {
    static final String FS = System.getProperty("file.separator");
    static final String LS = System.getProperty("line.separator");
    static final String CSV_SEPARATOR = "|";

    static final String PATH_TO_DEMON_CONFIG_DIR = "cfg";

    static final String FILE_LOADER_EXTENSION_PROPERTY = "file.loader.extension";
    static final String FILE_LOADER_EXTENSION_DEFAULT = "csv";
    static final String FILE_LOADER_INPUT_DIRECTORY_PROPERTY = "file.loader.input.directory";
    static final String FILE_LOADER_ARCHIVE_DIRECTORY_PROPERTY = "file.loader.archive.directory";
    static final String FILE_LOADER_ERROR_DIRECTORY_PROPERTY = "file.loader.error.directory";
    static final String FILE_LOADER_TEMP_DIRECTORY_PROPERTY = "file.loader.temp.directory";
    static final String DIM_INPUT_DIR_PROPERTY = "dim.input.dir";

    static final String METRIC_GROUP_ID_COLUMN = "metric_group_id";
    static final String METRIC_ID_COLUMN = "metric_id";
    static final String TIMEID_COLUMN = "timeid";
    static final String IMSI_COLUMN = "imsi";
    static final String DT_COLUMN = "dt";
    static final String SGM_COLUMN = "sgm";

    static final String METRIC_DEFINITION_PROPERTY_PREFIX = "metric.definition";
    static final String METRIC_DEFINITION_PROPERTY_ALL_SUFFIX = "all";

    static final String DT_PERIOD_FOR_TABLE_PROPERTY_PREFIX = "dt.period.for.table";

    static final String TTL_ENABLE_PROPERTY = "ttl.enable";
    static final String TTL_ENABLE_DEFAULTS = "true";

    static final String DT_PERIOD_FOR_HISTORICAL_TABLES_PROPERTY_PREFIX = "dt.period.for";
    static final String DT_PERIOD_FOR_HISTORICAL_TABLES_PROPERTY_SUFIX = "historical.tables";

    static final String MAX_FILES_IN_ARCHIVE_DIRECTORY_PROPERTY = "max.files.in.archive.directory";
    static final String MAX_FILES_IN_ARCHIVE_DIRECTORY_DEFAULT = "4000";

    static final String MAX_FILES_IN_ERROR_DIRECTORY_PROPERTY = "max.files.in.error.directory";
    static final String MAX_FILES_IN_ERROR_DIRECTORY_DEFAULT = "4000";

    static final String ADD_OTHERS_TO_TOP_METRICS_PROPERTY = "add.others.to.top.metrics";
    static final String ADD_OTHERS_TO_TOP_METRICS_DEFAULT = "true";
    
    static final String HIVE_EXTRACTOR_SOURCE_AGGVIEWS_FILTER_PROPERTY = "hive.extractor.source.aggviews.filter";
    static final String HIVE_EXTRACTOR_SOURCE_AGGVIEWS_FILTER_DEFAULT = "*";
    static final String HIVE_EXTRACTOR_HRCC_DICTIONARIES_FILTER_PROPERTY = "hive.extractor.hrcc.dictionaries.filter";
    static final String HIVE_EXTRACTOR_HRCC_DICTIONARIES_FILTER_DEFAULT = "*";
}
