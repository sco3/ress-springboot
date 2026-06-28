package com.tnf.cas.db;

import sco.common.properties.Dt;
import sco.common.properties.Sgm;

public class CasPropPartKeyCalculator implements PartKeyCalculator {

    @Override
    public String getDt(String timeId, String tableName) {
        return Dt.getDt(timeId, tableName);
    }

    @Override
    public String getSgm(String imsi, String table) {
        return Sgm.getSgm(imsi);
    }
}
