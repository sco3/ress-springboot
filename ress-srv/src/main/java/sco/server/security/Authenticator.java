package sco.server.security;

public interface Authenticator {

	byte[] sign(byte[] msg, byte[] secret);

	byte[] generateSecret();

	byte[] restoreKey(String permSecret, byte[] keySecret);

}