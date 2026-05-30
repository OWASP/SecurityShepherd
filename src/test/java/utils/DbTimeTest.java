package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class DbTimeTest {

	@Test
	void shouldRemainUtcWhenJvmTimezoneIsNotUtc() {
		TimeZone original = TimeZone.getDefault();

		try {
			TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));

			assertEquals(
					"UTC",
					DbTime.UTC.getTimeZone().getID()
			);

		} finally {
			TimeZone.setDefault(original);
		}
	}
}