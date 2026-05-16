package utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DbTime {

	private DbTime(){};

	public static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

}
