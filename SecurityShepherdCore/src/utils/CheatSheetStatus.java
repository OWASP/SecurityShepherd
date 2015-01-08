package utils;

/**
 * Class that holds the status of the avilablility of the Cheat Sheet functionality
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class CheatSheetStatus
{
	private static boolean adminEnabled = false;
	private static boolean playerEnabled = false;
	
	public static void disableForAll()
	{
		adminEnabled = false;
		playerEnabled = false;
	}
	
	public static void enableForAdminsOnly()
	{
		playerEnabled = false;
		adminEnabled = true;
	}
	
	public static void enableForAll()
	{
		adminEnabled = true;
		playerEnabled = true;
	}
	
	public static boolean getStatusForAll ()
	{
		return adminEnabled && playerEnabled;
	}
	
	public static boolean isEnabledForAdminsOnly()
	{
		return !playerEnabled && adminEnabled;
	}
	
	public static boolean isEnabledForPlayers()
	{
		return playerEnabled;
	}
	
	public static boolean isEnabledAtAll()
	{
		return adminEnabled || playerEnabled;
	}
	
	/**
	 * Returns boolean to tell view's whether Cheat Sheets are available for a specific user role or not
	 * @param userRole
	 * @return
	 */
	public static boolean showCheat(String userRole) 
	{
		boolean show = false;
		if(isEnabledAtAll())
		{
			if(isEnabledForPlayers())
				show = true;
			else
			{
				if(isEnabledForAdminsOnly() && userRole.compareTo("admin") == 0)
					show = true;
			}
		}
		return show;
	}
	
}
