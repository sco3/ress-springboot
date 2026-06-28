package com.tnf.bis.common.util;

public class ObjectsHelper {

    private ObjectsHelper() {
    }

    public static String identityToString(Object obj) {
        return "@" + Integer.toHexString(System.identityHashCode(obj));
    }
}
