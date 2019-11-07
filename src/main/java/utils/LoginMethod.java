package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Loads the SSO login configuration, if any. <br/>
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
public class LoginMethod {

	private static boolean isSaml = false;
	private static boolean isSet = false;

	private static org.apache.log4j.Logger log = Logger.getLogger(LoginMethod.class);

	public static boolean isSaml() {

		if (isSet) {
			// Data is cached, so let's fetch it from cache

			return isSaml;

		} else {

			ClassLoader classLoader = LoginMethod.class.getClassLoader();

			String unpackFileName = "sso.properties";

			try (InputStream inputStream = classLoader.getResourceAsStream(unpackFileName)) {
				if (inputStream != null) {
					Properties prop = new Properties();
					prop.load(inputStream);
					if (prop != null) {

						// Get id and name from SAML data

						String isSSOEnabled = prop.getProperty("sso.enabled");

						isSaml = Boolean.parseBoolean(isSSOEnabled);

						isSet = true;

					}
				} else {
					// SSO properties found, we default to sso = false
					isSaml = false;
					isSet = true;
				}
			} catch (IOException e) {
				String errorMsg = "SAML unpack properties file '" + unpackFileName + "' cannot be loaded";

				log.error(errorMsg);
				throw new RuntimeException(errorMsg);

			}

			return isSaml;

		}
	}

	public static Boolean isLogin() {
		return (!isSaml());
	}

}
