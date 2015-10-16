package servlets;

import static org.junit.Assert.fail;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

public class LoginTest
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(LoginTest.class);
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;
	
	@Before
	public void setup() 
	{
		log.debug("Setting Up Blank Request and Response");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
	}
	
	@Test
	public void testDoPostHttpServletRequestHttpServletResponse() 
	{
		try
		{
			//Param Values
			String userName = "admin";
			String password = "passw0rd";
			int expectedResponseCode = 200;
			
			log.debug("Creating Login Servlet Instance");
			Login servlet = new Login();
			servlet.init(new MockServletConfig("Login"));
			
			
			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("login", userName);
			request.addParameter("pwd", password);
			request.getSession().setAttribute("lang", lang);
			
			log.debug("Running doPost");
			servlet.doPost(request, response);
			
			if(response.getStatus() != expectedResponseCode)
				fail("Login Servlet Returned " + response.getStatus() + " Code");
			else
				log.debug("200 OK Detected");
		}
		catch(Exception e)
		{
			log.error("Could not Complete testDoPost: " + e.toString());
		}
	}
	/*
	@Test
	public void testDoGetHttpServletRequestHttpServletResponse() {
		fail("Not yet implemented");
	}
	*/
}
