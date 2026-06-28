package com.tnf.cas.db;

import com.google.common.base.Charsets;

public interface Blobber {
    default byte[] save(String s) {
        return s.getBytes(Charsets.UTF_8);
    }

    default String restore(byte[] b) {
        return new String(b, Charsets.UTF_8);
    }
}
