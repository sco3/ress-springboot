package com.tnf.cas.security;

public interface KeyProvider {

	byte[] getKey(String idx);

	KeyRecord getCurrentKey();

	Authenticator getAuthenticator();

}
