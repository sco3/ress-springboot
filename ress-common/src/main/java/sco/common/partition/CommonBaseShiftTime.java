package sco.common.partition;

import java.text.ParseException;

import com.tnf.bis.common.timeinterval.BaseTimeGranularity;
import com.tnf.bis.common.timeinterval.TimeInterval;

import sco.common.util.TimeHelper;

public class CommonBaseShiftTime {

    public static String shiftTime(long timeInMillis, String timeUnit) {
        TimeInterval timeInterval = BaseTimeGranularity.findTimeIntervalById(timeUnit);
        String beginningTimeId = TimeHelper.getBeginningTimeIdForInterval(timeInMillis, timeInterval);
        return TimeHelper.shortenTimeId(beginningTimeId);
    }

    public static String shiftTime(long timeInMillis, long shift, String timeUnit) {
        TimeInterval timeInterval = BaseTimeGranularity.findTimeIntervalById(timeUnit);
        long beginning = TimeHelper.getBeginningMillisForInterval(timeInMillis, timeInterval);
        String beginningTimeId = TimeHelper.getShiftedTimeId(beginning, (int) shift, timeInterval);
        return TimeHelper.shortenTimeId(beginningTimeId);
    }

    public static String shiftTime(String timeId, String timeUnit) {
        try {
            TimeInterval timeInterval = BaseTimeGranularity.findTimeIntervalById(timeUnit);
            long timeInMillis = TimeHelper.getTime(timeId);
            String beginningTimeId = TimeHelper.getBeginningTimeIdForInterval(timeInMillis, timeInterval);
            return TimeHelper.shortenTimeId(beginningTimeId);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex.toString());
        }
    }

    public static String shiftTime(String timeId, long shift, String timeUnit) {
        try {
            TimeInterval timeInterval = BaseTimeGranularity.findTimeIntervalById(timeUnit);
            long timeInMillis = TimeHelper.getTime(timeId);
            long beginning = TimeHelper.getBeginningMillisForInterval(timeInMillis, timeInterval);
            String beginningTimeId = TimeHelper.getShiftedTimeId(beginning, (int) shift, timeInterval);
            return TimeHelper.shortenTimeId(beginningTimeId);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex.toString());
        }
    }
}
