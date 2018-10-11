package utils;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class ShepherdLogManager
{
	private static org.apache.log4j.Logger log = Logger.getLogger(ShepherdLogManager.class);
	
	public static void setRequestIp (Object theIp)
	{
		MDC.put("RemoteAddress", theIp);
	}
	
	public static void logEvent(Object theIp, String theMessage)
	{
		setRequestIp(theIp);
		log.debug(theMessage);
	}
	
	public static void setRequestIp (Object theIp, String theForwardedIp)
	{
		if (theForwardedIp != null && !theForwardedIp.isEmpty()) //If string is not null and not empty set normal message
			MDC.put("RemoteAddress", theIp.toString() + " from " + theForwardedIp);
		else //No Forward Header detected so Log that
			MDC.put("RemoteAddress", theIp.toString() + " from ?.?.?.?");
	}
	
	public static void logEvent(Object theIp, String theForwardedIp, String theMessage)
	{
		setRequestIp(theIp, theForwardedIp);
		log.debug(theMessage);
	}
	
	/**
	 * Logs Event with username at beginning of log
	 * @param theIp
	 * @param theForwardedIp
	 * @param theMessage
	 * @param theUser
	 */
	public static void logEvent(Object theIp, String theForwardedIp, String theMessage, Object theUser)
	{
		String userName = new String();
		if(theUser != null)
			userName = theUser.toString();
		if (userName.isEmpty())
			userName = new String("UnknownUser");
		setRequestIp(theIp, theForwardedIp, userName);
		log.debug(theMessage);
	}
	
	/**
	 * Sets IP of request and preceeds it with the username of the logged in user
	 * @param theIp
	 * @param theForwardedIp
	 * @param theUser
	 */
	public static void setRequestIp (Object theIp, String theForwardedIp, String userName)
	{
		
		if (theForwardedIp != null && !theForwardedIp.isEmpty()) //If string is not null and not empty set normal message
			MDC.put("RemoteAddress", userName + " at " + theIp.toString() + " from " + theForwardedIp);
		else //No Forward Header detected so Log that
			MDC.put("RemoteAddress", userName + " at " + theIp.toString() + " from ?.?.?.?");
	}
}
