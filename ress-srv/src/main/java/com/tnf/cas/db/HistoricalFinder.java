package com.tnf.cas.db;

import java.util.Set;

public interface HistoricalFinder {

    String find(//
            Set<String> imsis, Set<String> msisdns, //
            String aggr, String timefrom, String timeto//
    );

    void processResultSets(//
            Set<String> imsis, Set<String> msisdns, //
            String aggr, String timefrom, //
            String timeto, StringBuilder sb//
    );

}