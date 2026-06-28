package com.tnf.bis.common;

public interface ClientConstants {

	static final String FS = System.getProperty("file.separator");
	static final String LS = System.getProperty("line.separator");

	static final String VGID_PARTITION_COLUMN = "vgid";
	static final String VGID_PARTITION_FOLDER_PREFIX = VGID_PARTITION_COLUMN + "=";
	static final String DT_PARTITION_COLUMN = "dt";
	static final String DT_PARTITION_FOLDER_PREFIX = DT_PARTITION_COLUMN + "=";
	static final String SGM_PARTITION_COLUMN = "sgm";
	static final String SGM_PARTITION_FOLDER_PREFIX = SGM_PARTITION_COLUMN + "=";

	// static final String HRCC_SEGMENTS_BY_IMSI_PROPERTY = "hrcc.segments_by_imsi";
}
