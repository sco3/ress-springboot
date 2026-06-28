package sco.server.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sco.server.db.DbSubProfiler;
import sco.server.db.SubProfiler;

public class DummySubProfiler extends DbSubProfiler implements SubProfiler {
	static final String JSON = ("" //
			+ "[{'imsi': 'imsi2-1', 'address': '21', 'age': null, 'current_plan': null, 'name': 'two'},"
			+ "{'imsi': 'imsi3-1', 'address': '31', 'age': null, 'current_plan': null, 'name': 'three'},"
			+ "{'imsi': 'imsi3-3', 'address': '33', 'age': null, 'current_plan': null, 'name': 'three'},"
			+ "{'imsi': 'imsi2-2', 'address': '22', 'age': null, 'current_plan': null, 'name': 'two'},"
			+ "{'imsi': 'imsi1-1', 'address': '11', 'age': null, 'current_plan': null, 'name': 'one'},"
			+ "{'imsi': '2', 'address': 'a2', 'age': null, 'current_plan': null, 'name': 'tw2'},"
			+ "{'imsi': '1', 'address': 'a1', 'age': null, 'current_plan': null, 'name': 'on1'},"
			+ "{'imsi': 'imsi3-2', 'address': '32', 'age': null, 'current_plan': null, 'name': 'three'}]" //
	);

	Map<String, JsonNode> mMap = new HashMap<String, JsonNode>();

	public DummySubProfiler() {
		setImsiResolver(new DummyImsiResolver());
		ObjectMapper om = new ObjectMapper();
		try {
			JsonNode o = om.readTree(JSON.replace('\'', '"'));
			for (Iterator<JsonNode> i = o.elements(); i.hasNext();) {
				JsonNode node = i.next();
				mMap.put(node.get("imsi").asText(), node);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getProfilesForImsis(Set<String> set) {
		String result = "";
		for (String item : set) {
			JsonNode o = mMap.get(item);
			if (o != null) {
				if (result.length() > 0) {
					result += ",";
				}
				result += o.toString();
			}
		}
		return "[" + result + "]";
	}
}
