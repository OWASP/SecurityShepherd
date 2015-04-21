package utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Used to help application kick users that have been suspended
 * @author Mark Denihan
 *
 */
public class UserKicker 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(UserKicker.class);
	private static List<String> kickTheseUsers = new ArrayList<String>();
	
	/**
	 * Add's a specific user to the kick list
	 * @param userName The user name to kick
	 */
	public static void addUserToKickList(String userName)
	{
		log.debug("Adding " + userName + " to kick list");
		kickTheseUsers.add(userName);
	}
	
	/**
	 * Tells you if a user is on the kick list
	 * @param userName User to search the list for
	 * @return True if the user should be kicked
	 */
	public static boolean shouldKickUser(String userName)
	{
		if (!kickTheseUsers.isEmpty())
		{
			log.debug("Kick list Is Not Empty! Checking...");
			boolean kickUser = kickTheseUsers.contains(userName);
			if (kickUser)
				log.debug(userName + " is in kick list");
			return kickUser;
		}
		else
		{
			//log.debug("Empty Kick List! Skiping...");
			return false;
		}
	}
	
	/**
	 * Removes a user from the kick list. Should be used after user has been kicked
	 * @param userName Username of the user to remove from kick list
	 */
	public static void removeFromKicklist(String userName)
	{
		if(shouldKickUser(userName)) //If User is in list
		{
			log.debug("Removing " + userName + " from kick list");
			kickTheseUsers.remove(userName);
		}
	}
}
