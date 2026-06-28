package com.tnf.cas.db;

import static sco.common.partition.CommonBaseShiftTime.shiftTime;

import java.util.SortedSet;
import java.util.TreeSet;

import com.tnf.cas.provider.BadParameters;

public class TimeSniper {
	public static final String WRONG_TIME_PERIOD = "Wrong Time Period";

	public static SortedSet<String> getDates(//
			String from, String to, String unit //
	) throws BadParameters {
		SortedSet<String> result = new TreeSet<String>();

		if (from == null) {
			from = to;
		}
		if (to == null) {
			to = from;
		}
		if (from == null && to == null) {
			throw new BadParameters(WRONG_TIME_PERIOD);
		}

		String t1 = shiftTime(from, 0, unit);
		String t2 = shiftTime(to, 0, unit);

		if (t1.compareTo(t2) > 0) {
			throw new BadParameters(//
					WRONG_TIME_PERIOD);
		}

		result.add(t1 + "00");
		String t = shiftTime(t1, 1, unit);

		while (t.compareTo(t2) < 0) {
			result.add(t + "00");
			t = shiftTime(t, 1, unit);
		}
		result.add(t2 + "00");
		return result;
	}
}
