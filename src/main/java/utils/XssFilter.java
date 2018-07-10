package utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


/**
 * Provides a number of filters that are used in different XSS challenges.
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
public class XssFilter 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(XssFilter.class);
	/**
	 * A method to badly validate a URL
	 * @param input URL to validate
	 * @return A poorly validated URL (XSS RISK)
	 */
	public static String anotherBadUrlValidate (String input)
	{
		String howToMakeAUrlUrl = new String("https://www.google.com/search?q=What+does+a+HTTP+link+look+like");
		input = input.toLowerCase();
		if (input.startsWith("http"))
		{
			try 
			{
				URL theUrl = new URL(input.replaceAll("#", "&#x23;").replaceFirst("<", "&#x3c;").replaceFirst(">", "&#x3e;").replaceFirst("\"", "&quot;"));
				input = theUrl.toString();
			} 
			catch (MalformedURLException e) 
			{
				log.debug("Could not Cast URL from input: " + e.toString());
				input = howToMakeAUrlUrl;
			}
		}
		else
		{
			log.debug("Was not a HTTP URL");
			input = howToMakeAUrlUrl;
		}
		return input;
	}
	
	/**
	 * White lists for specific URL types but doesn't sanitise it well
	 * @param input
	 * @return
	 */
	public static String badUrlValidate (String input)
	{
		String howToMakeAUrlUrl = new String("https://www.google.com/search?q=What+does+a+HTTP+link+look+like");
		input = input.toLowerCase();
		if (input.startsWith("http"))
		{
			try 
			{
				URL theUrl = new URL(input.replaceAll("#", "&#x23;").replaceAll("<", "&#x3c;").replaceAll(">", "&#x3e;").replaceFirst("\"", "&quot;"));
				input = theUrl.toString();
			} 
			catch (MalformedURLException e) 
			{
				log.debug("Could not Cast URL from input: " + e.toString());
				input = howToMakeAUrlUrl;
			}
		}
		else
		{
			log.debug("Was not a HTTP URL");
			input = howToMakeAUrlUrl;
		}
		return input;
	}
	
	/**
	 * Encodes for HTML, but doesn't escape ampersands
	 * @param input
	 * @return
	 */
	public static String encodeForHtml (String input)
	{
		log.debug("Filtering input at XSS white list");
		
		input = Encode.forHtml(input);
		//Decode a few things to open security holes
		input = input.replaceAll("&amp;", "&").replaceFirst("&quot;", "\"").replaceAll("&#x23;", "#").replaceFirst("&#x3d;", "=").replaceAll("&#x3b;", ";");
		//Encode lower-case "on" and upper-case "on" to complicate the required attack vectors to pass
		return input.replaceAll("on", "&#x6f;&#x6e;").replaceAll("ON", "&#x4f;&#x4e;");
	}
	
	/**
	 * Filters for specific javascript events recursively in a specific order. Can be bypassed by embedding a trigger late in the list in a trigger early in the list
	 * @param input String to be filtered for XSS attacks
	 * @return XSS Blacklist filtered HTML
	 */
	public static String levelFour (String input)
	{
		String[] javascriptTriggers = FindXSS.javascriptTriggers;
		log.debug("Filtering input at XSS levelFour");
		input = input.toLowerCase();
		while(input.contains("script"))
		{
			System.out.println("input = " + input);
			input = input.replaceAll("script", "scr.pt");
		}
		for(int i = 0; i < javascriptTriggers.length; i++)
		{
			while(input.contains(javascriptTriggers[i]))
			{
				int len = javascriptTriggers[i].length();
				String replacement = javascriptTriggers[i].substring(0, (len / 2) - 1) + "." + javascriptTriggers[i].substring((len /2) + 1, len);
				input = input.replaceAll(javascriptTriggers[i], replacement);
			}
		}
		return screwHtmlEncodings(input);
	}
	
	/**
	 * Filters the word "script" specifically
	 * @param input Input to be filtered for XSS
	 * @return XSS Blacklist filtered HTML
	 */
	public static String levelOne (String input)
	{
		log.debug("Filtering input at XSS levelOne");
		return input.toLowerCase().replaceAll("script", "scr.pt").replaceAll("SCRIPT", "SCR.PT");
	}
	
	/**
	 * Filters for javascript triggers 3 times before stopping and breaks HTML encodings
	 * @param input
	 * @return
	 */
	public static String levelThree (String input)
	{
		log.debug("Filtering input at XSS levelThree");
		input = input.toLowerCase();
		input = input.replaceAll("script", "scr.pt");
		for(int h = 0; h < FindXSS.javascriptTriggers.length; h++)
		{
			for(int i = 0; i <= 3; i++)
				input = input.replaceAll(FindXSS.javascriptTriggers[h], "");
		}
		return screwHtmlEncodings(input);
	}
	
	/**
	 * Filters specific javascript event triggers
	 * @param input String to be filtered for XSS attacks
	 * @return XSS Blacklist filtered HTML
	 */
	public static String levelTwo (String input)
	{
		input = input.toLowerCase();
		log.debug("Filtering input at XSS levelTwo");
		input = input.replaceAll("script", "scr.pt");
		input = input.replaceAll("onclick", "o.ick");
		input = input.replaceAll("onmouseover", "o.ver");
		input = input.replaceAll("onload", "o.oad");
		input = input.replaceAll("onerror", "o.err");
		input = input.replaceAll("ondblclick", "o.dbl");
		return screwHtmlEncodings(input);
	}
	
	/**
	 * Use this to cripple HTML encoded attacks. This is can be used to limit the vectors of attack for success
	 * @param input The string you want to remove HTML encoding from
	 * @return A string without HTML encoding
	 */
	private static String screwHtmlEncodings(String input)
	{
		input = input.replaceAll("&", "!").replaceAll(":", "!");
		return input;
	}
}
