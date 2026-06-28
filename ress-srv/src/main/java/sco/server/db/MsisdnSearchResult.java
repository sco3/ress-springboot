package sco.server.db;

import java.util.HashSet;
import java.util.Set;

public class MsisdnSearchResult {
    Set<String> mImsis = new HashSet<String>();
    Set<String> mBadMsisdns = new HashSet<String>();

    public Set<String> getImsis() {
        return mImsis;
    }

    public void setImsis(Set<String> imsis) {
        mImsis = imsis;
    }

    public Set<String> getBadMsisdns() {
        return mBadMsisdns;
    }

    public void setBadMsisdns(Set<String> badMsisdns) {
        mBadMsisdns = badMsisdns;
    }
}
