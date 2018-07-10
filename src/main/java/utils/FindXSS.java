package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import org.apache.log4j.Logger;
import org.w3c.tidy.Tidy;

/**
 * Class is responsible for finding valid XSS and CSRF attacks in user submissions
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
public class FindXSS
{
	private static org.apache.log4j.Logger log = Logger.getLogger(FindXSS.class);
	/**
	 * Method used to detect valid java script in a user submission. Specifically the presence of a script that will execute an alert command.
	 * Script tag, URI java script and java script triggers vectors are all including in this detection method.
	 * @param xssString User XSS submission (After filter if any)
	 * @return Boolean returned reflecting the presence of valid XSS attacks or not.
	 */

	public static String[] javascriptTriggers = {
		"onabort", "onbeforecopy", "onbeforecut", "onbeforepaste", "oncopy", "oncut", 
		"oninput", "onkeydown", "onkeypress", "onkeyup", "onpaste", "onbeforeunload", 
		"onhaschange", "onload", "onoffline", "ononline", "onreadystatechange", 
		"onreadystatechange", "onstop", "onunload", "onreset", "onsubmit", "onclick", 
		"oncontextmenu", "ondblclick", "onlosecapture", "onmouseenter", "onmousedown", 
		"onmouseleave", "onmousemove", "onmouseout", "onmouseover", "onmouseup", "onmousewheel",
		"onscroll", "onmove", "onmoveend", "onmovestart", "ondrag", "ondragenter", "ondragleave",
		"ondragover", "ondragstart", "ondrop", "onresize", "onresizeend", "onresizestart",
		"onactivate", "onbeforeactivate", "onbeforedeactivate", "onbeforeeditfocus", "onblur",
		"ondeactivate", "onfocus", "onfocusin", "onfocusout", "oncontrolselect", "onselect", 
		"onselectionchange", "onselectstart", "onafterprint", "onbeforeprint", "onhelp", 
		"onerror", "onerrorupdate", "onafterupdate", "onbeforeupdate", "oncellchange", 
		"ondataavailable", "ondatasetchanged", "ondatasetcomplete", "onrowenter", "onrowexit",
		"onrowsdelete", "onrowsinserted", "onbounce", "onfinish", "onstart", "onchange", "onwheel", 
		"onfilterchange", "onpropertychange", "onsearch", "onmessage", "formaction", "textinput",
		"onhashchange", "onpagehide", "onpageshow", "onpopstate", "onstorage", "oninvalid", "ondragend",
		"oncanplay", "oncanplaythrough", "oncuechange", "ondurationchange", "onemptied", "onended", 
		"onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay", "onplaying", "onprogress", 
		"onratechange", "onseeked", "onseeking", "onstalled", "onsuspend", "ontimeupdate", "onvolumechange",
		"onwaiting", "onshow", "ontoggle"};
	public static String[] uriAttributes = {
			"href", "src", "action"
	};
	public static String[] colons = {
			":", "&#x3a", "&#x3a;", "&#58", "&#58;"
	};
	
	/**
	 * Method used to validate GET request CSRF attacks embeded in IMG tags.
	 * @param messageForAdmin
	 * @param falseId
	 * @return
	 */
	public static boolean findCsrf (String messageForAdmin, String falseId)
	{
		//Find a HTML tag
		while(messageForAdmin.contains("< "))
			messageForAdmin = messageForAdmin.replaceAll("< ", "<");
		while(messageForAdmin.contains(" >"))
			messageForAdmin = messageForAdmin.replaceAll(" >", ">");
		log.debug("Cleaned to: " + messageForAdmin);
		log.debug("Checking for <img>");
		if(messageForAdmin.contains("<img"))
		{
			log.debug("Possible <img>");
			int tempStart = messageForAdmin.indexOf("<img");
			int tempEnd = messageForAdmin.indexOf("/>", tempStart + 5);
			if(tempEnd == -1)
			{
				log.debug("Invalid <img> Tag");
			}
			else
			{
				log.debug("Searching for SRC attribute");
				String tempMessage = messageForAdmin.substring(tempStart, tempEnd);
				log.debug("Working on: " + tempMessage);
				if(tempMessage.contains(" src"))
				{
					log.debug("Finding src after '='");
					int srcStart = tempMessage.indexOf(" src") + 4;
					tempMessage = tempMessage.substring(srcStart);
					log.debug("After SRC: " + tempMessage);
					int srcEqual = tempMessage.indexOf("=") + 1;
					log.debug("srcEqual = " + srcEqual);
					int counter = 0;
					while(tempMessage.substring(srcEqual + counter).startsWith(" "))
					{
						//Find end of white space after equals sign, and then evaluate if the url is valid
						counter++;
						log.debug("counter = " + counter);
					}

					tempMessage = tempMessage.substring(srcEqual + counter);
					log.debug("Working on: " + tempMessage);
					String quoteType = null;
					if(tempMessage.startsWith("\""))
					{
						quoteType = "\"";
					}
					else if(tempMessage.startsWith("'"))
					{
						quoteType = "'";
					}
					else
					{
						log.debug("No Quotes found around url");
						int endOfUrl = tempMessage.indexOf(" ");
						if(endOfUrl == -1)
							endOfUrl = tempMessage.length();
						else
							endOfUrl--;
						log.debug(tempMessage);
						tempMessage = tempMessage.substring(0, endOfUrl);
						log.debug(tempMessage);
					}
					if(quoteType != null)
					{
						log.debug("Quotes Found: " + quoteType);
						tempMessage = tempMessage.substring(1, tempMessage.substring(2).indexOf(quoteType) + 2);
					}
					log.debug("URL found to be: " + tempMessage);
					boolean validUrl = false;
					log.debug("Validating URL for Solution");
					try
					{
						URL csrfUrl = new URL(tempMessage);
						log.debug("URL Host: " + csrfUrl.getHost());
						log.debug("URL Port: " + csrfUrl.getPort());
						log.debug("URL Path: " + csrfUrl.getPath());
						log.debug("URL Query: " + csrfUrl.getQuery());
						validUrl = csrfUrl.getPath().toLowerCase().equalsIgnoreCase("/root/grantComplete/csrflesson");
						if(!validUrl)
							log.debug("1");
						validUrl = csrfUrl.getQuery().toLowerCase().equalsIgnoreCase(("userId=" + falseId).toLowerCase()) && validUrl;
						if(!validUrl)
							log.debug("2");
					}
					catch(MalformedURLException e)
					{
						log.error("Invalid URL: " + e.toString());
					}
					if(!validUrl)
					{
						log.debug("Invalid Url: " + tempMessage);
					}
					else
					{
						log.debug("Valid URL");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Searches for URL that contains CSRF attack string without user ID expected. Returns true if it is valid based on parameters submitted
	 * @param theUrl The Entire URL containing the attack
	 * @param csrfAttackPath The path the CSRF vulnerable function should be in
	 * @return boolean value depicting if the attack is valid or not
	 */
	public static boolean findCsrfAttackUrl (String theUrl, String csrfAttackPath) 
	{
		boolean validAttack = false;
		try
		{
			URL theAttack = new URL(theUrl);
			log.debug("theAttack Host: " + theAttack.getHost());
			log.debug("theAttack Port: " + theAttack.getPort());
			log.debug("theAttack Path: " + theAttack.getPath());
			log.debug("theAttack Query: " + theAttack.getQuery());
			validAttack = theAttack.getPath().toLowerCase().equalsIgnoreCase(csrfAttackPath);
			if(!validAttack)
				log.debug("Invalid Solution: Bad Path or Above");
		}
		catch(MalformedURLException e)
		{
			log.debug("Invalid URL Submitted: " + e.toString());
			validAttack = false;
		}
		catch(Exception e)
		{
			log.error("FindCSRF Failed: " + e.toString());
			validAttack = false;
		}
		return validAttack;
	}
	
	/**
	 * Searches for URL that contains CSRF attack string. Returns true if it is valid based on parameters submitted
	 * @param theUrl The Entire URL containing the attack
	 * @param csrfAttackPath The path the CSRF vulnerable function should be in
	 * @param userIdParameterName The user ID parameter name expected
	 * @param userIdParameterValue The user ID parameter value expected
	 * @return boolean value depicting if the attack is valid or not
	 */
	public static boolean findCsrfAttackUrl (String theUrl, String csrfAttackPath, String userIdParameterName, String userIdParameterValue ) 
	{
		boolean validAttack = false;
		try
		{
			URL theAttack = new URL(theUrl);
			log.debug("csrfAttackPath: " + csrfAttackPath);
			log.debug("theAttack Host: " + theAttack.getHost());
			log.debug("theAttack Port: " + theAttack.getPort());
			log.debug("theAttack Path: " + theAttack.getPath());
			log.debug("theAttack Query: " + theAttack.getQuery());
			validAttack = theAttack.getPath().toLowerCase().equalsIgnoreCase(csrfAttackPath);
			if(!validAttack)
				log.debug("Invalid Solution: Bad Path or Above");
			validAttack = theAttack.getQuery().toLowerCase().equalsIgnoreCase((userIdParameterName + "=" + userIdParameterValue).toLowerCase()) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solution: Bad Query or Above");
		}
		catch(MalformedURLException e)
		{
			log.debug("Invalid URL Submitted: " + e.toString());
			validAttack = false;
		}
		catch(Exception e)
		{
			log.error("FindCSRF Failed: " + e.toString());
			validAttack = false;
		}
		return validAttack;
	}
	
	/**
	 * Forms XSS Input for XHTML before Searching with Shepherd XSS Detector
	 * @param xssString Untrusted User Input
	 * @return Boolean value depicting if XSS was detected
	 */
	public static boolean search (String xssString)
	{
		boolean xssDetected = false;
		log.debug("String to Search: " + xssString);
		
		//Need to tidy submitted string, similar to how a browser would when it interprets it
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		InputStream inputStream = new ByteArrayInputStream(xssString.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		tidy.parseDOM(inputStream, outputStream);
		String tidyHtml = outputStream.toString().toLowerCase();
		try
		{
			outputStream.close();
			inputStream.close();
		}
		catch(Exception e)
		{
			log.error("Could not Cloud Tidy Input/Output Streams: " + e.toString());
		}
		// log.debug("String Tidied To: " + tidyHtml);
		
		//Now to Parse it and narrow down to the Body of the output
		Document parsedHtml = Jsoup.parseBodyFragment(tidyHtml);
		Element htmlBody = parsedHtml.body();
		
		//Now We're in Search Territory. Three main Stages
		//Stage One: Detect <script> tags
		Elements scriptTags = htmlBody.getElementsByTag("script");
		for(Element scriptTag: scriptTags)
		{
			String tagContents = scriptTag.html();
			//log.debug("tagContents: " + tagContents);
			if(tagContents.contains("alert"))
			{
				log.debug("Script Tags detected");
				xssDetected = true;
				break;
			}
		}
		if(!xssDetected) //If Stage One failed, Move onto Stage 2 and 3
		{
			//Stage Two/three look for different types of Attribute Based XSS
			//Search through every element
			Elements elements = htmlBody.getAllElements();
			for(Element element: elements)
			{
				//Stage Two: Look for URI attributes. 
				//Don't really care if they're in the correct element, the vector would have worked if they had it in the right one.
				//This way we'll return true on elements that newly support URI attributes in browsers
				for(int i = 0; i < uriAttributes.length && !xssDetected; i++)
				{
					String uriAttributeValue = element.attr(uriAttributes[i]);
					if (!uriAttributeValue.isEmpty())
					{
						log.debug("Found: " + uriAttributes[i] + " attribute");
						log.debug("Value: " + uriAttributeValue);
						//URI Attack Vectors can be Encoded for HTML and still be interpreted by browsers
						if(uriAttributeValue.contains("&"))
						{
							log.debug("HTML Encoded URI attriute detected");
							
							uriAttributeValue = Parser.unescapeEntities(uriAttributeValue, false);
							log.debug("Decoded Attribute = " + uriAttributeValue);
						}
						//URI Attacks need a Colon after data or javascript - Need to find that else it is invalid
						boolean colonFound = false;
						for(int colonCount = 0; colonCount < colons.length; colonCount++)
						{
							colonFound = uriAttributeValue.contains(colons[colonCount]);
							if(colonFound)
							{
								// log.debug("Detected colon in the form of '" + colons[colonCount] + "'");
								uriAttributeValue = uriAttributeValue.substring(0, uriAttributeValue.indexOf(colons[colonCount]));
								//log.debug("URI Before colon: " + uriAttributeValue);
								break;
							}
						}
						if(uriAttributeValue.equalsIgnoreCase("data") || uriAttributeValue.equalsIgnoreCase("javascript"))
						{
							log.debug("URI XSS Detected");
							xssDetected = true;
						}
					} // else URI attribute not detected
				}
				//Stage Three: JavaScript Events
				if(!xssDetected) //If a URI XSS Vector was detected, we can skip Stage Three
				{
					for(int i = 0; i < javascriptTriggers.length && !xssDetected; i++)
					{
						String javascriptTriggerValue = element.attr(javascriptTriggers[i]);
						if(!javascriptTriggerValue.isEmpty())
						{
							if(javascriptTriggerValue.startsWith("alert"))
							{
								log.debug("Javascript Trigger XSS Detected");
								xssDetected = true;
							}
						}
					}
				}
				if(xssDetected) //XSS was detected in this element: Break out of For Loop
				{
					break;
				}
			}			
		}
		return xssDetected;
	}
}
