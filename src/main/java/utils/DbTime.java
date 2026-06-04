package utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DbTime {

  private DbTime() {}

  public static final ThreadLocal<Calendar> UTC =
      ThreadLocal.withInitial(() -> Calendar.getInstance(TimeZone.getTimeZone("UTC")));
}
