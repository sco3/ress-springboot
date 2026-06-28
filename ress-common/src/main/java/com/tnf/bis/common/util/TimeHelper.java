package com.tnf.bis.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.tnf.bis.common.timeinterval.IntervalTimeUnit;
import com.tnf.bis.common.timeinterval.TimeInterval;

public class TimeHelper {

    private static final long MILLIS_IN_MINUTE = 1000 * 60L;
    private static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60L;
    public static final String DT_TIMESTAMP_FORMAT = "yyyyMMddHHmm";
    public static final String AGG_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    public static final String TDR_TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final ThreadLocal<SimpleDateFormat> TDR_TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat utcDateFormat = new SimpleDateFormat(TDR_TIMESTAMP_FORMAT);
            utcDateFormat.setTimeZone(UTC);
            return utcDateFormat;
        }
    };
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("UTC");
    public static final Locale DEFAULE_LOCALE = new Locale("en", "US");
    private static TimeZone mTimeZone = DEFAULT_TIME_ZONE;
    private static Locale mLocale = DEFAULE_LOCALE;
    private static ReentrantReadWriteLock timeZoneRWLock = new ReentrantReadWriteLock();

    private TimeHelper() {
    }

    public static void setTimeZone(TimeZone timeZone, Locale locale) {
        timeZoneRWLock.writeLock().lock();
        try {
            mTimeZone = timeZone;
            mLocale = locale;
        } finally {
            timeZoneRWLock.writeLock().unlock();
        }
    }

    public static SimpleDateFormat getUtcDateFormatInstance() {
        return TDR_TIMESTAMP_FORMATTER.get();
    }

    public static long getTime(String timestamp) throws ParseException {
        SimpleDateFormat utcDateFormat = getUtcDateFormatInstance();
        String normalizedTimestamp = normalizeTimestamp(timestamp);
        if (normalizedTimestamp == null) {
            return 0;
        }
        return utcDateFormat.parse(normalizedTimestamp).getTime();
    }

    public static String normalizeTimestamp(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        String normalizedTimestamp = timestamp.trim();
        if (normalizedTimestamp.length() != TDR_TIMESTAMP_FORMAT.length()) {
            if (normalizedTimestamp.length() < TDR_TIMESTAMP_FORMAT.length()) {
                StringBuffer padded = new StringBuffer(normalizedTimestamp);
                for (int i = padded.length(); i < TDR_TIMESTAMP_FORMAT.length(); i++) {
                    if (i == 5 || i == 7) {
                        padded.append("1");
                    } else {
                        padded.append("0");
                    }
                }
                normalizedTimestamp = padded.toString();
            } else {
                normalizedTimestamp = normalizedTimestamp.substring(0, TDR_TIMESTAMP_FORMAT.length());
            }
        }
        return normalizedTimestamp;
    }

    public static String durationFrom(long startMillis) {
        long millis = System.currentTimeMillis() - startMillis;
        return String.format("%d.%03d seconds", millis / 1000, (millis % 1000));
    }

    public static String getBeginningTimeIdForInterval( //
            long timeInMillis, //
            TimeInterval timeInterval //
    ) {
        long millis = getBeginningMillisForInterval(timeInMillis, timeInterval);
        return getUtcDateFormatInstance().format(new Date(millis));
    }

    public static long getBeginningMillisForInterval( //
            long timeInMillis, //
            TimeInterval timeInterval //
    ) {
        int intervalDuration = timeInterval.getIntervalDuration();
        IntervalTimeUnit intervalTimeUnit = timeInterval.getIntervalTimeUnit();
        if (intervalDuration < 1 || intervalTimeUnit == null) {
            throw new IllegalArgumentException();
        }
        TimeZone timeZone = null;
        Locale locale = null;
        timeZoneRWLock.readLock().lock();
        try {
            timeZone = mTimeZone;
            locale = mLocale;
        } finally {
            timeZoneRWLock.readLock().unlock();
        }
        Calendar result = Calendar.getInstance(timeZone, locale);
        result.setTimeInMillis(timeInMillis);
        result.set(Calendar.MILLISECOND, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.HOUR_OF_DAY, 0);
        if (intervalTimeUnit == IntervalTimeUnit.MINUTE || intervalTimeUnit == IntervalTimeUnit.HOUR) {
            long shiftedValue = toLocalHour(result.getTimeInMillis(), timeZone, -1);
            if (intervalTimeUnit == IntervalTimeUnit.MINUTE) {
                return timeInMillis - (timeInMillis - shiftedValue) % (intervalDuration * MILLIS_IN_MINUTE);
            } else {
                return timeInMillis - (timeInMillis - shiftedValue) % (intervalDuration * MILLIS_IN_HOUR);
            }
        }
        if (intervalTimeUnit == IntervalTimeUnit.DAY) {
            int currentValue = result.get(Calendar.DAY_OF_MONTH);
            int newValue = currentValue - (currentValue - 1) % intervalDuration;
            result.set(Calendar.DAY_OF_MONTH, newValue);
            return toLocalHour(result.getTimeInMillis(), timeZone, -1);
        }
        if (intervalTimeUnit == IntervalTimeUnit.WEEK) {
            // next lines works only for intervalDuration = 1
            int firstDayOfWeek = result.getFirstDayOfWeek();
            while (result.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
                result.add(Calendar.DAY_OF_MONTH, -1);
            }
            return toLocalHour(result.getTimeInMillis(), timeZone, -1);
        }
        result.set(Calendar.DAY_OF_MONTH, 1);
        if (intervalTimeUnit == IntervalTimeUnit.MONTH) {
            int currentValue = result.get(Calendar.MONTH);
            int newValue = currentValue - currentValue % intervalDuration;
            result.set(Calendar.MONTH, newValue);
            return toLocalHour(result.getTimeInMillis(), timeZone, -1);
        }
        throw new IllegalArgumentException();
    }

    private static long toLocalHour(long millis, TimeZone timeZone, int direction) {
        int offset = timeZone.getOffset(millis);
        int priorOffset = timeZone.getOffset(millis - MILLIS_IN_HOUR);
        if (priorOffset > offset) {
            millis = millis + direction * MILLIS_IN_HOUR;
        }
        return millis;
    }

    public static String getShiftedTimeId( //
            long timeInMillis, //
            int shift, //
            TimeInterval timeInterval //
    ) {
        long millis = getShiftedMillis(timeInMillis, shift, timeInterval);
        return getUtcDateFormatInstance().format(new Date(millis));
    }

    public static long getShiftedMillis( //
            long timeInMillis, //
            int shift, //
            TimeInterval timeInterval //
    ) {
        if (shift == 0) {
            return timeInMillis;
        }
        int intervalDuration = timeInterval.getIntervalDuration();
        IntervalTimeUnit intervalTimeUnit = timeInterval.getIntervalTimeUnit();
        if (intervalDuration < 1 || intervalTimeUnit == null) {
            throw new IllegalArgumentException();
        }
        int shiftedIntervalDuration = shift * intervalDuration;
        if (intervalTimeUnit == IntervalTimeUnit.MINUTE) {
            return timeInMillis + shiftedIntervalDuration * MILLIS_IN_MINUTE;
        }
        if (intervalTimeUnit == IntervalTimeUnit.HOUR) {
            return timeInMillis + shiftedIntervalDuration * MILLIS_IN_HOUR;
        }
        TimeZone timeZone = null;
        Locale locale = null;
        timeZoneRWLock.readLock().lock();
        try {
            timeZone = mTimeZone;
            locale = mLocale;
        } finally {
            timeZoneRWLock.readLock().unlock();
        }
        Calendar result = Calendar.getInstance(timeZone, locale);
        result.setTimeInMillis(timeInMillis);
        if (intervalTimeUnit == IntervalTimeUnit.DAY) {
            int currentValue = result.get(Calendar.DAY_OF_MONTH);
            int newValue = currentValue + shiftedIntervalDuration;
            result.set(Calendar.DAY_OF_MONTH, newValue);
            return toLocalHour(result.getTimeInMillis(), timeZone, Integer.signum(shift));
        }
        if (intervalTimeUnit == IntervalTimeUnit.WEEK) {
            int currentValue = result.get(Calendar.WEEK_OF_MONTH);
            int newValue = currentValue + shiftedIntervalDuration;
            result.set(Calendar.WEEK_OF_MONTH, newValue);
            return toLocalHour(result.getTimeInMillis(), timeZone, Integer.signum(shift));
        }
        if (intervalTimeUnit == IntervalTimeUnit.MONTH) {
            int currentValue = result.get(Calendar.MONTH);
            int newValue = currentValue + shiftedIntervalDuration;
            result.set(Calendar.MONTH, newValue);
            return toLocalHour(result.getTimeInMillis(), timeZone, Integer.signum(shift));
        }
        throw new IllegalArgumentException();
    }

    public static String shortenTimeId( //
            String timeId) {
        return timeId.substring(0, DT_TIMESTAMP_FORMAT.length());
    }

    public static boolean checkIfSourceTimeIntervalIsLast( //
            long millis, //
            TimeInterval sourceTimeInterval, //
            TimeInterval destinationTimeInterval //
    ) {
        long sourceStartMillis = getBeginningMillisForInterval(millis, sourceTimeInterval);
        long destinationStartMillis = getBeginningMillisForInterval(millis, destinationTimeInterval);
        if (sourceStartMillis < destinationStartMillis) {
            throw new IllegalArgumentException("sourceTimeInterval must be less than destinationTimeInterval");
        }
        long sourceEndMillis = getBeginningMillisForInterval( //
                getShiftedMillis(sourceStartMillis, 1, sourceTimeInterval), sourceTimeInterval);
        long destinationEndMillis = getBeginningMillisForInterval( //
                getShiftedMillis(destinationStartMillis, 1, destinationTimeInterval), destinationTimeInterval);
        if (sourceEndMillis == destinationEndMillis) {
            return true;
        }
        return false;
    }

    public static String convertToCronPattern( //
            TimeInterval timeInterval, //
            int delayInMututes //
    ) {
        StringBuilder buf = new StringBuilder("0 ");
        int intervalDuration = timeInterval.getIntervalDuration();
        IntervalTimeUnit intervalTimeUnit = timeInterval.getIntervalTimeUnit();
        if (intervalTimeUnit == IntervalTimeUnit.MINUTE) {
            int minuteShift = delayInMututes % intervalDuration;
            buf.append(minuteShift);
            buf.append("-59/").append(intervalDuration).append(" * * * ?");
        } else if (intervalTimeUnit == IntervalTimeUnit.HOUR) {
            int minuteShift = delayInMututes % 60;
            buf.append(minuteShift).append(" ");
            int hourShift = (delayInMututes / 60) % intervalDuration;
            buf.append(hourShift).append("-23/").append(intervalDuration).append(" * * ?");
        } else if (intervalTimeUnit == IntervalTimeUnit.DAY) {
            int minuteShift = delayInMututes % 60;
            buf.append(minuteShift).append(" ");
            int hourShift = (delayInMututes / 60) % 24;
            buf.append(hourShift).append(" ");
            int dayShift = 1;
            buf.append(dayShift).append("-31/").append(intervalDuration).append(" * ?");
        } else if (intervalTimeUnit == IntervalTimeUnit.WEEK) {
            int minuteShift = delayInMututes % 60;
            buf.append(minuteShift).append(" ");
            int hourShift = (delayInMututes / 60) % 24;
            buf.append(hourShift).append(" ? * ");
            // next line works only for intervalDuration = 1
            buf.append(Calendar.getInstance(mTimeZone, mLocale).getFirstDayOfWeek());
        } else if (intervalTimeUnit == IntervalTimeUnit.MONTH) {
            int minuteShift = delayInMututes % 60;
            buf.append(minuteShift).append(" ");
            int hourShift = (delayInMututes / 60) % 24;
            buf.append(hourShift).append(" ");
            int dayShift = 1;
            buf.append(dayShift).append(" ");
            int monthShift = 1;
            buf.append(monthShift).append("-12/").append(intervalDuration).append(" ?");
        }
        return buf.toString();
    }
}
