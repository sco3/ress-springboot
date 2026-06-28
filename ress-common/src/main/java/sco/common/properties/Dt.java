package sco.common.properties;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sco.common.CasCommonConstants;
import sco.partition.CommonBaseShiftTime;

public class Dt extends CommonBaseShiftTime {

	private static final Logger LOGGER = LoggerFactory.getLogger(Dt.class);

	private static final int PERIOD_IN_SECONDS = 600;
	private static final String DEFAULT_TIME_PERIOD = "D";
	private ScheduledExecutorService mScheduler;
	private static Dt INSTANCE;
	protected final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();
	protected final ReadLock mReadLock = mLock.readLock();
	protected final WriteLock mWriteLock = mLock.writeLock();

	public Map<String, String> mTableToTimePeriodMap = null;
	public String timeZoneId = "";
	public String localeId = "";

	public static Dt singleton() {
		if (INSTANCE == null) {
			INSTANCE = new Dt();
		}
		return INSTANCE;
	}

	public static String getDt(String timeId, String tableName) {
		return singleton().calculateDt(timeId, tableName);
	}

	private String calculateDt(String timeId, String tableName) {
		String timeUnit = findTimePeriodForTable(tableName);
		return shiftTime(timeId, timeUnit);
	}

	private String findTimePeriodForTable(String tableName) {
		String timePeriodForTable = null;
		if (tableName != null) {
			mReadLock.lock();
			try {
				timePeriodForTable = mTableToTimePeriodMap.get(tableName);
				if (timePeriodForTable == null) {
					mReadLock.unlock();
					mWriteLock.lock();
					try {
						timePeriodForTable = "?";
						String propertyName = new StringJoiner(".") //
								.add(CasCommonConstants.DT_PERIOD_FOR_TABLE_PROPERTY_PREFIX)//
								.add(tableName) //
								.toString();
						LOGGER.warn("Property '{}' not found. Default value will be used:'{}'", propertyName,
								timePeriodForTable);
						mTableToTimePeriodMap.put(tableName, timePeriodForTable);
					} catch (Exception ex) {
						LOGGER.error("", ex);
					} finally {
						mReadLock.lock();
						mWriteLock.unlock();
					}
				}
			} finally {
				mReadLock.unlock();
			}
		} else {
			timePeriodForTable = DEFAULT_TIME_PERIOD;
		}
		return timePeriodForTable;
	}

}
