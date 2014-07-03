package utils;

/**
 * Class that is responsible for storing a Security Shepherd's instance's host URL and vulnerable application root.
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
public class ExposedServer 
{
	private static String url = "http://127.0.0.1/Exposed/";
	private static String coreProtocol = "http";
	private static String applicationRoot = "";
	private static String secureHost = "127.0.0.1";
	private static String securePort = "";
	private static String secureUrl = "http://127.0.0.1/";
	private static String broadcastAddress = "";
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
		
	public static String getBroadcastAddress() 
	{
		return broadcastAddress;
	}

	public static void setBroadcastAddress(String broadcastAddress) 
	{
		ExposedServer.broadcastAddress = broadcastAddress;
	}

	public static String getSecureHost() 
	{
		return secureHost;
	}

	public static void setSecureHost(String secureHost) 
	{
		ExposedServer.secureHost = secureHost;
	}

	public static String getSecurePort() 
	{
		return securePort;
	}

	public static void setSecurePort(String securePort) 
	{
		ExposedServer.securePort = securePort;
	}
 
	public static String getSecureUrl()
	{
		return secureUrl;
	}
	
	public static String getUrl()
	{
		return url;
	}
	
	public static void setUrl(String url) 
	{
		ExposedServer.url = url;
	}
	public static String getApplicationRoot() 
	{
		return applicationRoot;
	}
	
	public static void setApplicationRoot(String applicationRoot)
	{
		ExposedServer.applicationRoot = applicationRoot;
	}
	

	public static String getCoreProtocol() 
	{
		return coreProtocol;
	}

	public static void setCoreProtocol(String coreProtocol) 
	{
		ExposedServer.coreProtocol = coreProtocol;
	}

	public static void setSecureUrl(String secureUrl)
	{
		ExposedServer.secureUrl = secureUrl;
	}
}
