package com.tnf.cas.db;

import static com.datastax.driver.core.querybuilder.QueryBuilder.in;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Component
public class DbSubProfiler implements SubProfiler {
    final static List<String> EMPTY = new LinkedList<String>();
    final static Logger mTrace = LoggerFactory.getLogger(DbSubProfiler.class);

    private ImsiResolver mImsiResolver;
    @Autowired
    private Session mSession;

    @Override
    public String getProfiles(Set<String> imsis, Set<String> msisdns) {
        String result = "";
        HashSet<String> all = new HashSet<String>();
        if (mImsiResolver != null) {
            Set<String> resolved = mImsiResolver.find(msisdns).getImsis();
            if (resolved != null && resolved.size() > 0) {
                all.addAll(resolved);
            }
        }
        if (imsis != null && imsis.size() > 0) {
            all.addAll(imsis);
        }
        result = getProfilesForImsis(all);
        return result;
    }

    public String getProfilesForImsis(Set<String> imsis) {
        String result = "";
        if (imsis != null && imsis.size() > 0) {
            Statement stm = QueryBuilder//
                    .select().json()//
                    .from("hrcc_subscriber")//
                    .where(in("imsi", new ArrayList<String>(imsis)));

            mTrace.debug("{}", stm);

            try {
                ResultSet rows = mSession.execute(stm);
                for (Row row : rows) {
                    if (result.length() > 0) {
                        result += ",";
                    }
                    result += row.getString(0);
                }
            } catch (Exception e) {
                mTrace.error("{}", e);

            }
        }
        return "[" + result + "]";
    }

    public ImsiResolver getImsiResolver() {
        return mImsiResolver;
    }

    @Autowired
    @Override
    public void setImsiResolver(ImsiResolver r) {
        mImsiResolver = r;
    }
}
