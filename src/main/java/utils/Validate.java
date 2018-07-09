package utils;

import java.math.BigInteger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Class is used to validate various inputs
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
public class Validate 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Validate.class);
	/**
	 * Finds JSession token from user's cookies[], validates and returns.
	 * @param userCookies Cookies from users browser
	 * @return JSession Id
	 */
	public static Cookie getSessionId (Cookie[] userCookies)
	{
		int i = 0;
		Cookie theSessionId = null;
		for(i = 0; i < userCookies.length; i++)
		{
			if(userCookies[i].getName().compareTo("JSESSIONID") == 0)
			{
				theSessionId = userCookies[i];
				break; //End Loop, because we found the theSessionId
			}
		}
		return theSessionId;
	}

	/**
	 * Finds CSRF token from user's cookies[], validates.
	 * @param userCookies All of the user's cookies from their browser
	 * @return csrfCookie
	 */
	public static Cookie getToken (Cookie[] userCookies)
	{
		int i = 0;
		Cookie theToken = null;
		for(i = 0; i < userCookies.length; i++)
		{
			if(userCookies[i].getName().compareTo("token") == 0)
			{
				theToken = userCookies[i];
				break; //End Loop, because we found the token
			}
		}
		if(theToken != null)
		{
			//log.debug("Found Cookie " + theToken.getName() + " with value " + theToken.getValue());
			//The Token is currently designed to be a random Big Integer. If the Big Integer Case does not work, the token has been modified. Potentially in a malicious manner
			try
			{
				BigInteger theTokenCasted = new BigInteger(theToken.getValue());
				BigInteger tenGrand = new BigInteger("10000");
				BigInteger tenGrandNeg = new BigInteger("-10000");
				if(!(theTokenCasted.compareTo(tenGrand) > 0 || theTokenCasted.compareTo(tenGrandNeg) < 0))
				{
					log.error("CSRF Cookie Token was modified in some manor!");
					theToken = null;
				}
			}
			catch (Exception e)
			{
				log.error("CSRF Cookie Token was modified in some manor: " + e.toString());
				theToken = null;
			}
		}
		return theToken;
	}
	
	/**
	 * Validates class year when creating classes. Class year should be YY/YY, e.g. 11/12. So the first year must be less than the second.	
	 * @param classYear Class Year in YY/YY format, e.g. 11/12.
	 * @return Boolean value stating weather or not these supplied attributes make a valid class year
	 */
	public static boolean isValidClassYear(String classYear)
	{
		boolean result = false;
		result = classYear.length() == 4;
		if(result)
		{
			try
			{
				result = Integer.parseInt(classYear) > 2010;
			}
			catch(Exception e)
			{
				log.error("Could not parse classYear");
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Email validation
	 * @param email
	 * @return Boolean reflect email validity
	 */
	public static boolean isValidEmailAddress(String email) 
	{
	   boolean result = true;
	   try 
	   {
		  log.debug("About to crash");
	      InternetAddress emailAddr = new InternetAddress(email);
	      log.debug("Did we crash");
	      emailAddr.validate();
	      log.debug("Didn't crash");
	   } 
	   catch (AddressException ex)
	   {
	      result = false;
	   }
	   return result;
	}

	/**
	 * Invalid password detecter
	 * @param passWord
	 * @return
	 */
	public static boolean isValidPassword(String passWord)
	{
		boolean result = false;
		result = passWord.length() > 7 && passWord.length() <= 512;
		if (!result)
		{
			log.debug("Invalid Password detected");
		}
		return result;
	}
	
	/**
	 * Used to validate user creation requests
	 * @param userName User Name
	 * @param passWord User Password
	 * @return Boolean value stating weather or not these supplied attributes make a valid user
	 */
	public static boolean isValidUser(String userName, String passWord)
	{
		boolean result = false;
		result = userName.length() > 4 && passWord.length() > 7 && userName.length() <= 32 && passWord.length() <= 512;
		if (!result)
		{
			log.debug("Invalid Data detected in Validate.isValidUser()");
		}
		return result;
	}
	
	/**
	 * Used to validate user creation requests
	 * @param userName User Name
	 * @param passWord User Password
	 * @param userAddress User address
	 * @return Boolean value stating weather or not these supplied attributes make a valid user
	 */
	public static boolean isValidUser(String userName, String passWord, String userAddress)
	{
		boolean result = false;
		result = userName.length() > 4 && passWord.length() >= 8 && userName.length() <= 32 && passWord.length() <= 512 && userAddress.length() <= 128;
		if (!result)
		{
			log.debug("Invalid Data detected in Validate.isValidUser()");
		}
		return result;
	}

	/**
	 * Quick method to prevent data and javascript URLs
	 * @param theUrl
	 * @return
	 */
	public static String makeValidUrl(String theUrl)
	{
		theUrl = theUrl.toLowerCase();
		if (!theUrl.startsWith("http"))
		{
			theUrl = "http" + theUrl;
			log.debug("Transformed to: " + theUrl);
		}
		return theUrl;
	}
	
	/**
	 * Session is checked for credentials and ensures that they have not been modified and that they are valid for an administrator
	 * @param ses HttpSession from users browser
	 * @return Boolean value that reflects the validity of the admins session
	 */
	public static boolean validateAdminSession(HttpSession ses)
	{
		boolean result = false;
		String userName = new String();
		if (ses == null)
		{
			log.debug("No Session Found");
		}
		else
		{
			if (ses.getAttribute("logout") != null) 
			{
				log.debug("Logout Attribute Found: Invalidating session...");
			    ses.invalidate(); // make servlet engine forget the session
			}
			else
			{
				//log.debug("Active Session Found");
				if (ses.getAttribute("userRole") != null && ses.getAttribute("userName") != null)
				{
					try 
					{
						userName = (String) ses.getAttribute("userName");
						//log.debug("Session holder is " + userName);
						String role = (String) ses.getAttribute("userRole");
						result = (role.compareTo("admin") == 0);
						if(!result)
							log.fatal("User " + userName + " Attempting Admin functions! (CSRF Tokens Not Checked)");
					} 
					catch (Exception e) 
					{
						log.fatal("Tampered Parameter Detected!!! Could not parameters");
					}
				}
				else
				{
					log.debug("Session has no credentials");
				}
			}
		}
		return result;
	}
	
	/**
	 * Session is checked for credentials and ensures that they have not been modified and that they are valid for an administrator. This function also validates CSRF tokens
	 * @param ses HttpSession from users browser
	 * @return Boolean value that reflects the validity of the admins session
	 */
	public static boolean validateAdminSession(HttpSession ses, Cookie cookieToken, Object requestToken)
	{
		boolean result = false;
		String userName = new String();
		if (ses == null)
		{
			log.debug("No Session Found");
		}
		else
		{
			if (ses.getAttribute("logout") != null) 
			{
				log.debug("Logout Attribute Found: Invalidating session...");
			    ses.invalidate(); // make servlet engine forget the session
			}
			else
			{
				//log.debug("Active Session Found");
				if (ses.getAttribute("userRole") != null && ses.getAttribute("userName") != null)
				{
					try 
					{
						userName = (String) ses.getAttribute("userName");
						//log.debug("Session holder is " + userName);
						String role = (String) ses.getAttribute("userRole");
						result = (role.compareTo("admin") == 0);
						if(!result)
						{
							//Check CSRF Tokens of User to ensure they are not being CSRF'd into causing Unauthorised Access Alert
							boolean validCsrfTokens = validateTokens(cookieToken, requestToken);
							if(validCsrfTokens)
								log.fatal("User account " + userName + " Attempting Admin functions! (With Valid CSRF Tokens)");
							else
								log.error("User account " + userName + " accessing admin function with bad CSRF Tokens");
						}
							
					} 
					catch (Exception e) 
					{
						log.fatal("Tampered Parameter Detected!!! Could not parameters");
					}
				}
				else
				{
					log.debug("Session has no credentials");
				}
			}
		}
		return result;
	}
	
	/**
	 * Takes a String submitted to be used to encrypt and makes it the correct length for an encryption key
	 * @param userSalt String to be validated
	 * @return A Valid Encryption Key based on the input
	 */
	public static String validateEncryptionKey(String userSalt)
	{
		String newKey = new String();
		int keySize = userSalt.length();
		if(keySize == 16)
		{
			//log.debug("Key Already Valid");
			newKey = userSalt;
		}
		else
		{
			if(keySize > 16)
			{
				//log.debug("Key too Long...");
				newKey = userSalt.substring(0, 16);
			}
			else // Shorter than 16
			{
				//log.debug("Key too Short...");
				newKey = userSalt;
				int howManyTimes = (16 / keySize) - 1;
				//log.debug("Repeating String " + howManyTimes + " times");
				for(int i = 0; i < howManyTimes; i++)
					newKey += userSalt;
				keySize = newKey.length();
				int toAdd = 16 - keySize;
				//log.debug("Adding " + toAdd + " more characters");
				newKey = newKey.concat(userSalt.substring(0, toAdd));
			}
		}
		log.debug("Encryption key is '" + newKey + "'");
		return newKey;
	}
	
	/**
	 * Function that will check if a valid language is set. if not, returns en (English)
	 * @param lang Session Language Parameter
	 * @return en by default, or the valid setting found in the submitted lang
	 */
	public static String validateLanguage(HttpSession ses)
	{
		String result = "en_GB";
		String lang = new String();
		try
		{
			lang = ses.getAttribute("lang").toString();
			//log.debug("lang submitted: " + lang);
			if(lang != null)
			{
				if (!lang.isEmpty())
					result = lang;
			}
			//log.debug("lang set to: " + result);
		}
		catch(Exception e)
		{
			log.error("Could not Retrieve User Lang Setting");
		}
		return result;
	}
	
	/**
	 * Validates objects received through a function request. Also ensures max length is not too high.
	 * @param input Object to validate
	 * @param maxLength Maximum length of object
	 * @return Validated String value or empty string value
	 */
	public static String validateParameter (Object input, int maxLength)
	{
		String result = new String();
		try
		{
			if(input == null)
			{
				result = new String();
			}
			else
			{
				result = (String) input;
				if(result.length() > maxLength)
				{
					log.debug("Parameter Too Long: " + result.length() + " characters");
					log.debug("Parmaeter Was: " + result);
					result = new String();
				}
			}
		}
		catch(Exception e)
		{
			log.debug("Invalid String Parameter: " + e.toString());
			result = new String();
		}
		return result;
	}
	
	/**
	 * Session is checked for credentials and ensures that they have not been modified and that they are valid
	 * @param ses HttpSession from users browser
	 * @return Boolean value that reflects the validity of the users session
	 */
	public static boolean validateSession(HttpSession ses)
	{
		boolean result = false;
		if (ses == null)
		{
			log.debug("No Session Found");
		}
		else
		{
			if (ses.getAttribute("logout") != null) 
			{
				log.debug("Logout Attribute Found: Invalidating session...");
			    ses.invalidate(); // make servlet engine forget the session
			}
			else
			{
				// log.debug("Active Session Found");
				if (ses.getAttribute("userRole") != null)
				{
					try 
					{
						//log.debug("Session holder is "+ses.getAttribute("userName").toString());
						String role = (String) ses.getAttribute("userRole");
						result = (role.compareTo("player") == 0 || role.compareTo("admin") == 0);
						if(!result)
							log.fatal("User Role Parameter Tampered. Role = " + role);
						else
						{
							String userName = ses.getAttribute("userName").toString();
							//Has the user been suspended? Should they be kicked?
							if(UserKicker.shouldKickUser(userName))
							{
								log.debug(userName + " has been Suspended. Invalidating Session and Reporting Invalid Session");
								ses.invalidate(); //Killing Session
								result = false; //User will not access function they were attempting to call
								UserKicker.removeFromKicklist(userName); //Removing from kick list, as they are now authenticated, the DB Layer Suspension will prevent them from signing in
							}
						}
					} 
					catch (Exception e) 
					{
						log.fatal("Tampered Parameter Detected!!! Could not Decrypt stamp");
					}
				}
				else
				{
					log.debug("Session has no credentials");
				}
			}
		}
		return result;
	}

	/**
	 * This method compares the two submitted tokens after ensuring they are not null and not empty.
	 * @param cookieToken CSRF cookie Token
	 * @param requestToken CSRF request Token
	 * @return A boolean value stating weather or not the tokens are valid
	 */
	public static boolean validateTokens (Cookie cookieToken, Object requestToken)
	{
		boolean result = false;
		boolean cookieNull = (cookieToken == null);
		boolean requestNull = (requestToken == null);
		if(!cookieNull && !requestNull)
		{
			try
			{
				String theRequest = (String)requestToken;
				String theCookie = cookieToken.getValue();
				boolean cookieEmpty = theCookie.isEmpty();
				boolean requestEmpty = theRequest.isEmpty();
				
				if(!cookieEmpty && !requestEmpty)
					result = theRequest.compareTo(theCookie) == 0;
				else if (cookieEmpty)
					log.error("Cookie Token Empty");
				else if (requestEmpty)
					log.error("Request Token Empty");
				
				if(!result)
					log.error("CSRF Tokens did not match");
			}
			catch(Exception e)
			{
				log.error("CSRF in Request Error: " + e.toString());
			}
		}
		else
		{
			if(cookieNull)
				log.error("Cookie Token was Null");
			else if (requestNull)
				log.error("Request Token was Null");
		}	
		return result;
	}
	
	/**
	 * Validates file name attributes to defend against path traversal
	 * @param fileName File name to validate
	 * @return Boolean value reflecting if valid or not
	 */
	/*
	public static String validateFileName(String fileName) 
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "fileName: " + fileName);
		fileName = fileName.replaceAll(" ", "").replaceAll("\\.", "").replaceAll("/", "").replaceAll("\\\\", "").replaceAll("\n", "");
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "fileName: " + fileName);
		return fileName;
	}
	*/
	
	public static boolean validHostUrl(String hostUrl)
	{
		//TODO - Pull other validation steps into this
		boolean result;
		result = hostUrl.endsWith("/");
		if (!result)
			log.error("URL Doesn't end with a forward slash. Very likely wrong");
		return result; 
	}
}
