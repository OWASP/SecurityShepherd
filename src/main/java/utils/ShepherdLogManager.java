package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class ShepherdLogManager {

  private static final Logger log = LogManager.getLogger(ShepherdLogManager.class);

  public static void setRequestIp(String theIp) {
    ThreadContext.put("RemoteAddress", theIp);
  }

  public static void logEvent(String theIp, String theMessage) {
    setRequestIp(theIp);
    log.debug(theMessage);
  }

  public static void setRequestIp(String theIp, String theForwardedIp) {
    if (theForwardedIp != null
        && !theForwardedIp.isEmpty()) // If string is not null and not empty set normal message
    {
      ThreadContext.put("RemoteAddress", theIp + " from " + theForwardedIp);
    } else // No Forward Header detected so Log that
    {
      ThreadContext.put("RemoteAddress", theIp + " from ?.?.?.?");
    }
  }

  public static void logEvent(String theIp, String theForwardedIp, String theMessage) {
    setRequestIp(theIp, theForwardedIp);
    log.debug(theMessage);
  }

  /**
   * Logs Event with username at beginning of log
   *
   * @param theIp
   * @param theForwardedIp
   * @param theMessage
   * @param theUser
   */
  public static void logEvent(
      String theIp, String theForwardedIp, String theMessage, Object theUser) {
    String userName = new String();
    if (theUser != null) {
      userName = theUser.toString();
    }
    if (userName.isEmpty()) {
      userName = new String("UnknownUser");
    }
    setRequestIp(theIp, theForwardedIp, userName);
    log.debug(theMessage);
  }

  /**
   * Sets IP of request and preceeds it with the username of the logged in user
   *
   * @param theIp
   * @param theForwardedIp
   */
  public static void setRequestIp(String theIp, String theForwardedIp, String userName) {

    if (theForwardedIp != null
        && !theForwardedIp.isEmpty()) // If string is not null and not empty set normal message
    {
      ThreadContext.put("RemoteAddress", userName + " at " + theIp + " from " + theForwardedIp);
    } else // No Forward Header detected so Log that
    {
      ThreadContext.put("RemoteAddress", userName + " at " + theIp + " from ?.?.?.?");
    }
  }
}
