package servlets;

/**
 * One Time pad encryption for user specific keys.
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
 * 
 * @author Jason
 *
 */
public class OneTimePad {

	  private static final String KEY = "SuperKalaFragalisticExpeAloDocious";
	  
	  /**
	   * Encrypts the supplied string value using the default key
	   * @param text The string that is to be encrypted
	   * @return The resulting string from encryption
	   */
	  public static String encrypt(final String text) 
	  {
		  return new String(org.apache.commons.codec.binary.Base64.encodeBase64String(xor(text.getBytes())).getBytes());
	  }
	  
	  /**
	   * Encrypts the supplied string value using the submitted key
	   * @param key The encryption key
	   * @param text The string that is to be encrypted
	   * @return The resulting string from encryption
	   */
	  public static String encrypt(final String text, String key) 
	  {
		  return new String(org.apache.commons.codec.binary.Base64.encodeBase64String(xor(text.getBytes(), key)).getBytes());
	  }
	  
	  /**
	   * Decrypts  the supplied string value using the default key
	   * @param hash The cipher text to be decrypted
	   * @return The plain text revealed from the decryption
	   * @throws Exception Throws illegal state Exception
	   */
	  public static String decrypt(String hash) throws Exception 
	  {
	    try 
	    {
	     	return new String(xor(org.apache.commons.codec.binary.Base64.decodeBase64(hash.getBytes())), "UTF-8");
	    } 
	    catch (java.io.UnsupportedEncodingException ex) 
	    {
	      throw new IllegalStateException(ex);
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
	   * @return
	   */
	  private static byte[] xor(final byte[] input) 
	  {
	    final byte[] output = new byte[input.length];
	    final byte[] secret = KEY.getBytes();
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
