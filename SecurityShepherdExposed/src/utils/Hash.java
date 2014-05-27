package utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import servlets.OneTimePad;

/**
 * Class used for miscellaneous Hash use
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
public class Hash 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Hash.class);
	public static String userNameKey = "NsH{[_pLw2Q.3gOz";
	/**
	 * Outputs a SHA256 digest
	 * @param toHash String to hash
	 * @return Hashed string
	 */
	public static String thisString (String toHash)
	{
		String hashed = null;
		byte[] byteArray = new byte[32];
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("SHA");
			log.debug("Hashing Value With " + md.getAlgorithm());
			byteArray = toHash.getBytes();
			md.update(byteArray);
			byteArray = md.digest();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			log.fatal("Could not Find SHA Algorithm: " + e.toString());
		}
		hashed = new String(byteArray);

		return hashed;
	}
	
	/**
	 * Outputs a MD5 digest
	 * @param toHash String to hash
	 * @return Hashed String
	 */
	public static String md5ThisString (String toHash)
	{
		String hashed = null;
		byte[] byteArray = new byte[512];
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("MD5");
			log.debug("Hashing Value With " + md.getAlgorithm());
			byteArray = toHash.getBytes();
			md.update(byteArray);
			byteArray = md.digest();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			log.fatal("Could not Find MD5 Algorithm: " + e.toString());
		}
		hashed = new String(byteArray);

		return hashed;
	}
	
	/**
	 * Creates a psedorandom string
	 * @return Random String
	 */
	public static String randomString() 
	{
		String result = new String();
		try
		{
			byte byteArray[] = new byte[32];
			SecureRandom psn1 = SecureRandom.getInstance("SHA1PRNG");
			psn1.setSeed(psn1.nextLong());
			psn1.nextBytes(byteArray);
			BigInteger bigInt = new BigInteger(byteArray);
			result = bigInt.toString();
			log.debug("Generated String = " + result);
		}
		catch(Exception e)
		{
			log.error("Random Number Error : " + e.toString());
		}
		return result;
	}
	
	public static String generateUserSolution(String baseKey, Cookie[] cookies)
	{
		log.debug("Getting user Specific key");
		String cookieName = "JSESSIONID3";
		Cookie myCookie = null;
		String toReturn = "Key Should be here! Please refresh the home page and try again!";
		log.debug("Looking for key cookie");
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++) 
			{
				log.debug("Looking at: " + cookies[i].getName() + " = " + cookies[i].getValue());
				if (cookies[i].getName().equals(cookieName))
				{
					myCookie = cookies[i];
					log.debug("Found Cookie with value: " + myCookie.getValue());
					break;
				}
			}
			try 
			{
				String decryptedUserName = Hash.decrypt(Hash.userNameKey, myCookie.getValue());
				log.debug("Decrypted UserName: " + decryptedUserName);
				toReturn = Hash.encrypt(Hash.validateEncryptionKey(decryptedUserName), baseKey);
				log.debug("Returning: " + toReturn);
			} 
			catch (Exception e) 
			{ 
				log.error("Encryption Failure: " + e.toString());
				toReturn = "Key Should be here! Please refresh the home page and try again!";
			}
		}
		return "<b style='word-wrap: break-word;'>" + toReturn + "</b>";
	}
	
	public static String decryptUserName (String encyptedUserName)
	{
		String decryptedUserName = new String();
		try 
		{
			decryptedUserName = Hash.decrypt(Hash.userNameKey, encyptedUserName);
			log.debug("Decrypted username to: " + decryptedUserName);
		} 
		catch (GeneralSecurityException e)
		{
			log.error("Could not decrypt username: " + e.toString());
		}
		return decryptedUserName;
	}
	
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
}
