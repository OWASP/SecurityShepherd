package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import dbProcs.Getter;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This level is based on the bug reported against shepherds previous method of creating user specific keys. The old mechanism is used here and can be broken to create keys by finding out the server secrets
 * 
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
 * 
 * @author Mark Denihan
 *
 */
public class BrokenCryptoHomeMade extends HttpServlet
{
	private static final String levelName = new String("Insecure Cryptographic Storage Home Made Key");
	private static final String levelHash = new String("9e5ed059b23632c8801d95621fa52071b2eb211d8c044dde6d2f4b89874a7bc4");
	private static final long serialVersionUID = 1L;
	public static String userNameKey = randomKeyLengthString();
	private static String serverEncryptionKey = randomKeyLengthString();
	private static String encryptionKeySalt = randomKeyLengthString();
	private static org.apache.log4j.Logger log = Logger.getLogger(BrokenCryptoHomeMade.class);
	public static List<List<String>> challenges = new ArrayList<List<String>>();
	public static boolean initDone = false;
	
	public static void initLists()
	{
		
		ArrayList<String> challenge = new ArrayList<String>();
		if(!initDone)
		{
			initDone = true;
			challenge.add("SQL Injection");
			challenge.add("E7182FB9A24F91723EC");
			challenges.add(challenge);
			challenge = new ArrayList<String>();
			challenge.add("Cross-Site Scripting");
			challenge.add("FAB281864D21E23C289");
			challenges.add(challenge);
			challenge = new ArrayList<String>();
			challenge.add("CSRF Lesson");
			challenge.add("89172BFE192C2184670");
			challenges.add(challenge);
			challenge = new ArrayList<String>();
			challenge.add("Security Misconfig");
			challenge.add("0138AA00F22317CBC27");
			challenges.add(challenge);
			challenge = new ArrayList<String>();
			challenge.add("This Challenge");
			challenge.add("F1E8B0C6D54A182D217");
			challenges.add(challenge);
		}
	}
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
			{
				initLists();
				//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
				HttpSession ses = request.getSession(true);
				String htmlOutput = new String();
				PrintWriter out = response.getWriter();  
				if(Validate.validateSession(ses))
				{
					ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
					log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
					Cookie tokenCookie = Validate.getToken(request.getCookies());
					Object tokenParmeter = request.getParameter("csrfToken");
					if(Validate.validateTokens(tokenCookie, tokenParmeter))
					{
						//Translation Stuff
						Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
						ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.insecureCryptoStorage.insecureCryptoStorage", locale);
						out.print(getServletInfo());
						try
						{
							if(ses.getAttribute("homemadebadanswers") == null)
							{
								ses.setAttribute("homemadebadanswers", 0);
							}
							
							int homemadebadanswers;
							try
							{
								homemadebadanswers = (int) ses.getAttribute("homemadebadanswers");
							}
							catch(ClassCastException e)
							{
								homemadebadanswers = Integer.parseInt((String)ses.getAttribute("homemadebadanswers"));
							}
							log.debug(homemadebadanswers + "previous bad attempts");
							if(homemadebadanswers < 5)
							{
								String submittedSolution = request.getParameter("theSubmission");
								String expectedSolution = BrokenCryptoHomeMade.generateUserSolutionKeyOnly(BrokenCryptoHomeMade.challenges.get(4).get(1), ses.getAttribute("userName").toString());
								if(submittedSolution.equals(expectedSolution))
								{
									log.debug("Correct Solution Submitted for 'This Challenge'. Returning Key");
									htmlOutput = "<h2 class='title'>" + bundle.getString("result.wellDone") + "</h2>" +
											"<p>" + bundle.getString("result.youDidIt") + "<br />" +
											bundle.getString("result.resultKey") + " <a>" +
												Hash.generateUserSolution(
														Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash),
													(String)ses.getAttribute("userName")
												)
											+ "</a>";
								}
								else
								{
									log.debug("Expected: " + expectedSolution);
									log.debug("Got     : " + submittedSolution);
									htmlOutput = "<h2 class='title'>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer") + "</h2><p>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer.warning") + "</p>";
									homemadebadanswers++;
									if(homemadebadanswers >= 5)
									{
										htmlOutput += "<p>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer.lockedOut") + "</p>";
									}
									else
									{
										htmlOutput += "<p>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer.notLockedOut") + "</p>";
										ses.setAttribute("homemadebadanswers", homemadebadanswers);
									}
								}
							}
							else
							{
								htmlOutput += "<h2 class='title'>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer") + "</h2><p>" + bundle.getString("insecureCyrptoStorage.homemade.badanswer.lockedOut") + "</p>";
							}
						}
						catch(Exception e)
						{
							log.debug("Exception: " + e.toString());
							htmlOutput += "<p>" + bundle.getString("result.failed") + "</p>";
						}
					}
				}
				else
				{
					log.error(levelName + " servlet accessed with no session");
				}
				out.write(htmlOutput);
				out.close();
			}
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
			{
				initLists();
				//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
				HttpSession ses = request.getSession(true);
				String htmlOutput = new String();
				PrintWriter out = response.getWriter();  
				if(Validate.validateSession(ses))
				{
					ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
					log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
					Cookie tokenCookie = Validate.getToken(request.getCookies());
					Object tokenParmeter = request.getParameter("csrfToken");
					if(Validate.validateTokens(tokenCookie, tokenParmeter))
					{
						//Translation Stuff
						Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
						ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.insecureCryptoStorage.insecureCryptoStorage", locale);
						out.print(getServletInfo());
						try
						{
							String name = new String();
							if(request.getParameter("name") != null)
							{
								name = request.getParameter("name").toString();
							}
							if(name.length() < 4)
							{
								htmlOutput = bundle.getString("insecureCyrptoStorage.homemade.nameTooShort");
							}
							else
							{
								for(int i = 0; i < BrokenCryptoHomeMade.challenges.size(); i++)
								{
									htmlOutput += "<tr><td>"+BrokenCryptoHomeMade.challenges.get(i).get(0)+"</td>";
									htmlOutput += "<td>"+ BrokenCryptoHomeMade.challenges.get(i).get(1) + "</td>";
									if(!BrokenCryptoHomeMade.challenges.get(i).get(0).equalsIgnoreCase("This Challenge"))
									{
										htmlOutput += "<td>" + BrokenCryptoHomeMade.generateUserSolution(BrokenCryptoHomeMade.challenges.get(i).get(1), name) + "</td>";
									}
									else
									{
										htmlOutput += "<td></td>";
									}
									htmlOutput += "</tr>";
								}
							}
						}
						catch(Exception e)
						{
							log.debug("Exception: " + e.toString());
							htmlOutput += "<p>" + bundle.getString("result.failed") + "</p>";
						}
					}
				}
				else
				{
					log.error(levelName + " servlet accessed with no session");
				}
				out.write(htmlOutput);
				out.close();
			}
	
	/**
	 * Merges current server encryption key with user name based encryption key to create user specific key
	 * @param userName
	 * @return
	 */
	private static String createUserSpecificEncryptionKey (String userNameKey) throws Exception 
	{
		if(userNameKey.length() != 16)
			throw new Exception("User Name key must be 16 bytes long");
		else
		{
			byte[] serverKey = serverEncryptionKey.getBytes();
			byte[] userKey = userNameKey.getBytes();
			for(int i = 0; i < userKey.length; i++)
			{
				userKey[i] = (byte)(userKey[i] + serverKey[i]);
			}
			return new String(userKey, Charset.forName("US-ASCII"));
		}
	}
	
	/**
	 * Decrypts data using specific key and ciphertext
	 * @param key Encryption Key (Must be 16 Bytes)
	 * @param encrypted Ciphertext to decrypt
	 * @return Plaintext decrypted from submitted ciphertext and key
	 * @throws GeneralSecurityException
	 */
	public static String decrypt(String key, String encrypted)
	throws GeneralSecurityException 
	{
		byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
		if (raw.length != 16)
		{
			throw new IllegalArgumentException("Invalid key size.");
		}
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
		byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
		return new String(original, Charset.forName("US-ASCII"));
	}
	
	
	
	/**
	 * Specifically decrypts encrypted user names
	 * @param encyptedUserName Encrypted user name
	 * @return Decrypted User name
	 */
	public static String decryptUserName (String encyptedUserName)
	{
		String decryptedUserName = new String();
		try 
		{
			decryptedUserName = decrypt(userNameKey, encyptedUserName);
			log.debug("Decrypted user-name to: " + decryptedUserName);
		} 
		catch (GeneralSecurityException e)
		{
			log.error("Could not decrypt user name: " + e.toString());
		}
		return decryptedUserName;
	}
	
	public static String decryptUserSpecificSolution(String userNameKey, String encryptedSolution)
		throws GeneralSecurityException, Exception 
	{
		try
		{
			String key = createUserSpecificEncryptionKey(userNameKey);
			byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
			if (raw.length != 16)
			{
				throw new IllegalArgumentException("Invalid key size.");
			}
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
			byte[] original = cipher.doFinal(Base64.decodeBase64(encryptedSolution));
			return new String(original, Charset.forName("US-ASCII"));
		}
		catch (Exception e)
		{
			throw new Exception("Decryption Failure: Could not Craft User Key or Ciphertext was Bad");
		}
	}
	
	/**
	 * Encrypts plain text into cipher text based on encryption key
	 * @param key Encryption Key (Must be 16 Bytes)
	 * @param value Plain text to encrypt
	 * @return Cipher text based on plain text and key submitted
	 * @throws GeneralSecurityException
	 */
	public static String encrypt(String key, String value)
	throws GeneralSecurityException 
	{
		byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
		if (raw.length != 16) 
		{
			throw new IllegalArgumentException("Invalid key size.");
		}
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
		return Base64.encodeBase64String(cipher.doFinal(value.getBytes(Charset.forName("US-ASCII"))));
	}
	
	public static String randomKeyLengthString()
	{
		String result = new String();
		try
		{
			byte byteArray[] = new byte[16];
			SecureRandom psn1 = SecureRandom.getInstance("SHA1PRNG");
			psn1.setSeed(psn1.nextLong());
			psn1.nextBytes(byteArray);
			result = new String(byteArray, Charset.forName("US-ASCII"));
			//log.debug("Generated Key = " + result);
			if(result.length() != 16)
			{
				log.error("Generated Key is the incorrect Length: Shortening ");
				result = result.substring(0, 15);
				if(result.length() != 16)
					log.fatal("Encryption key length is Still not Right");
			}
		}
		catch(Exception e)
		{
			log.error("Random Number Error : " + e.toString());
		}
		return result;
	}
	
	/**
	 * Generates user specific solution based on the user name provided and server side encryption keys
	 * @param baseKey The stored result key for the module
	 * @param userSalt The User Specific Encryption Salt (Based on user name)
	 * @return User Specific Solution
	 */
	public static String generateUserSolution(String baseKey, String userSalt)
	{
		log.debug("Generating key for " + userSalt);
		String toReturn = "Key Should be here! Please refresh the home page and try again!";

			try 
			{
				String key = createUserSpecificEncryptionKey(Validate.validateEncryptionKey(userSalt));
				String forLog = BrokenCryptoHomeMade.encrypt(key, baseKey + getCurrentSalt());
				toReturn = "<script>prepTooltips();prepClipboardEvents();</script>"
						+ "<div class='input-group'>" +
								"<textarea id='theKey' rows=2 style='height: 30px; display: inline-block; float: left; padding-right: 1em; overflow: hidden; width:65%'>"+forLog+"</textarea>" +
								"<span class='input-group-button'>" +
									"<button class='btn' type='button' data-clipboard-shepherd data-clipboard-target='#theKey' style='height: 30px;'>" +
										"<img src='../js/clipboard-js/clippy.svg' width='14' alt='Copy to clipboard'>" +
									"</button>" +
								"</span><p>&nbsp;</p>"
						+ "</div>";


				log.debug("Returning: " + forLog);
			} 
			catch (Exception e) 
			{ 
				log.error("Encrypt Failure: " + e.toString());
				toReturn = "Key Should be here! Please refresh the home page and try again!";;
			}
		return toReturn;
	}
	
	public static String generateUserSolutionKeyOnly(String baseKey, String userSalt)
	{
		log.debug("Generating key for " + userSalt);
		String forLog = "Key Should be here! Please refresh the home page and try again!";

			try 
			{
				String key = createUserSpecificEncryptionKey(Validate.validateEncryptionKey(userSalt));
				forLog = BrokenCryptoHomeMade.encrypt(key, baseKey + getCurrentSalt());

				log.debug("Returning: " + forLog);
			} 
			catch (Exception e) 
			{ 
				log.error("Encrypt Failure: " + e.toString());
			}
		return forLog;
	}
	
	/**
	 * This is used when encrypting/decrypting the salt. If this is bypassed characters can be lost in encryption process.
	 * @return
	 */
	public static String getCurrentSalt()
	{
		return Base64.encodeBase64String(encryptionKeySalt.getBytes());
	}
}
