package utils;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;

/**
 * Keeps track if we do normal login or SSO login
 * 
 * @author Mark Denihan
 *
 */
public class LoginMethod {

	public static String getLoginMethod() {
		
		boolean isSaml=false;
		
		try {
			Auth auth = new Auth();
			auth.toString();
			isSaml=true;
		} catch (IOException | SettingsException | Error e) {
			// Does not use SAML
			isSaml=false;
		}
		
		if(isSaml)	{
			return "saml";
		}
		else {
			return "login";
		}
		
	}
	
	public static Boolean isSaml() {
		return (getLoginMethod().equals("saml"));
	}
	
	public static Boolean isLogin() {
		return (getLoginMethod().equals("login"));
	}

}
