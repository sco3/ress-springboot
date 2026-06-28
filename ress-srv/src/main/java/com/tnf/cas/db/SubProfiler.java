package com.tnf.cas.db;

import java.util.Set;

public interface SubProfiler {

	String getProfiles(Set<String> imsis, Set<String> msisdns);

	String getProfilesForImsis(Set<String> imsis);

	void setImsiResolver(ImsiResolver r);

}