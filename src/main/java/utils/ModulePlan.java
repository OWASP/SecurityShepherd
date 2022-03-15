package utils;

import dbProcs.Getter;
import dbProcs.Setter;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class Determines how the users are presented with the modules. By default this method sets
 * the floor plan to CTF mode <br>
 * <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Mark Denihan
 */
public class ModulePlan {

  private static final Logger log = LogManager.getLogger(ModulePlan.class);

  private static boolean openFloor = false;
  private static boolean incrementalFloor = true;
  private static boolean tournamentFloor = false;

  private static boolean isLoaded = false;

  public static boolean isIncrementalFloor() {
    if (!isLoaded) {
      loadModuleLayout();
    }
    return incrementalFloor;
  }

  public static boolean isOpenFloor() {
    if (!isLoaded) {
      loadModuleLayout();
    }
    return openFloor;
  }

  public static boolean isTournamentFloor() {
    if (!isLoaded) {
      loadModuleLayout();
    }
    ;
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

  public static void setTournamentFloor() {
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
    if (!isLoaded) {
      loadModuleLayout();
    }
    if (openFloor) {
      result = "Open Floor";
    } else if (incrementalFloor) {
      result = "CTF";
    } else {
      result = "Tournament";
    }
    return result;
  }

  private static void saveModuleLayout() {
    try {
      if (openFloor) {
        Setter.setModuleLayout("", "open");
      } else if (incrementalFloor) {
        Setter.setModuleLayout("", "ctf");
      } else if (tournamentFloor) {
        Setter.setModuleLayout("", "tournament");
      } else {
        String message = "No module layouts enabled!";
        log.fatal(message);
        throw new RuntimeException(message);
      }

    } catch (SQLException e) {
      log.fatal("Could not save module plan setting in database: " + e.toString());
      throw new RuntimeException(e);
    }
  }

  private static void loadModuleLayout() {
    String theModuleLayout = "";

    try {

      theModuleLayout = Getter.getModuleLayout("");

    } catch (SQLException e) {
      log.fatal("Could not load module plan setting from database: " + e.toString());
      throw new RuntimeException(e);
    }

    if (theModuleLayout.equals("open")) {
      openFloor = true;
      incrementalFloor = false;
      tournamentFloor = true;
    } else if (theModuleLayout.equals("ctf")) {
      openFloor = false;
      incrementalFloor = true;
      tournamentFloor = false;

    } else if (theModuleLayout.equals("tournament")) {
      openFloor = false;
      incrementalFloor = false;
      tournamentFloor = true;

    } else {
      String message = "Invalid module layout loaded from database: " + theModuleLayout;
      log.fatal(message);
      throw new RuntimeException(message);
    }

    isLoaded = true;
  }
}
