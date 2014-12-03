package utils;

import org.apache.log4j.Logger;

/**
 * Filters used to make SQL injection more difficult to perform
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
public class SqlFilter 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlFilter.class);

	public static String levelFour (String input)
	{
		input = input.toLowerCase();
		while(input.contains("'"))
		{
			log.debug("Scrubbing ' from input");
			input = input.replaceAll("'", "");
		}
		return input;
	}
	
	public static String levelOne (String input)
	{
		log.debug("Filtering input at SQL levelOne");
		return input.replaceFirst("'", "");
	}
	
	public static String levelThree (String input)
	{
		log.debug("Filtering input at SQL levelThree");
		input = input.toLowerCase();
		input = input.replaceAll("|", "").replaceAll("&", "").replaceAll("!", "").replaceAll("-", "").replaceAll(";", "");
		while(input.contains("or") || input.contains("true") || input.contains("false") || input.contains("and") || input.contains("is"))
			input = input.replaceAll("or", "").replaceAll("true", "").replaceAll("and", "").replaceAll("false", "").replaceAll("is", "");
		return input;
	}
	
	public static String levelTwo (String input)
	{
		log.debug("Filtering input at SQL levelTwo");
		input = input.replaceAll("OR", "").replaceAll("or", "");
		input = input.replaceAll("OR", "").replaceAll("or", "");
		input = input.replaceAll("|", "").replaceAll("&", "");
		input = input.replaceAll("true", "").replaceAll("TRUE", "");
		return input;
	}
}
