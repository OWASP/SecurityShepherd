package utils;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import dbProcs.Getter;
import dbProcs.Setter;

/**
 * This class Determines how the registration functionality is available <br/>
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
public class OpenRegistration {
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenRegistration.class);

	private static boolean enabled = false;

	private static boolean isLoaded = false;

	public static boolean isEnabled() {
		loadRegistrationStatus();

		return enabled;
	}

	public static boolean isDisabled() {
		loadRegistrationStatus();

		return !enabled;
	}

	public static void enable() {
		if (!isLoaded) {
			loadRegistrationStatus();
		}
		enabled = true;
		saveRegistrationStatus();

	}

	public static void disable() {
		if (!isLoaded) {
			loadRegistrationStatus();
		}
		enabled = false;
		saveRegistrationStatus();

	}

	public static void toggle() {
		if (!isLoaded) {
			loadRegistrationStatus();
		}
		enabled = !enabled;
		saveRegistrationStatus();

	}

	private static void saveRegistrationStatus() {
		try {

			Setter.setRegistrationStatus("", enabled);

		} catch (SQLException e) {
			log.fatal("Could not save registration status setting in database: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	private static void loadRegistrationStatus() {
		try {

			enabled = Getter.getRegistrationStatus("");

		} catch (SQLException e) {
			log.fatal("Could not save registration status setting in database: " + e.toString());
			throw new RuntimeException(e);
		}
		isLoaded = true;
	}
}
