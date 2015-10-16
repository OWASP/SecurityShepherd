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

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

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
	public static String userNameKey = randomKeyLengthString();
	private static String encryptionKeySalt = randomKeyLengthString();
	private static String serverEncryptionKey = randomKeyLengthString();
	
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
			decryptedUserName = Hash.decrypt(Hash.userNameKey, encyptedUserName);
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
	
	/**
	 * Generates user solution based on the user name stored in their encypted cookie
	 * @param baseKey The stored result key for the module
	 * @param cookies All of a users session cookies. The encrypted user name is pulled out from this array
	 * @return User Specific Solution
	 */
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
				String key = createUserSpecificEncryptionKey(Validate.validateEncryptionKey(decryptedUserName));
				toReturn = Hash.encrypt(key, baseKey + getCurrentSalt());
				log.debug("Returning: " + toReturn);
			} 
			catch (Exception e) 
			{ 
				log.error("Encryption Failure: " + e.toString());
				toReturn = "Key Should be here! Please refresh the home page and try again! If that doesn't work, sign in and out again!";
			}
		}
		return "<b style='word-wrap: break-word;'>" + toReturn + "</b>";
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
				String forLog = Hash.encrypt(key, baseKey + getCurrentSalt());
				toReturn = "<script>prepTooltips();prepClipboardEvents();</script><div class='input-group'>" +
								"<textarea id='theKey' rows=2 style='display: inline-block; float: left; padding-right: 1em; overflow: hidden; width:85%'>"+forLog+"</textarea>" +
								"<span class='input-group-button'>" +
									"<button class='btn' type='button' data-clipboard-shepherd data-clipboard-target='#theKey'>" +
										"<img src='../js/clipboard-js/clippy.svg' width='14' alt='Copy to clipboard'>" +
									"</button>" +
								"</span><p>&nbsp;</p>";

				log.debug("Returning: " + forLog);
			} 
			catch (Exception e) 
			{ 
				log.error("Encrypt Failure: " + e.toString());
				toReturn = "Key Should be here! Please refresh the home page and try again!";
			}
		return toReturn;
	}
	
	/**
	 * This is used when encrypting/decrypting the salt. If this is bypassed characters can be lost in encryption process.
	 * @return
	 */
	public static String getCurrentSalt()
	{
		return Base64.encodeBase64String(encryptionKeySalt.getBytes());
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
		hashed = new String(byteArray, Charset.forName("US-ASCII"));

		return hashed;
	}
	
	/**
	 * Creates a psedorandom base64 string
	 * @return Random String
	 */
	public static String randomBase64String() 
	{
		String result = new String();
		try
		{
			byte byteArray[] = new byte[256];
			SecureRandom psn1 = SecureRandom.getInstance("SHA1PRNG");
			
			Base64 base64 = new Base64();
			psn1.setSeed(psn1.nextLong());
			psn1.nextBytes(byteArray);
			result = new String(byteArray, Charset.forName("US-ASCII"));
			result = base64.encode(thisString(thisString(byteArray.toString())).getBytes()).toString();
			log.debug("Generated String = " + result);
		}
		catch(Exception e)
		{
			log.error("Random Number Error : " + e.toString());
		}
		return result;
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
	 * Creates a psedorandom string
	 * @return Random String
	 */
	public static String randomString() 
	{
		String result = new String();
		try
		{
			byte byteArray[] = new byte[16];
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
	
	/**
	 * Generates a small psedorandom string
	 * @return Random String
	 */
	public static String smallRandomString() 
	{
		String result = new String();
		try
		{
			byte byteArray[] = new byte[4];
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
	
	/**
	 * Outputs a SHA256 digest
	 * @param toHash String to hash
	 * @return Hashed string
	 */
	public static String thisString (String toHash)
	{
		String hashed = null;
		byte[] byteArray = new byte[256];
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
		hashed = new String(byteArray, Charset.forName("US-ASCII"));

		return hashed;
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
	
}
