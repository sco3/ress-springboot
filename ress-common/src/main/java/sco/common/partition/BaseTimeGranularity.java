package sco.common.partition;

import java.util.StringJoiner;
import java.util.regex.Pattern;

public enum BaseTimeGranularity {
    MIN5("5MIN", new TimeInterval(5, IntervalTimeUnit.MINUTE), "5min"), //
    MIN15("15MIN", new TimeInterval(15, IntervalTimeUnit.MINUTE), "15min"), //
    HOUR("H", new TimeInterval(1, IntervalTimeUnit.HOUR), "hourly"), //
    DAY("D", new TimeInterval(1, IntervalTimeUnit.DAY), "daily"), //
    WEEK("W", new TimeInterval(1, IntervalTimeUnit.WEEK), "weekly"), //
    MONTH("MON", new TimeInterval(1, IntervalTimeUnit.MONTH), "monthly");

    private static final BaseTimeGranularity[] counting = new BaseTimeGranularity[] { HOUR, DAY, WEEK, MONTH };
    private String mId;
    private TimeInterval mTimeInterval;
    private String mAdjective;

    private BaseTimeGranularity(String id, TimeInterval timeInterval, String adjective) {
        mId = id;
        mTimeInterval = timeInterval;
        mAdjective = adjective;
    }

    public String getId() {
        return mId;
    }

    public TimeInterval getTimeInterval() {
        return mTimeInterval;
    }

    public String getAdjective() {
        return mAdjective;
    }

    public static BaseTimeGranularity findById(String id) {
        for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
            if (value.getId().equalsIgnoreCase(id)) {
                return value;
            }
        }
        // not found message
        StringJoiner list = new StringJoiner(",", "[", "]");
        for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
            list.add(value.getId());
        }
        String message = "'" + id + "' does not match the list: " + list;
        throw new IllegalArgumentException(message);
    }

    public static TimeInterval findTimeIntervalById(String id) {
        for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
            if (value.getId().equalsIgnoreCase(id)) {
                return value.getTimeInterval();
            }
        }
        for (BaseTimeGranularity value : counting) {
            if (id.toUpperCase().endsWith(value.getId())) {
                String str = id.substring(0, id.length() - value.getId().length());
                try {
                    int number = Integer.parseInt(str);
                    if (number > 0 && Integer.toString(number).equals(str)) {
                        TimeInterval timeInterval = value.getTimeInterval();
                        return new TimeInterval(number, timeInterval.getIntervalTimeUnit());
                    }
                } catch (NumberFormatException ex) {
                }
            }
        }
        // not found message
        StringJoiner list = new StringJoiner(",", "[", "]");
        for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
            list.add(value.getId());
        }
        String message = "'" + id + "' does not match the list: " + list;
        throw new IllegalArgumentException(message);
    }

    public static BaseTimeGranularity findByTimeInterval(TimeInterval timeInterval) {
        if (timeInterval != null) {
            for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
                if (value.getTimeInterval().equals(timeInterval)) {
                    return value;
                }
            }
        }
        StringJoiner list = new StringJoiner(",", "[", "]");
        for (BaseTimeGranularity value : BaseTimeGranularity.values()) {
            list.add(value.getTimeInterval().toString());
        }
        String message = "'" + (timeInterval != null ? timeInterval.toString() : null) + "' does not match the list: "
                + list;
        throw new IllegalArgumentException(message);
    }

    public static String getTimeGranularityByTableName(String tableName) {
        if (tableName != null) {
            String[] parts = tableName.split(Pattern.quote(">"));
            tableName = parts[0].trim().toLowerCase();
            if (tableName.endsWith("_min_5")) {
                return BaseTimeGranularity.MIN15.getId();
            } else if (tableName.endsWith("_h_1")) {
                return BaseTimeGranularity.HOUR.getId();
            } else if (tableName.endsWith("_d_1")) {
                return BaseTimeGranularity.DAY.getId();
            } else if (tableName.endsWith("_w_1")) {
                return BaseTimeGranularity.WEEK.getId();
            } else if (tableName.endsWith("_m_1")) {
                return BaseTimeGranularity.MONTH.getId();
            }
        }
        return "?";
    }

    public static int compare(BaseTimeGranularity first, BaseTimeGranularity second) {
        return first.getTimeInterval().compareTo(second.getTimeInterval());
    }
}
