package utils;

import dbProcs.Getter;
import dbProcs.Setter;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class holds the status of weather feedback is enabled or not
 *
 * @author Mark
 */
public class FeedbackStatus {

  private static final Logger log = LogManager.getLogger(FeedbackStatus.class);

  private static boolean enabled = false;

  private static boolean isLoaded = false;

  public static boolean isEnabled() {
    loadFeedbackStatus();
    return enabled;
  }

  public static boolean isDisabled() {
    loadFeedbackStatus();
    return !enabled;
  }

  public static void setEnabled() {
    if (!isLoaded) {
      loadFeedbackStatus();
    }
    enabled = true;
    saveFeedbackStatus();
  }

  public static void setDisabled() {
    if (!isLoaded) {
      loadFeedbackStatus();
    }
    enabled = false;
    saveFeedbackStatus();
  }

  private static void saveFeedbackStatus() {
    try {

      Setter.setFeedbackStatus("", enabled);

    } catch (SQLException e) {
      log.fatal("Could not save feedback setting in database: " + e.toString());
      throw new RuntimeException(e);
    }
  }

  private static void loadFeedbackStatus() {
    try {

      enabled = Getter.getFeedbackStatus("");

    } catch (SQLException e) {
      log.fatal("Could not load feedback setting from database: " + e.toString());
      throw new RuntimeException(e);
    }
    isLoaded = true;
  }
}
