package sco.server.db;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Component
public class DbImsiResolver implements ImsiResolver {
	Logger mTrace = LoggerFactory.getLogger(DbImsiResolver.class);

	@Autowired
	private Session mSession;
	private ConsistencyLevel mConsistencyLevel = ConsistencyLevel.QUORUM;

	private boolean mLogged = false;

	public MsisdnSearchResult find(Set<String> msisdns) {
		MsisdnSearchResult imsis = new MsisdnSearchResult();
		if (msisdns != null && msisdns.size() > 0) {
			for (String msisdn : msisdns) {
				Statement stm = QueryBuilder//
						.select("imsi")//
						.from("hrcc_msisdn_imsi")//
						.where(eq("msisdn", msisdn));
				stm.setConsistencyLevel(mConsistencyLevel);

				mTrace.debug("{}", stm);

				ResultSet rs = mSession.execute(stm);
				boolean found = false;
				for (Row row : rs) {
					found = true;
					imsis.getImsis().add(row.getString(0));
				}
				if (found == false) {
					imsis.getBadMsisdns().add(msisdn);
				}
			}
		}
		return imsis;
	}

	public ConsistencyLevel getConsistencyLevel() {
		return mConsistencyLevel;
	}

	@Value("${find.consistency.level:QUORUM}")
	public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
		mConsistencyLevel = consistencyLevel;
		if (!mLogged) {
			mLogged = true;
			mTrace.info("Query consistency level: {}", mConsistencyLevel);
		}
	}
}
