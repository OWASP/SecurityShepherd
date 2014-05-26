package utils;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class ShepherdExposedLogManager
{
	private static org.apache.log4j.Logger log = Logger.getLogger(ShepherdExposedLogManager.class);
	
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
}
