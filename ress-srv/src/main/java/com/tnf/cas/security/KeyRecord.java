package com.tnf.cas.security;

public class KeyRecord {
    private String mIndex;
    private byte[] mSecret;

    public String getIndex() {
        return mIndex;
    }

    public void setIndex(String index) {
        mIndex = index;
    }

    public byte[] getSecret() {
        return mSecret;
    }

    public void setSecret(byte[] secret) {
        mSecret = secret;
    }

}
