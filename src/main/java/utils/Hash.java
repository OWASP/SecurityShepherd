package utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

/**
 * Class used for miscellaneous Hash use <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark Denihan
 *
 */
public class Hash {
	private static org.apache.log4j.Logger log = Logger.getLogger(Hash.class);
	private static byte[] serverEncryptionKey = randomKeyBytes();

	/**
	 * Generates HMAC with servers random encryption key on user name concatenated
	 * with level's base result key in a user friendly HTML form
	 * 
	 * @param baseKey  The stored result key for the module
	 * @param userSalt Something specific to the user (User name)
	 * @return User Specific Solution in a user friendly HTML form
	 */
	public static String generateUserSolution(String baseKey, String userSalt) {
		log.debug("Generating User Solution...");
		String toReturn = "Key Should be here! Please refresh the home page and try again!";
		String userSpecificSolution = generateUserSolutionKeyOnly(baseKey, userSalt);
		if (userSpecificSolution != null) {
			toReturn = "<script>prepTooltips();prepClipboardEvents();</script>" + "<div class='input-group'>"
					+ "<textarea id='theKey' rows=2 style='height: 30px; display: inline-block; float: left; padding-right: 1em; overflow: hidden; width:85%'>"
					+ userSpecificSolution + "</textarea>" + "<span class='input-group-button'>"
					+ "<button class='btn' type='button' data-clipboard-shepherd data-clipboard-target='#theKey' style='height: 30px;'>"
					+ "<img src='../js/clipboard-js/clippy.svg' width='14' alt='Copy to clipboard'>" + "</button>"
					+ "</span><p>&nbsp;</p>" + "</div>";
		}
		return toReturn;
	}

	/**
	 * Generates HMAC with servers random encryption key on user name concatenated
	 * with level's base result key
	 * 
	 * @param baseKey  The stored result key for the module
	 * @param userSalt Something specific to the user (User name)
	 * @return User Specific Solution Key
	 */
	public static String generateUserSolutionKeyOnly(String baseKey, String userSalt) {
		log.debug("Generating User Solution...");
		String toReturn = null;
		try {
			Mac sha512_HMAC = null;
			final String HMAC_SHA512 = "HmacSHA512";
			sha512_HMAC = Mac.getInstance(HMAC_SHA512);
			byte[] key = getCurrentKey();
			SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_SHA512);
			sha512_HMAC.init(keySpec);
			byte[] mac_data = sha512_HMAC.doFinal((baseKey + userSalt).getBytes("UTF-16"));
			StringBuilder sb = new StringBuilder();
			for (byte b : mac_data) {
				sb.append(String.format("%02X", b));
			}
			String userSpecificSolution = sb.toString();
			log.debug("Returning: " + userSpecificSolution);
			toReturn = userSpecificSolution;
		} catch (Exception e) {
			log.error("Encrypt Failure: " + e.toString());
		}
		return toReturn;
	}

	public static byte[] getCurrentKey() {
		return serverEncryptionKey;
	}

	public static byte[] randomKeyBytes() {
		byte byteArray[] = new byte[16];

		SecureRandom psn1;
		try {
			psn1 = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			log.error("Could not find SHA1PRNG: " + e.toString());
			throw new RuntimeException(e);
		}
		psn1.setSeed(psn1.nextLong());
		psn1.nextBytes(byteArray);

		return byteArray;
	}

	/**
	 * Creates a psedorandom string
	 * 
	 * @return Random String
	 */
	public static String randomString() {
		String result = new String();

		byte byteArray[] = new byte[16];
		
		SecureRandom psn1=null;

		try {
			psn1 = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			log.error("Could not find SHA1PRNG: " + e.toString());
			throw new RuntimeException(e);
		}
		
		psn1.setSeed(psn1.nextLong());
		psn1.nextBytes(byteArray);
		BigInteger bigInt = new BigInteger(byteArray);
		result = bigInt.toString();
		log.debug("Generated String = " + result);

		return result;
	}
}
