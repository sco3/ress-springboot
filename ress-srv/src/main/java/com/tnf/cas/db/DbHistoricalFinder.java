package com.tnf.cas.db;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gte;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lt;
import static com.tnf.bis.common.db.CassandraDatasourceFactory.CASSANDRA_KEYSPACE;
import static com.tnf.cas.db.TimeSniper.getDates;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.core.utils.Bytes;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tnf.cas.provider.BadParameters;
import com.tnf.cas.provider.NoDataFound;

/**
 * 
 * @author dz
 *
 */

@Component
public class DbHistoricalFinder implements HistoricalFinder {
	public static final String MISSING_PARAMS = "Missing Mandatory Parameter(s)";
	public static final String NO_DATA_FOUND_FOR_IMSI_OR_MSISDN = (//
	"No data found for IMSI or MSISDN in the specified timeperiod"//
	);
	public static boolean VALIDATE_NUMERIC_IMSI_MSISDN = true;
	private static final String TIMEID = "timeid";
	private static final String IMSI = "imsi";
	private static final String SGM = "sgm";
	private static final String DT = "dt";
	Logger mTrace = LoggerFactory.getLogger(DbHistoricalFinder.class);
	private static final String NUMBERS_ONLY = "[0-9]+";

	private Session mSession;
	private ImsiResolver mImsiResolver;
	private String mColumnListCommasSeparated = null;
	private String[] mColumns;

	PartKeyCalculator mPkCalc = new CasPropPartKeyCalculator();
	private String mKeySpace;

	private String mMin5Table = "hrcc_historical_min_5";
	private String mHourlyTable = "hrcc_historical_h_1";
	private String mDailyTable = "hrcc_historical_d_1";
	private boolean mSearchTimeFilter = true;
	private boolean mSingleQuery = false;
	private ConsistencyLevel mConsistencyLevel = ConsistencyLevel.QUORUM;
	private boolean mLogged = false;

	public void setPartKeyCalculator(PartKeyCalculator calc) {
		mPkCalc = calc;
	}

	private Set<String> getImsis(Set<String> imsis, Set<String> msisdns, Set<String> badMsisdns) {
		Set<String> result = new HashSet<String>();
		if (imsis != null && imsis.size() > 0) {
			result.addAll(imsis);
		}

		if (msisdns != null && msisdns.size() > 0) {
			if (mImsiResolver != null) {
				MsisdnSearchResult findResult = mImsiResolver.find(msisdns);
				Set<String> foundImsis = findResult.getImsis();
				if (foundImsis != null && foundImsis.size() > 0) {
					result.addAll(foundImsis);
				}
				Set<String> bad = findResult.getBadMsisdns();
				if (bad != null && bad.size() > 0) {
					badMsisdns.addAll(bad);
				}
			}
		}

		return result;
	}

	@Override
	public void processResultSets(//
			Set<String> imsis, Set<String> msisdns, //
			String aggr, String timefrom, String timeto, //
			StringBuilder sb //
	) throws BadRequestException {
		boolean ok = true;
		checkAllEmpty(imsis, msisdns, aggr, timefrom, timeto);

		StringJoiner msg = new StringJoiner(", ");

		try {
			Set<String> badMsisdns = new HashSet<String>();
			Set<String> badImsis = new HashSet<String>();
			if (VALIDATE_NUMERIC_IMSI_MSISDN) {
				for (String imsi : imsis) {
					if (!imsi.matches(NUMBERS_ONLY)) {
						badImsis.add(imsi);
					}
				}
				for (String msisdn : msisdns) {
					if (!msisdn.matches(NUMBERS_ONLY)) {
						badMsisdns.add(msisdn);
					}
				}
				if (badImsis.size() > 0) {
					msg.add("Wrong value for Parameter imsi");
					ok = false;
				}
				if (badMsisdns.size() > 0) {
					msg.add("Wrong value for Parameter msisdn");
					ok = false;
				}
				if (!ok) {
					throw new BadParameters(msg.toString());
				}
			}

			imsis = getImsis(imsis, msisdns, badMsisdns);
			if (badMsisdns != null && badMsisdns.size() > 0) {
				msg.add(NO_DATA_FOUND_FOR_IMSI_OR_MSISDN);
				ok = false;
				throw new NoDataFound(msg.toString());
			}
			String table = null;
			String unit = null;
			if ("h".equalsIgnoreCase(aggr)) {
				table = mHourlyTable;
				unit = "H";
			} else if ("d".equalsIgnoreCase(aggr)) {
				table = mDailyTable;
				unit = "D";
			} else if ("5min".equalsIgnoreCase(aggr)) {
				table = mMin5Table;
				unit = "5MIN";
			}

			SortedSet<String> timeIds = null;
			if (unit == null || table == null || table.isEmpty()) {
				msg.add("Wrong aggregation period:" + aggr);
				ok = false;
				throw new BadParameters(msg.toString());
			}
			if (imsis == null || imsis.size() == 0) {
				msg.add("Wrong IMSI or MSISDN:" + imsis + " " + msisdns);
				ok = false;
				throw new BadParameters(msg.toString());
			}
			timeIds = getDates(timefrom, timeto, unit);
			if (timeIds == null || timeIds.size() == 0) {
				msg.add(TimeSniper.WRONG_TIME_PERIOD);
				ok = false;
				throw new BadParameters(msg.toString());
			}

			if (ok) {
				if (mSingleQuery) {
					singleQuery(timeIds, imsis, table, sb);
				} else {
					multipleAsyncQueries(timeIds, imsis, table, sb);
				}
			}
		} catch (NoDataFound | BadParameters e) {
			throw e;
		} catch (Exception e) {
			mTrace.error("{}", e);
			throw new InternalServerErrorException(msg.toString());
		}
		if (!ok) {
			throw new InternalServerErrorException(msg.toString());
		}
	}

	public static void checkAllEmpty(Set<String> imsis, Set<String> msisdns, String aggr, String timefrom,
			String timeto) {
		if ((imsis == null || imsis.size() == 0) //
				&& (msisdns == null || msisdns.size() == 0) //
				&& (aggr == null || aggr.isEmpty()) //
				&& (timefrom == null || timefrom.isEmpty()) //
				&& (timeto == null || timeto.isEmpty())) {

			throw new BadParameters(MISSING_PARAMS);

		}
	}

	public Map<PartToken, Set<String>> findPartitionKeys(//
			String table, Set<String> timeIds, Set<String> imsis//
	) {
		Map<PartToken, Set<String>> parts = new HashMap<PartToken, Set<String>>();
		for (String timeid : timeIds) {
			for (String imsi : imsis) {
				PartToken tok = new PartToken( //
						mPkCalc.getDt(timeid, table), //
						mPkCalc.getSgm(imsi, table) //
				);
				Set<String> imsiSet = parts.get(tok);
				if (imsiSet == null) {
					imsiSet = new HashSet<String>();
					parts.put(tok, imsiSet);
				}
				imsiSet.add(imsi);
			}
		}
		return parts;
	}

	private void multipleAsyncQueries(//
			SortedSet<String> strTimeIds, Set<String> imsis, String table, //
			StringBuilder sb //
	) {

		String minTimeStr = strTimeIds.first();
		String maxTimeStr = strTimeIds.last();
		Long minTime = null;
		Long maxTime = null;
		ArrayList<Long> timeIds = new ArrayList<Long>(strTimeIds.size());
		minTime = new Long(minTimeStr);
		maxTime = new Long(maxTimeStr);
		for (String t : strTimeIds) {
			timeIds.add(new Long(t));
		}

		try {
			Map<PartToken, Set<String>> parts = findPartitionKeys(//
					table, strTimeIds, imsis//
			);
			String cqls = "";

			List<ResultSetFuture> futures = new LinkedList<ResultSetFuture>();
			for (Entry<PartToken, Set<String>> entry : parts.entrySet()) {
				PartToken tok = entry.getKey();
				Set<String> imsiSet = entry.getValue();
				String dt = tok.getDt();
				String sgm = tok.getSgm();

				for (String imsiStr : imsiSet) {

					Statement stm;
					Select select;

					if (mColumns == null) {
						select = QueryBuilder.select().from(table);
					} else {
						select = QueryBuilder.select(mColumns).from(table);
					}

					Where where = select //
							.where(eq(DT, dt))//
							.and(eq(SGM, sgm))//
							.and(eq(IMSI, imsiStr)//
							);//

					if (mSearchTimeFilter) {
						stm = where//
								.and(gte(TIMEID, minTime))//
								.and(lt(TIMEID, maxTime)//
								);
					} else {
						stm = where;
					}

					stm.setConsistencyLevel(mConsistencyLevel);
					if (mTrace.isDebugEnabled()) {
						cqls += "\n" + stm.toString();
					}

					ResultSetFuture f = getSession().executeAsync(stm);
					futures.add(f);
				}
			}
			mTrace.debug("CQLs:\n{}\n", cqls);
			List<ListenableFuture<ResultSet>> rss = Futures.inCompletionOrder(futures);

			for (ListenableFuture<ResultSet> future : rss) {
				ResultSet rs = future.get();
				processFlat(rs, sb);
			}

		} catch (Exception e) {
			mTrace.error("{}", e);
		}

	}

	private void singleQuery(//
			SortedSet<String> timeIds, Set<String> imsis, String table, StringBuilder sb //
	) {
		String minTimeStr = timeIds.first();
		String maxTimeStr = timeIds.last();
		Long minTime = new Long(minTimeStr);
		Long maxTime = new Long(maxTimeStr);

		Set<String> dts = new TreeSet<String>();
		for (String t : timeIds) {
			dts.add(mPkCalc.getDt(t, table));
		}
		Set<String> sgms = new TreeSet<String>();
		for (String imsi : imsis) {
			sgms.add(mPkCalc.getSgm(imsi, table));
		}

		Statement stm;
		Select select;

		if (mColumns == null) {
			select = QueryBuilder.select().from(table);
		} else {
			select = QueryBuilder.select(mColumns).from(table);
		}

		Where where = select //
				.where(in(IMSI, new ArrayList<String>(imsis))) //
				.and(in(DT, new ArrayList<String>(dts)))//
				.and(in(SGM, new ArrayList<String>(sgms))//

				);

		if (mSearchTimeFilter) {
			stm = where//
					.and(gte(TIMEID, minTime))//
					.and(lt(TIMEID, maxTime)//
					);
		} else {
			stm = where;
		}
		stm.setConsistencyLevel(mConsistencyLevel);

		mTrace.debug("CQL:\n{}\n", stm.toString());

		ResultSet rs = getSession().execute(stm);
		processFlat(rs, sb);

	}

	public void addValue(StringBuilder head, String key, String value) {
		if (head != null && head.length() > 1) {
			head.append(",");
		}
		head.append("\"").append(key).append("\":\"")//
				.append(value).append("\"");
	}

	public void addArray(StringBuilder head, String key, StringBuilder value) {
		if (head != null && head.length() > 1) {
			head.append(",");
		}
		head.append("\"").append(key).append("\":[").append(value).append("]");
	}

	public void addObject(StringBuilder result, StringBuilder values) {
		if (result != null && result.length() > 1) {
			result.append(",");
		}
		result.append("{").append(values).append("}");
	}

	public void addObject(StringBuilder result, String values) {
		if (result != null && result.length() > 1) {
			result.append(",");
		}
		result.append(values);
	}

	public void processFlat(//
			ResultSet rs, StringBuilder result //
	) {

		for (Row row : rs) {
			// if (getProperty(NO_TIMEID_FILTER) != null) {
			// String timeid = null;
			// for (Definition col : rs.getColumnDefinitions().asList()) {
			// String name = col.getName();
			// if (TIMEID.equalsIgnoreCase(name)) {
			// timeid = row.getString(name);
			// break;
			// }
			// }
			// if (//
			// timeid == null //
			// || timeid.compareTo(minTimeId) < 0 //
			// || timeid.compareTo(maxTimeId) > 0//
			// ) {
			// continue;
			// }
			// }

			StringBuilder rowNode = new StringBuilder();
			StringBuilder metrics = new StringBuilder();
			for (Definition col : rs.getColumnDefinitions().asList()) {
				String name = col.getName();
				if (IMSI.equalsIgnoreCase(name)) {
					String imsi = row.getString(name);
					addValue(rowNode, IMSI, imsi);
				} else if (TIMEID.equalsIgnoreCase(name)) {
					String time;
					if (col.getType().equals(DataType.bigint())) {
						time = Long.toString(row.getLong(name));
					} else {
						time = row.getString(name);
					}
					addValue(rowNode, "time", time);
				} else if (DT.equalsIgnoreCase(name)) {
					// skip
				} else if (SGM.equalsIgnoreCase(name)) {
					// skip
				} else {
					String val = null;
					if (col.getType().equals(DataType.blob())) {
						ByteBuffer bb = row.getBytes(name);
						if (bb != null) {
							byte[] b = Bytes.getArray(bb);
							val = BlobberRegistry.getBlobber().restore(b);
						}
						mTrace.trace("From blob: {} {}", bb, val);
					} else {
						try {
							val = row.getString(name);
						} catch (Exception e) {
							// ignore not string compatible columns
						}
					}
					if (val != null) {
						addObject(metrics, val);
					}
				}
			}
			addArray(rowNode, "metrics", metrics);
			addObject(result, rowNode);
		}
	}

	public Session getSession() {
		return mSession;
	}

	@Autowired
	public void setSession(Session session) {
		mSession = session;
	}

	public ImsiResolver getImsiResolver() {
		return mImsiResolver;
	}

	@Autowired
	public void setImsiResolver(ImsiResolver imsiResolver) {
		mImsiResolver = imsiResolver;
	}

	public String getColumnListCommasSeparated() {
		return mColumnListCommasSeparated;
	}

	@Value("${column.list.comma.separated:*}")
	public void setColumnListCommasSeparated(String columns) {
		if (columns != null && !"*".equals(columns)) {
			mColumnListCommasSeparated = columns;
			mColumns = mColumnListCommasSeparated.split(Pattern.quote(","));
		}
	}

	public String getKeySpace() {
		return mKeySpace;
	}

	@Value("${" + CASSANDRA_KEYSPACE + "}")
	public void setKeySpace(String keySpace) {
		mKeySpace = keySpace;
	}

	@Override
	public String find(Set<String> imsis, Set<String> msisdns, String aggr, String timefrom, String timeto) {
		StringBuilder result = new StringBuilder();
		result.append("[");
		processResultSets(imsis, msisdns, aggr, timefrom, timeto, result);
		result.append("]");
		if (mTrace.isDebugEnabled()) {
			mTrace.debug(result.toString());
		}
		String body = result.toString();
		if ("[]".equals(body)) {
			String message = NO_DATA_FOUND_FOR_IMSI_OR_MSISDN;
			throw new NoDataFound(message);
		}
		return body;
	}

	public String getMin5Table() {
		return mMin5Table;
	}

	public void setMin5Table(String min5Table) {
		mMin5Table = min5Table;
	}

	public String getHourlyTable() {
		return mHourlyTable;
	}

	public void setHourlyTable(String hourlyTable) {
		mHourlyTable = hourlyTable;
	}

	public String getDailyTable() {
		return mDailyTable;
	}

	public void setDailyTable(String dailyTable) {
		mDailyTable = dailyTable;
	}

	public boolean isSearchTimeFilter() {
		return mSearchTimeFilter;
	}

	@Value("${search.time.filter}")
	public void setSearchTimeFilter(boolean searchTimeFilter) {
		mSearchTimeFilter = searchTimeFilter;
	}

	public boolean isSingleQuery() {
		return mSingleQuery;
	}

	public void setSingleQuery(boolean singleQuery) {
		mSingleQuery = singleQuery;
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
