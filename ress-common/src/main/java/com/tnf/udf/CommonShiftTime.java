package com.tnf.udf;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.tnf.bis.common.CommonConstants;
import com.tnf.bis.common.util.TimeHelper;

public class CommonShiftTime extends CommonBaseShiftTime implements CommonConstants {
	private static String timeZoneId = "";
	private static String localeId = "";

	public static void updateMemebers(Properties properties) {
		String currentTimeZoneId = properties.getProperty(TIMEZONE_PROPERTY);
		String currentLocaleId = properties.getProperty(LOCALE_PROPERTY);
		if (!currentTimeZoneId.equals(timeZoneId) || currentLocaleId.equals(localeId)) {
			TimeZone timeZone = TimeZone.getTimeZone(currentTimeZoneId);
			String language = currentLocaleId.split("_")[0];
			String country = currentLocaleId.split("_")[1];
			Locale locale = new Locale(language, country);
			TimeHelper.setTimeZone(timeZone, locale);
			timeZoneId = currentTimeZoneId;
			localeId = currentLocaleId;
		}
	}

}
