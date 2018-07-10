package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Bad Crypto Challenge Three
 * Really bad crypto algorithm to break. Will reveal key if spaces are submitted
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
public class BrokenCrypto3 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(BrokenCrypto3.class);
	private static String levelName = "Broken Crypto Challenge 3";
	public static String levelHash = "2da053b4afb1530a500120a49a14d422ea56705a7e3fc405a77bc269948ccae1";
	public static String levelResult = "thisisthesecurityshepherdabcencryptionkey"; //Is used as encryption key in this level

	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{			
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			String htmlOutput = new String();
			
			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			
			//Translation Stuff
			Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
			ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
			ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.insecureCryptoStorage.insecureCryptoStorage", locale);
			try
			{
				String userData = request.getParameter("userData");
				log.debug("User Submitted - " + userData);
				
				log.debug("Decrypting user input");
				//Using level key as encryption key
				String decryptedUserData = decrypt(userData, levelResult);
				log.debug("Decrypted to: " + decryptedUserData);
				
				htmlOutput = "<h2 class='title'>" + bundle.getString("insecureCyrptoStorage.3.plaintextResult") + "</h2><p>" + bundle.getString("insecureCyrptoStorage.3.plaintextResult.message") + "<br/><br/><em>"
						+ Encode.forHtml(decryptedUserData)
						+ "</em></p>";
			}
			catch(Exception e)
			{
				log.fatal(levelName + " - " + e.toString());
				htmlOutput = errors.getString("error.funky");
			}
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
	
	/**
	   * Decrypts the supplied string value using the submitted key
	   * @param hash The cipher text to be decrypted
	   * @param key The encryption key
	   * @return The plain text revealed from the decryption
	   * @throws Exception Throws illegal state Exception
	   */
	  public static String decrypt(String hash, String key) throws Exception 
	  {
	    try 
	    {
	    	return new String(xor(org.apache.commons.codec.binary.Base64.decodeBase64(hash.getBytes()), key), "UTF-8");
	    } 
	    catch (java.io.UnsupportedEncodingException ex) 
	    {
	      throw new IllegalStateException(ex);
	    }
	  }
	  
	  /**
	   * XOR Function
	   * @param input Byte array to be XOR'd
	   * @param key Encryption Key
	   * @return
	   */
	  private static byte[] xor(final byte[] input, String theKey) 
	  {
	    final byte[] output = new byte[input.length];
	    final byte[] secret = theKey.getBytes();
	    int spos = 0;
	    for (int pos = 0; pos < input.length; pos += 1) 
	    {
	      output[pos] = (byte) (input[pos] ^ secret[spos]);
	      spos += 1;
	      if (spos >= secret.length) {
	        spos = 0;
	      }
	    }
	    return output;
	  }
}
