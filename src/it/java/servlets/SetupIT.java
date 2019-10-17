package servlets;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Constants;
import dbProcs.Getter;
import dbProcs.GetterTest;
import dbProcs.Setter;
import testUtils.TestProperties;
import utils.InstallationException;

public class SetupIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(SetupIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private StringBuffer dbProp= new StringBuffer();

	/**
	 * Initialize directories
	 */
	@BeforeClass
	public static void setDirectory() 
	{
		TestProperties.setTestPropertiesFileDirectory(log);
	}
    
	@Before
	public void setup() 
	{
		log.debug("Setting Up Blank Request and Response");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

	}
	
	@Test
	public void testAuthCreation() {
		try {
			log.debug("Creating Setup Servlet Instance");
			Setup servlet = new Setup();
			servlet.init(new MockServletConfig("Setup"));
					
			String authData= Files.readString(Paths.get(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
			request.getSession().setAttribute("lang", lang);

			request.getSession().setAttribute("dbAuth", authData);

			log.debug("Running doPost");
			servlet.doPost(request, response);
			
			String location = response.getHeader("Location");
			if(!location.endsWith("login.jsp"))
			{
				throw new Exception("Setup not Redirecting to login.jsp.");
			}
		} catch (Exception e) {
			fail();
		}
	}
	
}
