package sco.server.db;

public interface PartKeyCalculator {
    public String getDt(String timeId, String tableName);

    public String getSgm(String imsi, String tableName);
}
