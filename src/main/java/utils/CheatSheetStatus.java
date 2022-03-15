package utils;

import dbProcs.Getter;
import dbProcs.Setter;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that holds the status of the avilablility of the Cheat Sheet functionality <br>
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
public class CheatSheetStatus {

  private static final Logger log = LogManager.getLogger(CheatSheetStatus.class);

  private static boolean adminEnabled = false;
  private static boolean playerEnabled = false;

  private static boolean isLoaded = false;

  public static void disableForAll() {

    if (!isLoaded) {
      loadCheatStatus();
    }

    adminEnabled = false;
    playerEnabled = false;
    saveCheatStatus();
  }

  public static void enableForAdminsOnly() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    playerEnabled = false;
    adminEnabled = true;
    saveCheatStatus();
  }

  public static void enableForAll() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    adminEnabled = true;
    playerEnabled = true;
    saveCheatStatus();
  }

  public static boolean getStatusForAll() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    return adminEnabled && playerEnabled;
  }

  public static boolean isEnabledForAdminsOnly() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    return !playerEnabled && adminEnabled;
  }

  public static boolean isEnabledForPlayers() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    return playerEnabled;
  }

  public static boolean isEnabledAtAll() {
    if (!isLoaded) {
      loadCheatStatus();
    }
    return adminEnabled || playerEnabled;
  }

  /**
   * Returns boolean to tell view's whether Cheat Sheets are available for a specific user role or
   * not
   *
   * @param userRole
   * @return
   */
  public static boolean showCheat(String userRole) {
    boolean show = false;
    if (isEnabledAtAll()) {
      if (isEnabledForPlayers()) {
        show = true;
      } else {
        if (isEnabledForAdminsOnly() && userRole.compareTo("admin") == 0) {
          show = true;
        }
      }
    }
    return show;
  }

  private static void saveCheatStatus() {
    try {

      Setter.setAdminCheatStatus("", adminEnabled);
      Setter.setPlayerCheatStatus("", playerEnabled);

    } catch (SQLException e) {
      log.fatal("Could not save cheat sheet status in database: " + e.toString());
      throw new RuntimeException(e);
    }
  }

  private static void loadCheatStatus() {
    try {

      adminEnabled = Getter.getAdminCheatStatus("");
      playerEnabled = Getter.getPlayerCheatStatus("");

    } catch (SQLException e) {
      log.fatal("Could not save cheat sheet status in database: " + e.toString());
      throw new RuntimeException(e);
    }
    isLoaded = true;
  }
}
