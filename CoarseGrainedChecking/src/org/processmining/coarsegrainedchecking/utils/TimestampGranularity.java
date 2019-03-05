package org.processmining.coarsegrainedchecking.utils;

import java.text.SimpleDateFormat;

public enum TimestampGranularity {

	MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS;
	
	public static SimpleDateFormat getCorrespondingFormat(TimestampGranularity granularity) {
		switch (granularity) {
			case MILLISECONDS: 
				return new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss:SS");
			case SECONDS:
				return new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss");
			case MINUTES:
				return new SimpleDateFormat("dd-MM-yyyy-hh:mm");
			case HOURS:
				return new SimpleDateFormat("dd-MM-yyyy-hh");
			case DAYS:
				return new SimpleDateFormat("dd-MM-yyyy");
			default: 
				return new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss");
		}
	}
}
