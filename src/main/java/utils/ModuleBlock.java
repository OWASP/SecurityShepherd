package utils;

import org.owasp.encoder.Encode;


/**
 * This class is just an instance memory structure for module blockers. Including the id of the module block, the message to give users and the current block status.
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
public class ModuleBlock 
{
	public static String blockerId = "";
	private static String blockerMessage = "";
	public static boolean blockerEnabled = false;
	
	/**
	 * Quick reset method
	 */
	public static void reset ()
	{
		blockerId = new String();
		blockerMessage = new String();
		blockerEnabled = false;
	}
	
	public static String getBlockerMessage ()
	{
		
		return Encode.forHtml(blockerMessage);
	}
	
	public static void setMessage(String theMessage)
	{
		blockerMessage = theMessage;
	}
}
