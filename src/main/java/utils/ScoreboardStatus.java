package utils;

import dbProcs.Getter;
import dbProcs.Setter;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Scoreboard management class
 *
 * @author Mark Denihan
 */
public class ScoreboardStatus {

  private static final Logger log = LogManager.getLogger(ScoreboardStatus.class);

  private static boolean scoreboardEnabled = true;
  private static String scoreboardClass = new String();
  private static boolean adminOnlyScoreboard = false;
  private static boolean classSpecificScoreboard = false;
  private static boolean publicScoreboard = false;

  private static boolean isLoaded = false;

  /**
   * Returns if user is authorised to see scoreboard currenly.
   *
   * @param userRole Must be player or admin
   * @return
   */
  public static boolean canSeeScoreboard(String userRole) {
    if (!isLoaded) {
      loadScoreboardStatus();
    }
    boolean authorised = true;
    if (adminOnlyScoreboard) {
      if (userRole == null) {
        return false;
      }
      authorised = userRole.equalsIgnoreCase("admin");
    } else if (publicScoreboard) {
      // Scoreboard is public, always allow scoreboard to be shown, even to
      // unauthorized users
      return true;
    }
    return authorised && scoreboardEnabled;
  }

  /**
   * Used to tell if the current scoreboard config is set to Class Specific
   *
   * @return Boolean Value
   */
  public static boolean getClassSpecificScoreboard() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    return classSpecificScoreboard;
  }

  public static String getScoreboardClass() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    return scoreboardClass;
  }

  /**
   * Is the scoreboard configured to be displayed?
   *
   * @return
   */
  public static boolean isScoreboardEnabled() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    return scoreboardEnabled;
  }

  /**
   * Method to know if user is running a class specific scoreboard or not
   *
   * @return True if class specific scoreboard is enabled. Otherwise False
   */
  public static boolean isClassSpecificScoreboard() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    return classSpecificScoreboard;
  }

  /**
   * Method to know if scoreboard is public
   *
   * @return True if scoreboard is public. Otherwise False
   */
  public static boolean isPublicScoreboard() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    return publicScoreboard;
  }

  /** Disables scoreboard functions */
  public static void disableScoreboard() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardEnabled = false;
    adminOnlyScoreboard = false;
    scoreboardClass = new String();
    classSpecificScoreboard = false;
    publicScoreboard = false;
    saveScoreboardStatus();
  }

  /** Sets the scoreboard to be admin only */
  public static void setScoreboardAdminOnly() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardEnabled = true;
    adminOnlyScoreboard = true;
    publicScoreboard = false;
    saveScoreboardStatus();
  }

  /**
   * Enables public Scoreboard based on scores from specific class
   *
   * @param theClass Class to base the Scoreboard on
   */
  public static void setScoreboardClass(String theClass) {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardClass = theClass;
    scoreboardEnabled = true;
    adminOnlyScoreboard = false;
    classSpecificScoreboard = false;
    publicScoreboard = false;
    saveScoreboardStatus();
  }

  /** Sets the scoreboard to show users the score from their class only */
  public static void setScoreboardClassSpecific() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardEnabled = true;
    scoreboardClass = new String();
    adminOnlyScoreboard = false;
    classSpecificScoreboard = true;
    publicScoreboard = false;
    saveScoreboardStatus();
  }

  /** Sets scoreboard to list all players regardless of class */
  public static void setScoreboardOpen() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardEnabled = true;
    scoreboardClass = new String();
    adminOnlyScoreboard = false;
    classSpecificScoreboard = false;
    publicScoreboard = false;
    saveScoreboardStatus();
  }

  /** Sets scoreboard to be public, even to unregistered users */
  public static void setScoreboardPublic() {
    if (!isLoaded) {
      loadScoreboardStatus();
    }

    scoreboardEnabled = true;
    scoreboardClass = new String();
    adminOnlyScoreboard = false;
    classSpecificScoreboard = false;
    publicScoreboard = true;
    saveScoreboardStatus();
  }

  private static void saveScoreboardStatus() {

    String statusToSave = "";

    if (scoreboardEnabled) {
      if (adminOnlyScoreboard) {
        statusToSave = "adminOnly";
      } else {
        if (classSpecificScoreboard) {
          statusToSave = "classSpecific";
        } else {
          if (publicScoreboard) {
            statusToSave = "public";
          } else {
            statusToSave = "open";
          }
        }
      }
    } else {
      statusToSave = "closed";
    }

    try {

      Setter.setScoreboardStatus("", statusToSave);
      Setter.setScoreboardClass("", scoreboardClass);

    } catch (SQLException e) {
      String message = "Could not save scoreboard status to database: " + e.toString();
      log.fatal(message);
      throw new RuntimeException(message);
    }
  }

  private static void loadScoreboardStatus() {

    String loadedStatus = "";

    try {

      loadedStatus = Getter.getScoreboardStatus("");
      scoreboardClass = Getter.getScoreboardClass("");

    } catch (SQLException e) {

      String message = "Could not load scoreboard status setting from database " + e.toString();
      log.fatal(message);
      throw new RuntimeException(message);
    }

    if (loadedStatus.equals("closed")) {
      scoreboardEnabled = false;
      adminOnlyScoreboard = false;
      classSpecificScoreboard = false;
      scoreboardClass = "";
      publicScoreboard = false;
    } else if (loadedStatus.equals("open")) {
      scoreboardEnabled = true;
      adminOnlyScoreboard = false;
      classSpecificScoreboard = false;
      publicScoreboard = false;
    } else if (loadedStatus.equals("adminOnly")) {
      scoreboardEnabled = true;
      adminOnlyScoreboard = true;
      classSpecificScoreboard = false;
      publicScoreboard = false;
    } else if (loadedStatus.equals("classSpecific")) {
      scoreboardEnabled = true;
      adminOnlyScoreboard = false;
      classSpecificScoreboard = true;
      publicScoreboard = false;
    } else if (loadedStatus.equals("public")) {
      scoreboardEnabled = true;
      adminOnlyScoreboard = false;
      classSpecificScoreboard = false;
      publicScoreboard = true;
    } else {
      String message = "Invalid scoreboard status loaded from database: " + loadedStatus;
      log.fatal(message);
      throw new RuntimeException(message);
    }

    isLoaded = true;
  }
}
