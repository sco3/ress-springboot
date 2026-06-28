package com.tnf.cas.security;

import org.mortbay.jetty.security.HashUserRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tnf.cas.web.WebServerConstants;

public class PropsFileUserStorage implements WebServerConstants {
	Logger mTrace = LoggerFactory.getLogger(PropsFileUserStorage.class);

	private String mRealmConfig;

	private HashUserRealm mUserRealm;

	public HashUserRealm getUserRealm() {
		try {
			if (mUserRealm == null) {
				mRealmConfig = Thread.currentThread()//
						.getContextClassLoader()//
						.getResource(WEBSERVER_PASSWD).toString();
				mUserRealm = new HashUserRealm();
				mUserRealm.setConfig(mRealmConfig);
			}
		} catch (Exception e) {
			mTrace.error("{}", e);
		}
		return mUserRealm;
	}

	public boolean isValid(String user, String pass) {
		boolean result = false;
		if (null != getUserRealm().authenticate(user, pass, null)) {
			result = true;
		}
		return result;
	}
}
