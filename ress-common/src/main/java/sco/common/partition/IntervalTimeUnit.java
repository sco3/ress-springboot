package sco.common.partition;

import java.io.Serializable;

public enum IntervalTimeUnit implements Serializable {
	MINUTE(1), //
	HOUR(1 * 60), //
	DAY(1 * 60 * 24), //
	WEEK(1 * 60 * 24 * 7), //
	MONTH(1 * 60 * 24 * 30);

	private int minutes = 0;

	IntervalTimeUnit(int minutes) {
		this.minutes = minutes;
	}

	public int getMinutes() {
		return minutes;
	}

	public static IntervalTimeUnit getByTimeUnitName(String timeUnitName) {
		for (IntervalTimeUnit intervalTimeUnit : IntervalTimeUnit.values()) {
			if (intervalTimeUnit.name().equalsIgnoreCase(timeUnitName)) {
				return intervalTimeUnit;
			}
		}
		throw new IllegalArgumentException();
	}
}
