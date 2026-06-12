package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Calendar;
import org.junit.jupiter.api.Test;

class DbTimeTest {

  @Test
  void utcCalendarUsesUtc() {
    assertEquals("UTC", DbTime.utcCalendar().getTimeZone().getID());
  }

  @Test
  void utcCalendarReturnsIndependentInstances() {
    Calendar first = DbTime.utcCalendar();
    Calendar second = DbTime.utcCalendar();

    assertNotSame(first, second);
  }
}
