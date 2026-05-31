package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class DbTimeTest {

	@Test
	void shouldReadTimestampCorrectlyInNonUtcJvm() throws Exception {
		TimeZone original = TimeZone.getDefault();

		try {
			TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));

			Instant expected = Instant.parse("2025-01-01T12:00:00Z");

			ResultSet resultSet = mock(ResultSet.class);

			when(resultSet.getTimestamp(eq(7), eq(DbTime.UTC)))
					.thenReturn(Timestamp.from(expected));

			Timestamp suspendedUntil =
					resultSet.getTimestamp(7, DbTime.UTC);

			assertEquals(
					expected.toEpochMilli(),
					suspendedUntil.toInstant().toEpochMilli());

		} finally {
			TimeZone.setDefault(original);
		}
	}
}