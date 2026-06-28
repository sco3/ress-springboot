package com.tnf.cas.security;

public interface Authenticator {

	byte[] sign(byte[] msg, byte[] secret);

	byte[] generateSecret();

	byte[] restoreKey(String permSecret, byte[] keySecret);

}