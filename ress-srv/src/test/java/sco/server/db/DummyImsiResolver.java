package sco.server.db;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import sco.server.db.ImsiResolver;
import sco.server.db.MsisdnSearchResult;

public class DummyImsiResolver implements ImsiResolver {

    private static final String COMMA = Pattern.quote(",");
    HashMap<String, String[]> mMap = null;

    private String[] get(String s) {
        if (mMap == null) {
            mMap = new HashMap<String, String[]>();
            mMap.put("m1", "i1-1".split(COMMA));
            mMap.put("m2", "i2-1,i2-2".split(COMMA));
            mMap.put("m3", "i3-1,i3-2,i3-3".split(COMMA));
        }
        return mMap.get(s);
    }

    @Override
    public MsisdnSearchResult find(Set<String> msisdns) {
        MsisdnSearchResult result = new MsisdnSearchResult();
        for (String msisdn : msisdns) {
            String[] imsis = get(msisdn);
            if (imsis != null) {
                for (String imsi : imsis)
                    result.getImsis().add(imsi);
            }
        }
        return result;
    }
}
