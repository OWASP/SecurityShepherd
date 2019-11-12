package testUtils;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import utils.CountdownHandler;
import utils.XmlDocumentBuilder;

import testUtils.TestProperties;

import javax.xml.parsers.DocumentBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;

public class TestCountdownHandler {
	private static org.apache.log4j.Logger log = Logger.getLogger(TestCountdownHandler.class);

	/**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void resetDatabase() throws IOException, SQLException {
		TestProperties.setTestPropertiesFileDirectory(log);

		TestProperties.createMysqlResource();

		TestProperties.executeSql(log);
	}

	@Test
	public void countdownHandler_SetCorrectLockTime() {
		LocalDateTime testTime = LocalDateTime.now();

		CountdownHandler.setLockTime(testTime);

		assertEquals(testTime, CountdownHandler.getLockTime());

		testTime = LocalDateTime.parse("2020-01-01T12:00:00");

		CountdownHandler.setLockTime(testTime);

		assertEquals(testTime, CountdownHandler.getLockTime());

		testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setLockTime(testTime);

		assertEquals(testTime, CountdownHandler.getLockTime());
	}

	@Test
	public void countdownHandler_SetCorrectEndTime() {
		LocalDateTime testTime = LocalDateTime.now();

		CountdownHandler.setEndTime(testTime);

		assertEquals(testTime, CountdownHandler.getEndTime());

		testTime = LocalDateTime.parse("2020-01-01T12:00:00");

		CountdownHandler.setEndTime(testTime);

		assertEquals(testTime, CountdownHandler.getEndTime());

		testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setEndTime(testTime);

		assertEquals(testTime, CountdownHandler.getEndTime());
	}

	@Test
	public void countdownHandler_compareLockTime() {
		LocalDateTime testTime = LocalDateTime.now().minusMinutes(5);

		CountdownHandler.setLockTime(testTime);

		assertTrue(CountdownHandler.isLocked());

		testTime = LocalDateTime.now().minusYears(5);

		CountdownHandler.setLockTime(testTime);

		assertTrue(CountdownHandler.isLocked());
		
		testTime = LocalDateTime.now().plusMinutes(5);

		CountdownHandler.setLockTime(testTime);

		assertFalse(CountdownHandler.isLocked());

		testTime = LocalDateTime.now().plusYears(5);

		CountdownHandler.setLockTime(testTime);

		assertFalse(CountdownHandler.isLocked());		
		
	}

	@Test
	public void countdownHandler_compareEndTime() {
		LocalDateTime testTime = LocalDateTime.now().minusMinutes(5);

		CountdownHandler.setEndTime(testTime);

		assertTrue(CountdownHandler.hasEnded());

		testTime = LocalDateTime.now().minusYears(5);

		CountdownHandler.setEndTime(testTime);

		assertTrue(CountdownHandler.hasEnded());
		
		testTime = LocalDateTime.now().plusMinutes(5);

		CountdownHandler.setEndTime(testTime);

		assertFalse(CountdownHandler.hasEnded());

		testTime = LocalDateTime.now().plusYears(5);

		CountdownHandler.setEndTime(testTime);

		assertFalse(CountdownHandler.hasEnded());		
		
	}
	
}
