package utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.apache.log4j.Logger;

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
	 * Script tag, Url java script and java script tiggeres vectors are all including in this detection method.
	 * @param xssString User XSS submission (After filter if any)
	 * @return Boolean returned reflecting the presence of valid XSS attacks or not.
	 */
	public static boolean search (String xssString)
	{
		try
		{
			boolean elementFound = false, triggerFound = false, uriAttributeFound = false, possibleXssAttack = false;
			xssString = xssString.toLowerCase();
			xssString = xssString.trim();
			log.debug("xssString is " + xssString);
			//Test for <script> vector
			int start;
			int end;
			int middle;
			
			start = xssString.indexOf("<script>");
			end = xssString.indexOf("</script>", start);
			middle = xssString.indexOf("alert");
			if(start > -1 && end > -1 && (start < middle && middle < end))
			{
				log.debug("<script> Detected");
				return true;
			}
			else
			{
				log.debug("Complex XSS detected");
				//Search for HTML Elements, and see if they have a script embeded in a src attribute, style attribute or javascript trigger
				String[] uriAttributes = {
						"href", "src", "action"
				};
				String[] colons = {
						":", "&#x3a", "&#x3a;", "&#58", "&#58;"
				};
				String[] javascriptTriggers = {
					"onload", "onunload", "onblur", "onchange", "onfocus",
					"onreset", "onselect", "onsubmit", "onabort", "onkeydown",
					"onkeyup", "onkeypress", "onclick", "ondblclick", "onmousedown",
					"onmousemove", "onmouseout", "onmouseover", "onmouseup", "onerror", "formaction"};
				String[] htmlElements = {
					"a", "abbr", "acronym", "address", "applet", "area", "b", 
					"base", "basefont", "bdo", "big", "blockquote", "body", "br",
					"button", "caption", "center", "cite", "col", "colgroup", "dd",
					"del", "dfn", "dir", "div", "dl", "dt", "em", "fieldset", "h1",
					"h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "!", "iframe",
					"img", "input", "ins", "isindex", "kbd", "label", "legend", "li",
					"link", "map", "menu", "meta", "noframes", "noscript", "object", 
					"ol", "optgroup", "option", "p", "param", "pre", "q", "s", "samp",
					"script", "select", "small", "span", "strike", "strong", "style", 
					"sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
					"title", "tr", "tt", "u", "ul", "var"};
				
				//log.debug("Cleaning Xss String");
				String tempXss = xssString;
				tempXss.replaceAll("\n", "");
				while(tempXss.contains("< "))
					tempXss = tempXss.replaceAll("< ", "<");
				while(tempXss.contains(" >"))
					tempXss = tempXss.replaceAll(" >", ">");
				log.debug("Cleaned to: " + tempXss);
				
				String htmlElement = new String();
				int k, l = 0, i, j = 0;
				int tempStart, tempMiddle, tempEnd;
				
				do
				{
					//log.debug("Checking for elements from index [" + l + "]");
					//Find element
					for(k = l; k < htmlElements.length; k++)
					{
						if(tempXss.contains("<" + htmlElements[k]))
						{
							// log.debug("Possible <" + htmlElements[k] + ">");
							tempStart = tempXss.indexOf("<" + htmlElements[k]);
							tempMiddle = -1;
							tempEnd = tempXss.indexOf("/>", tempStart + 1 + htmlElements[k].length());
							if(tempEnd == -1)
							{
								tempMiddle = tempXss.indexOf(">", tempStart + 1 + htmlElements[k].length());
								if(tempMiddle > -1)
								{
									// log.debug("Looking for </" + htmlElements[k] + "> in " + tempXss);
									tempEnd = tempXss.indexOf("</" + htmlElements[k] + ">");
								}
							}
							elementFound = tempStart > -1 && tempEnd > -1;
							if(elementFound)
							{
								htmlElement = tempXss.substring(tempStart, tempEnd + 1);
								// log.debug("Found HTML element: " + htmlElement);
								l = k + 1;
								break;
							}
							else
							{
								// log.debug("tempStart: " + tempStart);
								// log.debug("tempEnd: " + tempEnd);
								// log.debug("False Positive");
							}
						}
					}
					if(elementFound)
					{
						// log.debug("Search for URI attacks");
						//Find URI attributes
						for(i = j; i < uriAttributes.length; i++)
						{
							uriAttributeFound = htmlElement.indexOf(uriAttributes[i]) > -1;
							if(uriAttributeFound)
							{
								j = i + 1;
								break;
							}
						}
						if(!uriAttributeFound)
						{
							// log.debug("Searching for javascript event triggers");
							//Search for triggers
							for(i = j; i < javascriptTriggers.length; i++)
							{
								triggerFound = htmlElement.indexOf(javascriptTriggers[i]) > -1;
								if(triggerFound)
								{
									j = i + 1;
									break;
								}
							}
						}
						else // URI Attribute was found
						{
							//Clear out start of html element
							htmlElement = htmlElement.replaceFirst(htmlElements[k], "");
							// Clear Spaces out
							htmlElement = htmlElement.replaceAll(" ", "");
							// Get rid of return characters if there are any
							htmlElement = htmlElement.replaceAll("\n", "");
							// Identify start index (after attriubte)
							start = htmlElement.indexOf(uriAttributes[i]) + uriAttributes[i].length();
							log.debug("Start = " + start);
							//URI Attacks can be encoded in HTML encoding and still be rendered by most browsers so this line decodes the values once if an ampersand exists
							if(htmlElement.contains("&"))
							{
								log.debug("HTML Encoded URI attriute detected: " + htmlElement);
								Encoder encoder = ESAPI.encoder();
								htmlElement = encoder.decodeForHTML(htmlElement);
								log.debug("Decoded Attribute = " + htmlElement);
							}
							
							//URI Attacks need a Colon after data or javascript - Need to find that else it is invalid
							boolean colonFound = false;
							for(int colonCount = 0; colonCount < colons.length; colonCount++)
							{
								colonFound = htmlElement.contains(colons[colonCount]);
								if(colonFound)
								{
									// log.debug("Detected colon in the form of '" + colons[colonCount] + "'");
									middle = htmlElement.indexOf(colons[colonCount]);
									// log.debug("middle = " + middle);
									break;
								}
							}
							
							if(colonFound)
							{
								//Colon was found so now check if the uri value before the colon was data or javascript
								String firstPartOfUriValue = htmlElement.substring(start, middle);
								if(firstPartOfUriValue.startsWith("="))
								{
									// log.debug("Found = at start of string");
									firstPartOfUriValue = firstPartOfUriValue.substring(1);
									// log.debug("Striped to: " + firstPartOfUriValue);
								}
									
								//Stripping off starting quote or appostraphy
								if(firstPartOfUriValue.startsWith("\"") || firstPartOfUriValue.startsWith("'"))
								{
									// log.debug("Found quote at start of string");
									firstPartOfUriValue = firstPartOfUriValue.substring(1);
									// log.debug("Stripted to: " + firstPartOfUriValue);
								}
								// log.debug("Checking to see if the string is equal to data or javascript");
								// log.debug("Checking the string '" + firstPartOfUriValue + "'");
								possibleXssAttack = firstPartOfUriValue.equalsIgnoreCase("data") || firstPartOfUriValue.equalsIgnoreCase("javascript");
								
								if(possibleXssAttack)
									return true;
							}
							
							
						}
						if(triggerFound)
						{
							htmlElement = htmlElement.replaceFirst(htmlElements[k], "");
							htmlElement = htmlElement.replaceAll(" ", "");
							htmlElement = htmlElement.replaceAll("\n", "");
							start = htmlElement.indexOf(javascriptTriggers[i]) + javascriptTriggers[i].length();
							middle = htmlElement.indexOf("alert");
							if(start < middle)
							{
								char quote = htmlElement.charAt(start + 1);
								//log.debug("quote = " + quote);
								end = htmlElement.indexOf(quote, start + 2);
								String payload = htmlElement.substring(start + 2, end);
								//log.debug("The payload = " + payload);
								int payloadStart = payload.indexOf("alert(");
								int payloadEnd = payload.indexOf(")", payloadStart + 6);
								payload = payload.substring(payloadStart + 6, payloadEnd);
								//log.debug("alert payload = " + payload);
								
								if(!((payload.startsWith("\"") && payload.endsWith("\"")) || (payload.startsWith("'") && payload.endsWith("'"))))
								{
									Long.parseLong(payload);
									triggerFound = true;
								}
								else
								{
									//Its not a number, so it has quotes, so it does matter.
								}
							}
						}
						else
						{
							elementFound = false;
						}
					}
				}
				while((elementFound != true && triggerFound != true && k < htmlElements.length));
			}
			return elementFound == true && triggerFound == true;
		}
		catch(Exception e)
		{
			log.error("FindXSS Error: " + e.toString());
			return false;
		}
	}
	
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
				log.debug("Invlaid <img> Tag");
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
						log.debug("Url Host: " + csrfUrl.getHost());
						log.debug("Url Port: " + csrfUrl.getPort());
						log.debug("Url Path: " + csrfUrl.getPath());
						log.debug("Url Query: " + csrfUrl.getQuery());
						validUrl = csrfUrl.getHost().equals(ExposedServer.getSecureHost().toLowerCase());
						if(!validUrl)
							log.debug("1");
						validUrl = new Integer(csrfUrl.getPort()).toString().equals(ExposedServer.getSecurePort().toLowerCase()) && validUrl;
						if(!validUrl)
							log.debug("2");
						validUrl = csrfUrl.getPath().toLowerCase().equalsIgnoreCase("/root/grantComplete/csrflesson") && validUrl;
						if(!validUrl)
							log.debug("3");
						validUrl = csrfUrl.getQuery().toLowerCase().equalsIgnoreCase(("userId=" + falseId).toLowerCase()) && validUrl;
						if(!validUrl)
							log.debug("4");
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
			log.debug("theAttack Host: " + theAttack.getHost());
			log.debug("theAttack Port: " + theAttack.getPort());
			log.debug("theAttack Path: " + theAttack.getPath());
			log.debug("theAttack Query: " + theAttack.getQuery());
			validAttack = theAttack.getHost().equals(ExposedServer.getSecureHost().toLowerCase());
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Host");
			validAttack = new Integer(theAttack.getPort()).toString().equals(ExposedServer.getSecurePort().toLowerCase()) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Port or Above");
			validAttack = theAttack.getPath().toLowerCase().equalsIgnoreCase(csrfAttackPath) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Path or Above");
			validAttack = theAttack.getQuery().toLowerCase().equalsIgnoreCase((userIdParameterName + "=" + userIdParameterValue).toLowerCase()) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Query or Above");
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
			validAttack = theAttack.getHost().equals(ExposedServer.getSecureHost().toLowerCase());
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Host");
			validAttack = new Integer(theAttack.getPort()).toString().equals(ExposedServer.getSecurePort().toLowerCase()) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Port or Above");
			validAttack = theAttack.getPath().toLowerCase().equalsIgnoreCase(csrfAttackPath) && validAttack;
			if(!validAttack)
				log.debug("Invalid Solutoin: Bad Path or Above");
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
}
