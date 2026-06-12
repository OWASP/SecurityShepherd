package utils;

import java.util.Calendar;
import java.util.TimeZone;

public final class DbTime {

  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  private DbTime() {}

  public static Calendar utcCalendar() {
    return Calendar.getInstance(UTC);
  }
}
