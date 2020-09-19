package utils;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;

import dbProcs.Getter;
import dbProcs.Setter;

/**
 * 
 * <br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark Denihan
 *
 */
public class CountdownHandler {
	private static org.apache.log4j.Logger log = Logger.getLogger(CountdownHandler.class);

	private static LocalDateTime startTime;
	private static boolean hasStartTime = false;
	private static LocalDateTime lockTime;
	private static boolean hasLockTime = false;
	private static LocalDateTime endTime;
	private static boolean hasEndTime = false;

	private static boolean isLoaded = false;

	private static boolean validate() throws InvalidCountdownStateException {

		if (!isLoaded) {
			loadCountdowns();
		}

		if (hasStartTime && hasLockTime) {
			if (startTime.isAfter(lockTime)) {
				throw new InvalidCountdownStateException("Start time must be before or equal to lock time");
			}
		}

		if (hasStartTime && hasEndTime) {
			if (startTime.isAfter(endTime)) {
				throw new InvalidCountdownStateException("Start time must be before or equal to end time");
			}
		}

		if (hasLockTime && hasEndTime) {
			if (lockTime.isAfter(endTime)) {
				throw new InvalidCountdownStateException("Lock time must be before or equal to end time");
			}
		}

		return true;

	}
	
	public static boolean willStart() throws InvalidCountdownStateException {

		// Returns true if there is a start time but the CTF hasn't started yet

		validate();

		return hasStartTime() && !isRunning();
	}

	public static boolean willLock() throws InvalidCountdownStateException {

		// Returns true if CTF has started and there is a lock time that hasn't happened yet 

		validate();

		return isStarted() && hasLockTime() && isOpen();
	}
	
	public static boolean willEnd() throws InvalidCountdownStateException {

		// Return true if CTF it is locked or started, has an end timer that hasn't happened yet

		validate();

		return !willLock () && isRunning() && hasEndTime() && !hasEnded();
	}

	public static boolean isOpen() throws InvalidCountdownStateException {

		// CTF is open if it has started, isn't locked and hasn't ended

		validate();

		return isStarted() && !isLocked() && !hasEnded();
	}

	public static boolean isRunning() throws InvalidCountdownStateException {

		// CTF is running if it has started, hasn't ended, but ignores lock state

		validate();

		return isStarted() && !hasEnded();
	}

	public static boolean isStarted() {
		if (!isLoaded) {
			loadCountdowns();
		}

		if (hasStartTime) {

			// Start timer enabled, only return true if timer has passed
			return startTime.isBefore(LocalDateTime.now());
		} else {

			// Start timer disabled, always say it's started
			return true;
		}
	}

	public static boolean isLocked() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return hasLockTime && lockTime.isBefore(LocalDateTime.now());
	}

	public static boolean hasEnded() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return hasEndTime && endTime.isBefore(LocalDateTime.now());
	}

	public static LocalDateTime getStartTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return startTime;
	}

	public static boolean hasStartTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return hasStartTime;
	}

	public static LocalDateTime getLockTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return lockTime;
	}

	public static boolean hasLockTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return hasLockTime;
	}

	public static LocalDateTime getEndTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return endTime;
	}

	public static boolean hasEndTime() {
		if (!isLoaded) {
			loadCountdowns();
		}
		return hasEndTime;
	}

	public static void setStartTime(LocalDateTime theStartTime) {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasStartTime = true;
		startTime = theStartTime;

		saveCountdowns();
	}

	public static void enableStartTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasStartTime = true;

		saveCountdowns();
	}

	public static void disableStartTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasStartTime = false;

		saveCountdowns();
	}

	public static void setLockTime(LocalDateTime theLockTime) {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = true;
		lockTime = theLockTime;

		saveCountdowns();
	}

	public static void enableLockTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = true;

		saveCountdowns();
	}

	public static void disableLockTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = false;

		saveCountdowns();
	}

	public static void setEndTime(LocalDateTime theEndTime) {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = true;
		endTime = theEndTime;

		saveCountdowns();
	}

	public static void enableEndTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = true;

		saveCountdowns();
	}

	public static void disableEndTime() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = false;

		saveCountdowns();
	}

	private static void saveCountdowns() {
		try {

			if (isLoaded) {

				Setter.setLockTime("", startTime);
				Setter.setLockTimeStatus("", hasStartTime);
				Setter.setLockTime("", lockTime);
				Setter.setLockTimeStatus("", hasLockTime);
				Setter.setEndTime("", endTime);
				Setter.setEndTimeStatus("", hasEndTime);

			}

		} catch (SQLException e) {
			log.fatal("Could not save countdown settings in database: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	private static void loadCountdowns() {

		try {

			startTime = Getter.getStartTime("");
			hasStartTime = Getter.getStartTimeStatus("");
			lockTime = Getter.getLockTime("");
			hasLockTime = Getter.getLockTimeStatus("");
			endTime = Getter.getEndTime("");
			hasEndTime = Getter.getEndTimeStatus("");

		} catch (SQLException e) {
			log.fatal("Could not load module plan setting from database: " + e.toString());
			throw new RuntimeException(e);
		}

		isLoaded = true;
	}

}
