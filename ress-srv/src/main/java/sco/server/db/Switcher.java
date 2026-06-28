package sco.server.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Switcher {
    private boolean mTableStat = false;
    private boolean mUserStat = false;
    private boolean mCompress = true;

    @Autowired
    DbHistoricalFinder mFinder;

    public String getMin5Table() {
        return mFinder.getMin5Table();
    }

    public void setMin5Table(String min5Table) {
        mFinder.setMin5Table(min5Table);
    }

    public String getHourlyTable() {
        return mFinder.getHourlyTable();
    }

    public void setHourlyTable(String hourlyTable) {
        mFinder.setHourlyTable(hourlyTable);
    }

    public String getDailyTable() {
        return mFinder.getDailyTable();
    }

    public void setDailyTable(String dailyTable) {
        mFinder.setDailyTable(dailyTable);
    }

    public boolean isSearchTimeFilter() {
        return mFinder.isSearchTimeFilter();
    }

    public void setSearchTimeFilter(boolean value) {
        mFinder.setSearchTimeFilter(value);
    }

    @Value("${rest.table.stat}")
    public boolean isTableStat() {
        return mTableStat;
    }

    public void setTableStat(boolean tableStat) {
        mTableStat = tableStat;
    }

    public boolean isUserStat() {
        return mUserStat;
    }

    @Value("${rest.user.stat}")
    public void setUserStat(boolean userStat) {
        mUserStat = userStat;
    }

    public boolean isSingleQuery() {
        return mFinder.isSingleQuery();
    }

    public void setSingleQuery(boolean singleQuery) {
        mFinder.setSingleQuery(singleQuery);
    }

    public String getColumnListCommasSeparated() {
        return mFinder.getColumnListCommasSeparated();
    }

    @Value("${column.list.comma.separated:*}")
    public void setColumnListCommasSeparated(String columns) {
        mFinder.setColumnListCommasSeparated(columns);
    }

    public boolean isCompress() {
        return mCompress;
    }

    @Value("${rest.gzip.compress:false}")
    public void setCompress(boolean compress) {
        mCompress = compress;
    }
}
