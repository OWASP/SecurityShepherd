package utils;

/**
 * This class Determines how the users are presented with the modules. By default this method sets the floor plan to CTF mode
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
public class ModulePlan
{
	public static boolean openFloor = false;
	public static boolean incrementalFloor = true;
	public static boolean tornyFloor = false;
	
	public static boolean isIncrementalFloor() 
	{
		return incrementalFloor;
	}
	
	public static boolean isOpenFloor() 
	{
		return openFloor;
	}
	
	public static boolean isTournyFloor()
	{
		return tornyFloor;
	}

	public static void setIncrementalFloor()
	{
		openFloor = false;
		incrementalFloor = true;
		tornyFloor = false;
	}

	public static void setOpenFloor()
	{
		openFloor = true;
		incrementalFloor = false;
		tornyFloor = false;
	}
	
	public static void setTournyFloor()
	{
		openFloor = false;
		incrementalFloor = false;
		tornyFloor = true;
	}
	
	public static String currentMode()
	{
		String result = new String();
		if(openFloor)
			result = "Open Floor";
		else if (incrementalFloor)
			result = "CTF";
		else
			result = "Tournament";
		return result;
	}
}
