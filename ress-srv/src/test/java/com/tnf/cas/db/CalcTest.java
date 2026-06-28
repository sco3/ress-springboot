package com.tnf.cas.db;

public class CalcTest implements PartKeyCalculator {

    @Override
    public String getDt(String timeId, String tableName) {
        return timeId.substring(0, 8) + "0000";
    }

    @Override
    public String getSgm(String imsi, String table) {
        return imsi.substring(imsi.length() - 1, imsi.length());
    }
}
