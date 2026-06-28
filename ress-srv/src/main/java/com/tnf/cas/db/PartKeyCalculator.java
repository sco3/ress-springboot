package com.tnf.cas.db;

public interface PartKeyCalculator {
    public String getDt(String timeId, String tableName);

    public String getSgm(String imsi, String tableName);
}
