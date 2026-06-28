package sco.server.db;

import java.util.Set;

public interface ImsiResolver {
	public MsisdnSearchResult find(Set<String> msisdns);
}