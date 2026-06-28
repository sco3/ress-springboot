package com.tnf.bis.common.timeinterval;

import java.io.Serializable;

public class TimeInterval implements Comparable<TimeInterval>, Serializable {

	private static final long serialVersionUID = -2030038475114851372L;
	private final int intervalDuration;
	private final IntervalTimeUnit intervalTimeUnit;

	public TimeInterval(int intervalDuration, IntervalTimeUnit intervalTimeUnit) {
		this.intervalDuration = intervalDuration;
		this.intervalTimeUnit = intervalTimeUnit;
	}

	public int getIntervalDuration() {
		return intervalDuration;
	}

	public IntervalTimeUnit getIntervalTimeUnit() {
		return intervalTimeUnit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + intervalDuration;
		result = prime * result + ((intervalTimeUnit == null) ? 0 : intervalTimeUnit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeInterval other = (TimeInterval) obj;
		if (intervalDuration != other.intervalDuration)
			return false;
		if (intervalTimeUnit != other.intervalTimeUnit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
		builder.append("[intervalDuration=");
		builder.append(intervalDuration);
		builder.append(", intervalTimeUnit=");
		builder.append(intervalTimeUnit);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(TimeInterval other) {
		int thisValue = this.intervalDuration;
		if (this.intervalTimeUnit != null) {
			thisValue = this.intervalDuration * this.intervalTimeUnit.getMinutes();
		}
		long otherValue = other.intervalDuration; //
		if (other.intervalTimeUnit != null) {
			otherValue = other.intervalDuration * other.intervalTimeUnit.getMinutes();
		}
		return new Long(thisValue).compareTo(new Long(otherValue));
	}
}
