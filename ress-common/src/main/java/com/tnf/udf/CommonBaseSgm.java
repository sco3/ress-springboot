package com.tnf.udf;

import java.util.zip.CRC32;

/**
 * 
 * User defined function for imsi hash calculated as a modulus of 10 of crc32 of
 * the given imsi string
 * 
 * @author dz
 */

public class CommonBaseSgm {
    protected static int base = Integer.MIN_VALUE;

    public static void setBase(int aBase) {
        base = aBase;
    }

    public static int sgm(String s) {
        if (base < 0) {
            throw new RuntimeException("base is not set for sgm()!");
        }
        int result = -1;
        if (s != null && s.length() > 0) {
            CRC32 crc = new CRC32();
            byte[] b = s.getBytes();
            crc.update(b, 0, b.length);
            result = (int) (crc.getValue() % base);
        }
        return result;
    }
}
