package sco.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sco.web.WebServerConstants;

public class PropsFileUserStorage implements WebServerConstants {
	Logger mTrace = LoggerFactory.getLogger(PropsFileUserStorage.class);

	private String mRealmConfig;
	private Map<String, String> mUsers = new HashMap<>();

	public Map<String, String> getUsers() {
		try {
			if (mUsers.isEmpty()) {
				mRealmConfig = Thread.currentThread()//
						.getContextClassLoader()//
						.getResource(WEBSERVER_PASSWD).toString();
				loadUsers();
			}
		} catch (Exception e) {
			mTrace.error("{}", e);
		}
		return mUsers;
	}

	private void loadUsers() throws Exception {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(WEBSERVER_PASSWD);
		if (is == null) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				String[] parts = line.split(":");
				if (parts.length >= 2) {
					mUsers.put(parts[0], parts[1]);
				}
			}
		}
	}

	public boolean isValid(String user, String pass) {
		Map<String, String> users = getUsers();
		return users.containsKey(user) && users.get(user).equals(pass);
	}
}
