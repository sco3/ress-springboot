package com.tnf.cas.db;

//import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;

public class TestPartitionKeys {

    @Test
    public void test() {
        String[] timeArray = { //
                "20161117000000", "20161117010000", "20161117020000",
                "20161117030000", "20161117040000", "20161117050000",
                "20161117060000", "20161117070000", "20161117080000",
                "20161117090000", "20161117100000", "20161117110000",
                "20161117120000", "20161117130000", "20161117140000",
                "20161117150000", "20161117160000", "20161117170000",
                "20161117180000", "20161117190000", "20161117200000",
                "20161117210000", "20161117220000", "20161117230000",
                "20161118000000", "20161118010000", "20161118020000",
                "20161118030000", "20161118040000", "20161118050000",
                "20161118060000", "20161118070000", "20161118080000",
                "20161118090000", "20161118100000", "20161118110000",
                "20161118120000", "20161118130000", "20161118140000",
                "20161118150000", "20161118160000", "20161118170000",
                "20161118180000", "20161118190000", "20161118200000",
                "20161118210000", "20161118220000", "20161118230000" //
        };

        HashSet<String> timeIds = new HashSet<String>(Arrays.asList(timeArray));
        Assert.assertEquals(48, timeIds.size());
        HashSet<String> imsis = new HashSet<String>(//
                Arrays.asList(new String[] { "imsi1" })//
        );

        DbHistoricalFinder finder = new DbHistoricalFinder();
        finder.setPartKeyCalculator(new TestCalc());
        Map<PartToken, Set<String>> p = finder.findPartitionKeys(//
                "", timeIds, imsis//
        );
        int cnt = 0;
        for (Entry<PartToken, Set<String>> e : p.entrySet()) {
            System.out.println(e);
            cnt++;
        }
        Assert.assertEquals(2, cnt);
    }
}
