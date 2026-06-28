package sco.security;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import sco.web.WebServerConstants;

public class SingleKeyProvider implements KeyProvider, WebServerConstants {
	static Logger mTrace = LoggerFactory.getLogger(SingleKeyProvider.class);
	final static String SIMPLE = ("" //
			+ "246kOshzbgRM++UMg5V1/oSvV7AGAsW0JiS8kTqIKdCxs0ykVuOD2jxj0BhhorOUY5dvLL6kxYAC7\n"
			+ "wQGaMm6kBeP5pktsSYCgL2nXJXpfHvgsolNt0DqCXP6IuWg6VXnxJzgyUXlIS/o24a/Lz5OI5rta\n"
			+ "YpTsidjnmt0LLTKFyyGTaKIOXBtz0fSLx9TLutMi2WrtmFIurnt970ZF0rBixniJAze3diDxworp\n"
			+ "gpM+9p5yTwC24k/bLxJF2Ofl2MxLpWHQitWZrRDvtgP2eA+SzNaVmEPuwXr5Dm554xf+UvS1gWzr\n"
			+ "dTX7k5sr8vkIAVCNwsja6O6Na7osrVyxuf2iTg==\n" //
	);

	byte[] mKey = null;

	private Authenticator mAuthenticator;

	@Value("${" + AUTH_SECRET + "}")
	private String mSecret;

	@Override
	public byte[] getKey(String i) {
		if (mKey == null && getAuthenticator() != null) {
			mKey = getAuthenticator().restoreKey(//
					mSecret, //
					Base64.decodeBase64(SIMPLE) //
			);
		}
		return mKey;
	}

	public KeyRecord getCurrentKey() {
		KeyRecord record = new KeyRecord();
		record.setIndex("");
		record.setSecret(getKey(""));
		return record;
	}

	public Authenticator getAuthenticator() {
		return mAuthenticator;
	}

	public void setAuthenticator(Authenticator authenticator) {
		mAuthenticator = authenticator;
	}

}
