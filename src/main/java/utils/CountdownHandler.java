package utils;

import java.sql.SQLException;
import java.sql.Timestamp;

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

	private static Timestamp lockTimestamp;
	private static boolean hasLockTime = false;
	private static Timestamp endTimestamp;
	private static boolean hasEndTime = false;

	private static boolean isLoaded = false;

	public static Timestamp getLockTimestamp() {
		loadCountdowns();
		return lockTimestamp;
	}

	public static boolean hasLockTime() {
		loadCountdowns();
		return hasLockTime;
	}

	public static Timestamp getEndTimestamp() {
		loadCountdowns();
		return endTimestamp;
	}

	public static boolean hasEndTime() {
		loadCountdowns();
		return hasEndTime;
	}

	public static void setLockTimestamp(Timestamp theLockTimestamp) {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = true;
		lockTimestamp = theLockTimestamp;

		saveCountdowns();
	}

	public static void enableLockTimestamp() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = true;

		saveCountdowns();
	}

	public static void disableLockTimestamp() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasLockTime = false;

		saveCountdowns();
	}

	public static void setEndTimestamp(Timestamp theEndTimestamp) {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = true;
		endTimestamp = theEndTimestamp;

		saveCountdowns();
	}

	public static void enableEndTimestamp() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = true;

		saveCountdowns();
	}

	public static void disableEndTimestamp() {
		if (!isLoaded) {
			loadCountdowns();
		}

		hasEndTime = false;

		saveCountdowns();
	}

	private static void saveCountdowns() {
		try {

			if (isLoaded) {

				Setter.setLockTimestamp("", lockTimestamp);
				Setter.setLockTimestampStatus("", hasLockTime);
				Setter.setEndTimestamp("", endTimestamp);
				Setter.setEndTimestampStatus("", hasEndTime);

			}

		} catch (SQLException e) {
			log.fatal("Could not save countdown settings in database: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	private static void loadCountdowns() {
		String theModuleLayout = "";

		try {

			lockTimestamp = Getter.getLockTimestamp("");
			hasLockTime = Getter.getLockTimestampStatus("");
			endTimestamp = Getter.getEndTimestamp("");
			hasEndTime = Getter.getEndTimestampStatus("");

		} catch (SQLException e) {
			log.fatal("Could not load module plan setting from database: " + e.toString());
			throw new RuntimeException(e);
		}

		isLoaded = true;
	}

}
