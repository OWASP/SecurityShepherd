package testUtils;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import utils.CountdownHandler;
import utils.InvalidCountdownStateException;
import testUtils.TestProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.SQLException;
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
	public void countdownHandler_SetCorrectStartTime() {
		LocalDateTime testTime = LocalDateTime.now();

		CountdownHandler.setStartTime(testTime);

		assertEquals(testTime, CountdownHandler.getStartTime());

		testTime = LocalDateTime.parse("2020-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime);

		assertEquals(testTime, CountdownHandler.getStartTime());

		testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime);

		assertEquals(testTime, CountdownHandler.getStartTime());
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
	public void countdownHandler_compareStartTime() {

		LocalDateTime testTime = LocalDateTime.now().minusMinutes(5);

		CountdownHandler.setStartTime(testTime);
		assertTrue(CountdownHandler.isStarted());
		CountdownHandler.disableStartTime();
		assertFalse(CountdownHandler.isStarted());

		testTime = LocalDateTime.now().minusYears(5);

		CountdownHandler.setStartTime(testTime);
		assertTrue(CountdownHandler.isStarted());
		CountdownHandler.disableStartTime();
		assertFalse(CountdownHandler.isStarted());

		testTime = LocalDateTime.now().plusMinutes(5);

		CountdownHandler.setStartTime(testTime);
		assertFalse(CountdownHandler.isStarted());
		testTime = LocalDateTime.now().plusYears(5);
		CountdownHandler.setStartTime(testTime);
		assertFalse(CountdownHandler.isStarted());

	}

	@Test
	public void countdownHandler_compareLockTime() {

		LocalDateTime testTime = LocalDateTime.now().minusMinutes(5);

		CountdownHandler.setLockTime(testTime);
		assertTrue(CountdownHandler.isLocked());
		CountdownHandler.disableLockTime();
		assertFalse(CountdownHandler.isLocked());

		testTime = LocalDateTime.now().minusYears(5);
		CountdownHandler.setLockTime(testTime);
		assertTrue(CountdownHandler.isLocked());
		CountdownHandler.disableLockTime();
		assertFalse(CountdownHandler.isLocked());

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
		CountdownHandler.disableEndTime();
		assertFalse(CountdownHandler.hasEnded());

		testTime = LocalDateTime.now().minusYears(5);
		CountdownHandler.setEndTime(testTime);
		assertTrue(CountdownHandler.hasEnded());
		CountdownHandler.disableEndTime();
		assertFalse(CountdownHandler.hasEnded());

		testTime = LocalDateTime.now().plusMinutes(5);

		CountdownHandler.setEndTime(testTime);
		assertFalse(CountdownHandler.hasEnded());
		testTime = LocalDateTime.now().plusYears(5);
		CountdownHandler.setEndTime(testTime);
		assertFalse(CountdownHandler.hasEnded());

	}

	@Test
	public void countdownHandler_TestIsOpen() throws InvalidCountdownStateException {

		LocalDateTime longPastTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime shortPastTime = LocalDateTime.now().minusMinutes(5);
		LocalDateTime shortFutureTime = LocalDateTime.now().plusMinutes(5);
		LocalDateTime longFutureTime = LocalDateTime.now().plusMinutes(10);

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortFutureTime);
		CountdownHandler.setEndTime(longFutureTime);

		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableStartTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableLockTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableEndTime();
		assertTrue(CountdownHandler.isOpen());

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortPastTime);
		CountdownHandler.setEndTime(longFutureTime);

		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableStartTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableLockTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.disableEndTime();
		assertTrue(CountdownHandler.isOpen());

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(longPastTime);
		CountdownHandler.setEndTime(shortPastTime);

		assertFalse(CountdownHandler.isOpen());
		CountdownHandler.disableStartTime();
		assertFalse(CountdownHandler.isOpen());
		CountdownHandler.disableLockTime();
		assertFalse(CountdownHandler.isOpen());
		CountdownHandler.disableEndTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.enableLockTime();
		assertTrue(CountdownHandler.isOpen());
		CountdownHandler.enableStartTime();
		assertTrue(CountdownHandler.isOpen());

	}

	@Test
	public void countdownHandler_TestIsOpenStartAfterEndInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime.plusMinutes(5));
		CountdownHandler.setEndTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isOpen());

	}

	@Test
	public void countdownHandler_TestIsOpenStartAfterLockInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime.plusMinutes(5));
		CountdownHandler.setLockTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isOpen());

	}

	@Test
	public void countdownHandler_TestIsOpenLockAfterEndInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setLockTime(testTime.plusMinutes(5));
		CountdownHandler.setEndTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isOpen());

	}

	@Test
	public void countdownHandler_TestIsRunning() throws InvalidCountdownStateException {

		LocalDateTime longPastTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime shortPastTime = LocalDateTime.now().minusMinutes(5);
		LocalDateTime shortFutureTime = LocalDateTime.now().plusMinutes(5);
		LocalDateTime longFutureTime = LocalDateTime.now().plusMinutes(10);

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortFutureTime);
		CountdownHandler.setEndTime(longFutureTime);

		assertTrue(CountdownHandler.isRunning());
		CountdownHandler.disableStartTime();
		assertTrue(CountdownHandler.isRunning());
		CountdownHandler.disableLockTime();
		assertTrue(CountdownHandler.isRunning());
		CountdownHandler.disableEndTime();
		assertTrue(CountdownHandler.isRunning());

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortPastTime);
		CountdownHandler.setEndTime(longFutureTime);

		assertFalse(CountdownHandler.isRunning());
		CountdownHandler.disableStartTime();
		assertFalse(CountdownHandler.isRunning());
		CountdownHandler.disableLockTime();
		assertTrue(CountdownHandler.isRunning());
		CountdownHandler.enableLockTime();
		assertFalse(CountdownHandler.isRunning());

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(longPastTime);
		CountdownHandler.setEndTime(shortPastTime);

		assertFalse(CountdownHandler.isRunning());
		CountdownHandler.disableStartTime();
		assertFalse(CountdownHandler.isRunning());
		CountdownHandler.disableLockTime();
		assertFalse(CountdownHandler.isRunning());
		CountdownHandler.disableEndTime();
		assertTrue(CountdownHandler.isRunning());

	}

	@Test
	public void countdownHandler_TestIsRunningStartAfterEndInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime.plusMinutes(5));
		CountdownHandler.setEndTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isRunning());

	}

	@Test
	public void countdownHandler_TestIsRunningStartAfterLockInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setStartTime(testTime.plusMinutes(5));
		CountdownHandler.setLockTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isRunning());

	}

	@Test
	public void countdownHandler_TestIsRunningLockAfterEndInvalid() {

		LocalDateTime testTime = LocalDateTime.parse("1980-01-01T12:00:00");

		CountdownHandler.setLockTime(testTime.plusMinutes(5));
		CountdownHandler.setEndTime(testTime.minusMinutes(5));

		assertThrows(InvalidCountdownStateException.class, () -> CountdownHandler.isRunning());

	}

	@Test
	public void countdownHandler_TestIsRunningEqualTimes() throws InvalidCountdownStateException {

		// These equal-time edge cases should work even though they don't make much
		// sense...
		LocalDateTime longPastTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime shortPastTime = LocalDateTime.now().minusMinutes(5);
		LocalDateTime shortFutureTime = LocalDateTime.now().plusMinutes(5);
		LocalDateTime longFutureTime = LocalDateTime.now().plusMinutes(10);

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(longPastTime);
		CountdownHandler.setEndTime(longFutureTime);

		CountdownHandler.isRunning();

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortFutureTime);
		CountdownHandler.setEndTime(shortFutureTime);

		CountdownHandler.isRunning();

		CountdownHandler.setStartTime(shortPastTime);
		CountdownHandler.setLockTime(shortPastTime);
		CountdownHandler.setEndTime(shortPastTime);

		CountdownHandler.isRunning();

	}

	@Test
	public void countdownHandler_TestIsOpenEqualTimes() throws InvalidCountdownStateException {

		// These equal-time edge cases should work even though they don't make much
		// sense...
		LocalDateTime longPastTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime shortPastTime = LocalDateTime.now().minusMinutes(5);
		LocalDateTime shortFutureTime = LocalDateTime.now().plusMinutes(5);
		LocalDateTime longFutureTime = LocalDateTime.now().plusMinutes(10);

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(longPastTime);
		CountdownHandler.setEndTime(longFutureTime);

		CountdownHandler.isOpen();

		CountdownHandler.setStartTime(longPastTime);
		CountdownHandler.setLockTime(shortFutureTime);
		CountdownHandler.setEndTime(shortFutureTime);

		CountdownHandler.isOpen();

		CountdownHandler.setStartTime(shortPastTime);
		CountdownHandler.setLockTime(shortPastTime);
		CountdownHandler.setEndTime(shortPastTime);

		CountdownHandler.isOpen();

	}

}
