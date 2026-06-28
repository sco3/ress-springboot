package com.tnf.cas.provider;

public class InformerRecord {
    long mStart;
    long mDuration;
    int mTimeIds;
    boolean mSecure;
    int mMetrics;
    String mTable;
    String mUser;

    public InformerRecord(//
            long start, long duration, int timeIds, //
            boolean secure, int metrics, String table, //
            String user //
    ) {
        mStart = start;
        mDuration = duration;
        mTimeIds = timeIds;
        mSecure = secure;
        mMetrics = metrics;
        mTable = table;
        mUser = user;
    }
}
