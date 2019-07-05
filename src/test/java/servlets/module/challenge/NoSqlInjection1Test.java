package servlets.module.challenge;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.FongoDB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;
import dbProcs.GetterTest;
import dbProcs.Setter;
import org.json.JSONObject;
import org.junit.AfterClass;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;


public class NoSqlInjection1Test extends Mockito {

    private static String MONGO_COLL_NAME = "shepherdGames";
    private static Fongo fongo = new Fongo(MONGO_COLL_NAME);
    private static MongoClient mongoClient;
    private static FongoDB fakeDB;
    private static String applicationRoot = new String();
    private static String USERNAME = "lessonTester";
    private static String TEST_DB = "shepherdTest";
    private static String LANG = "en_GB";
    private static String SOLUTION = "c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a";

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NoSqlInjection1Test.class);
    private ResourceBundle bundle = ResourceBundle.getBundle
            ("i18n.servlets.challenges.injection.nosql", new Locale(LANG));

    @Mock
    private MockHttpServletRequest request;

    @Mock
    private MockHttpServletResponse response;

    @BeforeClass
    public static void initAll()
    {
        fakeDB = fongo.getDB(TEST_DB);
        mongoClient = fongo.getMongo();
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

        //Populate the DB
        DBCollection dbCollection = fakeDB.getCollection(MONGO_COLL_NAME);
        BasicDBObject user = new BasicDBObject("_id", "c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a")
                .append("name", "Marlo")
                .append("address", "Baltimore");
        dbCollection.insert(user);
    }

    @AfterClass
    public static void teardown()
    {
        fakeDB.getCollection(MONGO_COLL_NAME).drop();
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


    @Test(expected = MissingResourceException.class)
    //@DisplayName("Normal HTTP Request to MongoDb")
    public void testResultMatches() throws Exception
    {

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        Cursor cursor;
        Object id;
        Object name;
        Object address;

        try {

            DBCollection dbCollection = fakeDB.getCollection(MONGO_COLL_NAME);
            DBObject firstMatched = dbCollection.findOne();

            JSONObject jsonObject = new JSONObject(firstMatched.toString());
            String solution = jsonObject.getString("_id");

            log.debug("THE SOLUTION: " + solution);


            GetterTest.verifyTestUser(applicationRoot, USERNAME, USERNAME);
            log.debug("Signing in as " + USERNAME + " Through LoginServlet");
            TestProperties.loginDoPost(log, request, response, USERNAME, USERNAME, null, LANG);
            log.debug("Login Servlet Complete, Getting CSRF Token");
            if (response.getCookie("token") == null)
                fail("No CSRF Token Was Returned from Login Servlet");
            String csrfToken = response.getCookie("token").getValue();
            if (csrfToken.isEmpty()) {
                String message = new String("No CSRF token returned from Login Servlet");
                log.fatal(message);
                fail(message);
            } else {
                request.setCookies(response.getCookies());
                String servletResponse = moduleDoPost(solution, csrfToken, 302);

                if(servletResponse.contains(bundle.getString("result.failed")) ||
                        servletResponse.contains(bundle.getString("result.mongoError")))
                {
                    String message = new String("Valid Key Returned Funky Error");
                    log.fatal(message);
                    fail(message);
                }
                else if(!servletResponse.contains(SOLUTION))
                {
                    String message = new String("Valid Solution did not return Result Key");
                    log.fatal(message);
                    fail(message);
                }

                assertEquals(SOLUTION, solution);
            }
        }
        catch (IOException e) {fail("IO Exception should not have been thrown");}
        catch (ServletException e) {fail("ServletException should not have been thrown");}
        //catch (Exception e) {}

    }



}