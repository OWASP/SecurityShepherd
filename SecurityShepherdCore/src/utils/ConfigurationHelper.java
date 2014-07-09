package utils;

import org.apache.log4j.Logger;


/**
 * This class will store all of the configuration set-up that appears for first time sign in administrators
 * 
 * @author Mark Denihan
 *
 */
public class ConfigurationHelper 
{
	private static boolean hasAlreadyBeenConfigured = false; //Set to false at start up
	private static org.apache.log4j.Logger log = Logger.getLogger(ConfigurationHelper.class); //Logger
	private static String defaultCoreServerAddress = "http://127.0.0.1:8080/";
	private static String defaultExposedServerAddress = "http://127.0.0.1:8080/Exposed/";
	public static boolean alreadyConfigured ()
	{
		if(!hasAlreadyBeenConfigured)
		{
			log.debug("Checking if Configuration Wizard should be shown");
			//Checking ExposedServer Variables for non default values
			boolean defaultValuesDetected = true;
			boolean defaultCoreValueDetected = true;
			boolean defaultExposedValueDetected = true;
			log.debug("exposedServer = " + ExposedServer.getSecureUrl());
			log.debug("thisClass     = " + defaultCoreServerAddress);
			defaultCoreValueDetected = ExposedServer.getSecureUrl().equalsIgnoreCase(defaultCoreServerAddress) || ExposedServer.getSecureUrl().equalsIgnoreCase(defaultCoreServerAddress.replaceFirst("http", "https"));
			if(defaultCoreValueDetected)
				log.info("Default Core Detected");
			
			defaultExposedValueDetected = ExposedServer.getUrl().equalsIgnoreCase(defaultExposedServerAddress) || ExposedServer.getUrl().equalsIgnoreCase(defaultExposedServerAddress.replaceFirst("http", "https"));
			if(defaultExposedValueDetected)
				log.info("Default Exposed Detected");
			
			defaultValuesDetected = defaultCoreValueDetected && defaultExposedValueDetected;
			if(!defaultValuesDetected)
			{
				//Non Default values detected: setting configured variable to true so this will always be skipped
				log.debug("Non-Default server settings detected: setting configured flag to true");
				hasAlreadyBeenConfigured = true;
			}
			
		}
		return hasAlreadyBeenConfigured;
	}
	
	public static void setConfiguredFlag(boolean flag)
	{
		hasAlreadyBeenConfigured = flag;
	}
}
