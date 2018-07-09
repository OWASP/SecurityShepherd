package utils;

/**
 * This class holds the status of weather feedback is enabled or not
 * @author Mark
 *
 */
public class FeedbackStatus 
{
	private static boolean enabled = false;
	private static boolean disabled = true;
	
	public static boolean isEnabled() 
	{
		return enabled;
	}
	
	public static void setEnabled() 
	{
		enabled = true;
		disabled = false;
	}
	
	public static boolean isDisabled()
	{
		return disabled;
	}
	public static void setDisabled() 
	{
		disabled = true;
		enabled = false;
	}
}
