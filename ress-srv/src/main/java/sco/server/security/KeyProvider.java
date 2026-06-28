package sco.server.security;

public interface KeyProvider {

	byte[] getKey(String idx);

	KeyRecord getCurrentKey();

	Authenticator getAuthenticator();

}
