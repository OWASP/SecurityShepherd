package utils;

/**
 * This class Determines how the registration functionality is available
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
public class OpenRegistration 
{
	private static boolean enabled = true;
	private static boolean disabled = false;
	
	public static boolean isEnabled() 
	{
		return enabled;
	}

	public static boolean isDisabled()
	{
		return disabled;
	}
	
	public static void enable () 
	{
		enabled = true;
		disabled = false;
	}
	
	public static void disable () 
	{
		enabled = false;
		disabled = true;
	}
	
	public static void toggle()
	{
		enabled = !enabled;
		disabled = !disabled;
	}
}
