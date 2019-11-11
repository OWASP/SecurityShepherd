package utils;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import dbProcs.Getter;
import dbProcs.Setter;

/**
 * This class Determines how the users are presented with the modules. By
 * default this method sets the floor plan to CTF mode <br/>
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
public class ModulePlan {
	private static org.apache.log4j.Logger log = Logger.getLogger(CheatSheetStatus.class);

	public static boolean openFloor = false;
	public static boolean incrementalFloor = true;
	public static boolean tournamentFloor = false;

	private static boolean isLoaded = false;

	public static boolean isIncrementalFloor() {
		loadModuleLayout();
		return incrementalFloor;
	}

	public static boolean isOpenFloor() {
		loadModuleLayout();
		return openFloor;
	}

	public static boolean isTournyFloor() {
		loadModuleLayout();
		return tournamentFloor;
	}

	public static void setIncrementalFloor() {
		if (!isLoaded) {
			loadModuleLayout();
		}
		openFloor = false;
		incrementalFloor = true;
		tournamentFloor = false;
		saveModuleLayout();
	}

	public static void setOpenFloor() {
		if (!isLoaded) {
			loadModuleLayout();
		}
		openFloor = true;
		incrementalFloor = false;
		tournamentFloor = false;
		saveModuleLayout();

	}

	public static void setTournyFloor() {
		if (!isLoaded) {
			loadModuleLayout();
		}
		openFloor = false;
		incrementalFloor = false;
		tournamentFloor = true;
		saveModuleLayout();

	}

	public static String currentMode() {
		String result = new String();
		loadModuleLayout();
		if (openFloor)
			result = "Open Floor";
		else if (incrementalFloor)
			result = "CTF";
		else
			result = "Tournament";
		return result;
	}

	private static void saveModuleLayout() {
		try {

			Setter.setAdminCheatStatus("", openFloor);
			Setter.setAdminCheatStatus("", incrementalFloor);
			Setter.setAdminCheatStatus("", tournamentFloor);

		} catch (SQLException e) {
			log.fatal("Could not save module plan setting in database: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	private static void loadModuleLayout() {
		try {

			openFloor = Getter.getAdminCheatStatus("");
			incrementalFloor = Getter.getAdminCheatStatus("");
			tournamentFloor = Getter.getAdminCheatStatus("");

		} catch (SQLException e) {
			log.fatal("Could not save module plan setting in database: " + e.toString());
			throw new RuntimeException(e);
		}
		isLoaded = true;
	}

}
