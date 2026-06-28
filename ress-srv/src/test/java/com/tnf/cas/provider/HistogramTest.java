package com.tnf.cas.provider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HistogramTest {

    @Test
    public void test() {
        Informer inf = new Informer();
        String m = "";
        for (int i = 0; i < Informer.mHistMargins.length; i++) {
            System.out.println(Informer.mHistMargins[i]);
            m += "|" + Informer.mHistMargins[i];
        }
        System.out.println(m);
        assertEquals(9, Informer.mHistMargins.length);
        assertEquals("|500|1000|1500|2000|2500|3000|3500|4000|2147483647", m);

        inf.update(0, 100, 0, false, 0);
        inf.update(0, 1000, 0, false, 0);
        String h = inf.calc();
        System.out.println(h);
        assertEquals("~50~50~0~0~0~0~0~0~0", h);

        inf.update(0, 100, 0, false, 0);
        inf.update(0, 1000, 0, false, 0);
        inf.update(0, 2000, 0, false, 0);
        h = inf.calc();
        System.out.println(h);
        assertEquals("~33~33~0~33~0~0~0~0~0", h);

        inf.update(0, 1000, 0, false, 0);
        inf.update(0, 1000, 0, false, 0);
        inf.update(0, 1000, 0, false, 0);
        inf.update(0, 1000, 0, false, 0);

        inf.update(0, 2000, 0, false, 0);
        inf.update(0, 2000, 0, false, 0);
        inf.update(0, 2000, 0, false, 0);
        inf.update(0, 2000, 0, false, 0);

        inf.update(0, 8000, 0, false, 0);
        inf.update(0, 8000, 0, false, 0);

        h = inf.calc();
        System.out.println(h);
        assertEquals("~0~40~0~40~0~0~0~0~20", h);

    }
}
