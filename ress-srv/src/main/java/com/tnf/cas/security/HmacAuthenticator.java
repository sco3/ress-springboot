package com.tnf.cas.security;

import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Charsets;

import sco.web.WebServerConstants;

public class HmacAuthenticator implements WebServerConstants, Authenticator {
	static Logger mTrace = LoggerFactory.getLogger(HmacAuthenticator.class);

	@Value("${" + AUTH_METHOD + "}")
	String mAlg;

	@Override
	public byte[] sign(byte[] msg, byte[] secret) {
		byte[] sign = null;
		try {

			SecretKeySpec key = new SecretKeySpec(//
					secret, mAlg//
			);

			Mac mac = Mac.getInstance(mAlg);
			mac.init(key);
			mac.update(msg);
			sign = mac.doFinal();
		} catch (Exception e) {
			mTrace.error("{}", e);
		}
		return sign;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tnf.cas.security.Authenticator#generateSecret()
	 */
	@Override
	public byte[] generateSecret() {
		byte[] result = new byte[256];
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.nextBytes(result);
		} catch (Exception e) {
			result = null;
			mTrace.error("{}", e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tnf.cas.security.Authenticator#restoreKey(java.lang.String,
	 * byte[])
	 */
	@Override
	public byte[] restoreKey(String permSecret, byte[] keySecret) {
		byte[] result = new byte[256];
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			if (permSecret != null) {
				random.setSeed(permSecret.getBytes(Charsets.UTF_8));
			} else {
				mTrace.warn("Secret was not defined, key is random.");
			}
			random.nextBytes(result);
			result = sign(result, keySecret);
		} catch (Exception e) {
			result = null;
			mTrace.error("{}", e);
		}
		return result;
	}
}
