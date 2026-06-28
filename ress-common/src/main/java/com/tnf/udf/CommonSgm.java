package com.tnf.udf;

import java.util.Map;
import java.util.Properties;

import com.tnf.bis.common.CommonConstants;

public class CommonSgm extends CommonBaseSgm {

    public static void updateMemebers(Properties properties) {
        String ssegments = properties.getProperty(CommonConstants.SEGMENTS_BY_IMSI_PROPERTY);
        if (ssegments == null) {
            throw new RuntimeException("'" + CommonConstants.SEGMENTS_BY_IMSI_PROPERTY + "' property not found!");
        } else {
            int newBase = Integer.parseInt(ssegments);
            if (newBase != base) {
                base = newBase;
                // System.out.println("sgm=" + base);
            }
        }
    }

    public static void startUpdateTask(Map<String,String> map) {
    }
}