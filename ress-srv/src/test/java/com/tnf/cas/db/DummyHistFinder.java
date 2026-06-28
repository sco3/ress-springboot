package com.tnf.cas.db;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tnf.cas.provider.NoDataFound;

import com.datastax.driver.core.ConsistencyLevel;

public class DummyHistFinder implements HistoricalFinder {
	static final String JSON = ("      " //
			+ "[{                      " //
			+ "   'imsi': '353',       " //
			+ "   'time': '2016',      " //
			+ "   'metrics': [{'a':1}] " //
			+ "	},                     " //
			+ "	{                      " //
			+ "	  'imsi': '354',       " //
			+ "	  'time': '2017',      " //
			+ "	  'metrics': [{'b':2}] " //
			+ "}]"//
	);
	ObjectMapper om = new ObjectMapper();
	private ConsistencyLevel mConsistencyLevel = null;

	@Override
	public String find(Set<String> imsis, Set<String> msisdns, String aggr, String timefrom, String timeto) {

		DbHistoricalFinder.checkAllEmpty(imsis, msisdns, aggr, timefrom, timeto);

		String json = JSON.replace('\'', '"');
		ArrayNode result = om.createArrayNode();
		try {
			if (imsis.size() > 0) {

				result = (ArrayNode) (om.readTree(json));
				if (imsis.size() == 1) {
					result.remove(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result.size() == 0) {
			throw new NoDataFound(DbHistoricalFinder.NO_DATA_FOUND_FOR_IMSI_OR_MSISDN);
		}
		return result.toString();
	}

	@Override
	public void processResultSets(Set<String> imsis, Set<String> msisdns, String aggr, String timefrom, String timeto,
			StringBuilder sb) {
	}

	public ConsistencyLevel getConsistencyLevel() {
		return mConsistencyLevel;
	}

	@Value("${find.consistency.level:QUORUM}")
	public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
		mConsistencyLevel = consistencyLevel;
	}

}
