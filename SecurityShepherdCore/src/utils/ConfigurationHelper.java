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
			defaultCoreValueDetected = ExposedServer.getSecureUrl().contains("127.0.0.1");
			if(defaultCoreValueDetected)
				log.info("Default Core Detected");
			
			defaultExposedValueDetected = ExposedServer.getUrl().contains("127.0.0.1");
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
