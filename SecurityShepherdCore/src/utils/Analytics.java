package utils;

/**
 * Manages What Google Analytics is used by the Shepherd instance. If Any
 * @author Mark Denihan
 *
 */
public class Analytics 
{

	public static boolean googleAnalyticsOn = false;
	public static String googleAnalyticsScript = "<script>\n" +
		"(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n" +
		"(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n" +
		"m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n" +
		"})(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n" +
		"\n" +
		"ga('create', 'UA-51746570-1', 'securityshepherd.eu');\n" +
		"ga('send', 'pageview');\n" +
		"</script>";
	public static String sourceForgeMobileVmLinkBlurb = new String(""
			+ "To complete this challenge you'll need to use the <a href='http://bit.ly/mobileShepherdVm'>Security Shepherd Android Virtual Machine</a> that contains the app. ");
	private static String mobileVmLinkBlurb1 = new String("To complete this challenge you'll need to use the ");
	private static String mobileVmLinkBlurb2 = new String(" app found in the <a href='http://bit.ly/mobileShepherdVm' target='_blank'>Security Shepherd Android Virtual Machine</a>.");
	
	public static String getMobileLevelBlurb (String appName)
	{
		return mobileVmLinkBlurb1 + appName + mobileVmLinkBlurb2;
	}
}
