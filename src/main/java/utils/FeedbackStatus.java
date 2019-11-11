package utils;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import dbProcs.Getter;
import dbProcs.Setter;

/**
 * This class holds the status of weather feedback is enabled or not
 * 
 * @author Mark
 *
 */
public class FeedbackStatus {
	private static org.apache.log4j.Logger log = Logger.getLogger(FeedbackStatus.class);

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
