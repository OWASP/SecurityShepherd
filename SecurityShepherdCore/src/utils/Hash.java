package utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import org.apache.commons.codec.binary.Base64;

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
	/**
	 * Outputs a SHA256 digest
	 * @param toHash String to hash
	 * @return Fashed string
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
			result = new String(byteArray);
			result = base64.encode(thisString(thisString(byteArray.toString())).getBytes()).toString();
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
	
	public static String generateUserSolution(String baseKey, String userSalt)
	{
		log.debug("Generating key for " + userSalt);
		String toReturn = "Key Should be here! Please refresh the home page and try again!";
			try 
			{
				toReturn = OneTimePad.encrypt(baseKey, userSalt);
				log.debug("Returning: " + toReturn);
			} 
			catch (Exception e) 
			{ 
				log.error("OneTimePad Failure: " + e.toString());
				toReturn = "Key Should be here! Please refresh the home page and try again!";
			}
		return toReturn;
	}
}
