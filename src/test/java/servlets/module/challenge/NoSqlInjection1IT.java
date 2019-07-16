package servlets.module.challenge;

import org.apache.log4j.Logger;
import dbProcs.GetterTest;
import dbProcs.Setter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.fail;


public class NoSqlInjection1IT extends Mockito {

    private static String applicationRoot = new String();
    private static String USERNAME = "lessonTester";
    private static String LANG = "en_GB";
    private static String NOSQL_ATTACK = "';return(true);var a='a";

    private static org.apache.log4j.Logger log = Logger.getLogger(NoSqlInjection1IT.class);
    private ResourceBundle bundle = ResourceBundle.getBundle
            ("i18n.servlets.challenges.injection.nosql", new Locale(LANG));

    @Mock
    private MockHttpServletRequest request;

    @Mock
    private MockHttpServletResponse response;

    @BeforeClass
    public static void initAll()
    {
        TestProperties.setTestPropertiesFileDirectory(log);
    }

    @Before
    public void init()
    {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        //Open All modules
        if(!Setter.openAllModules(applicationRoot))
            fail("Could not Mark All Modules As Open");
    }


    public String moduleDoPost(String theSubmission, String csrfToken, int expectedResponseCode) throws Exception
    {
        try
        {
            String servletClassName = "NoSqlInjection1";
            log.debug("Creating " + servletClassName + " Servlet Instance");
            NoSqlInjection1 servlet = new NoSqlInjection1();
            servlet.init(new MockServletConfig(servletClassName));

            //Setup Servlet Parameters and Attributes
            log.debug("Setting Up Params and Atrributes");
            request.addParameter("theGamerName", theSubmission);
            //Adding Correct CSRF Token (Token Submitted)
            request.addParameter("csrfToken", csrfToken);

            log.debug("Running doPost");
            servlet.doPost(request, response);

            if(response.getStatus() != expectedResponseCode)
                fail(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode + " Expected");
            else
            {
                log.debug("302 OK Detected");
                log.debug(servletClassName + " Successful, returning location retrieved: " + response.getContentAsString());
                return(response.getContentAsString());
            }
        }
        catch(Exception e)
        {
            throw e;
        }
        return null;
    }


    @Test
    public void testLevelValidAnswer()
    {
        try {
            GetterTest.verifyTestUser(applicationRoot, USERNAME, USERNAME);
            log.debug("Signing in as " + USERNAME + " Through LoginServlet");
            TestProperties.loginDoPost(log, request, response, USERNAME, USERNAME, null, LANG);
            log.debug("Login Servlet Complete, Getting CSRF Token");
            if (response.getCookie("token") == null)
                fail("No CSRF Token Was Returned from Login Servlet");
            String csrfToken = response.getCookie("token").getValue();
            if (csrfToken.isEmpty())
            {
                String message = new String("No CSRF token returned from Login Servlet");
                log.fatal(message);
                fail(message);
            }
            else
            {
                request.setCookies(response.getCookies());
                String servletResponse = moduleDoPost(NOSQL_ATTACK, csrfToken, 302);
                if(servletResponse.contains("An error was detected"))
                {
                    String message = new String("Valid Key Returned Funky Error");
                    log.fatal(message);
                    fail(message);
                }
                else if(!servletResponse.contains("Marlo</td><td>Baltimore</td></tr><tr><td>b6c02"))
                {
                    String message = new String("Valid Solution did not return Result Key");
                    log.fatal(message);
                    fail(message);
                }
            }
        }
        catch (IOException e) {fail("IO Exception should not have been thrown");}
        catch (ServletException e) {fail("ServletException should not have been thrown");}
        catch (Exception e) {fail("Exception Thrown" + e);}

    }

}