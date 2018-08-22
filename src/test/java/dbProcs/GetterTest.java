package dbProcs;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import testUtils.TestProperties;
import utils.InstallationException;
import utils.ScoreboardStatus;

/**
 * Class is targeted to test all of the methods found in the src/dbprocs/Getter.java class, but does include some coverage of other classes, such as Setter.java and Database.java
 * @author mark
 *
 */
public class GetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GetterTest.class);
	private static String lang = new String("en_GB");
	private static Locale locale = new Locale(lang);
	private static String applicationRoot = new String();
	private static final int totalNumberOfModulesInShepherd = 58;
	
	/**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void resetDatabase() 
	{
		TestProperties.setTestPropertiesFileDirectory(log);
		try 
		{
			TestProperties.executeSql(log);
		} 
		catch (InstallationException e) 
		{
			String message = new String("Could not create DB: " + e.toString());
			log.fatal(message);
			fail(message);
		}
	}
	
	/**
	 * Searches for class based on class name. If nothing is found, the class is created and the new class Id is returned
	 * @param className Name of the class you wish to search / create
	 * @return The Identifier of the class owning the name submitted
	 * @throws Exception If the class cannot be created or found
	 */
	public static String findCreateClassId(String className) throws Exception
	{
		String classId = new String();
		ResultSet rs = Getter.getClassInfo(applicationRoot);
		while(rs.next())
		{
			if(rs.getString(2).compareTo(className) == 0)
			{
				classId = rs.getString(1);
				break;
			}
		}
		rs.close();
		if(classId.isEmpty())
		{
			log.debug("Could not find class. Creating it");
			if(Setter.classCreate(applicationRoot, className, "2015"))
			{
				log.debug("Class Created. Getting ID");
				classId = findCreateClassId(className);
			}
			else
			{
				throw new Exception("Could not Create Class " + className);
			}
		}
		return classId;
	}
	
	/**
	 * Searches for class based on class name. If nothing is found, the class is created and the new class Id is returned
	 * @param className Name of the class you wish to search / create
	 * @return The Identifier of the class owning the name submitted
	 * @throws Exception If the class cannot be created or found
	 */
	public static String findCreateClassId(String className, String applicationRoot) throws Exception
	{
		String classId = new String();
		ResultSet rs = Getter.getClassInfo(applicationRoot);
		while(rs.next())
		{
			if(rs.getString(2).compareTo(className) == 0)
			{
				classId = rs.getString(1);
				break;
			}
		}
		rs.close();
		if(classId.isEmpty())
		{
			log.debug("Could not find class. Creating it");
			if(Setter.classCreate(applicationRoot, className, "2015"))
			{
				log.debug("Class Created. Getting ID");
				classId = findCreateClassId(className, applicationRoot);
			}
			else
			{
				throw new Exception("Could not Create Class " + className);
			}
		}
		return classId;
	}
	
	/**
	 * This method will sign in as an admin, or create the admin and sign in as them. If this fails it will throw an Exception.
	 * This function will pass if correct user credentials are passed as well
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the admin you want to create or sign in as
	 * @param password The password of the admin you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If admin Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestAdmin(String applicationRoot, String userName, String password) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "admin", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}
	
	/**
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestUser(String applicationRoot, String userName, String password) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}
	
	/**
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception. 
	 * They will be added to the submitted class
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @param theClass The identifier of the class
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestUser(String applicationRoot, String userName, String password, String theClass) throws Exception
	{
		boolean result = false;
		try
		{
			String className = new String();
			className = Getter.getClassInfo(applicationRoot, theClass)[0];
			if(className.isEmpty())
			{
				log.error("Class does not exist.");
				throw new Exception("Canot not verify test user with Invalid Class");
			}
			else
			{
				String user[] = Getter.authUser(applicationRoot, userName, userName);
				if(user == null || user[0].isEmpty())
				{
					log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
					Setter.userCreate(applicationRoot, theClass, userName, userName, "player", userName+"@test.com", false);
					user = Getter.authUser(applicationRoot, userName, userName);
				}
				if(user != null && !user[0].isEmpty())
				{
					log.debug(userName + " could authenticate. checking class");
					if((user[4] == null || user[4].isEmpty()) && !theClass.isEmpty())
					{
						log.debug("Need to update user's class");
						Setter.updatePlayerClass(applicationRoot, theClass, user[0]);
					} 
					else if(!user[4].equalsIgnoreCase(theClass))
					{
						log.debug("Need to update user's class");
						Setter.updatePlayerClass(applicationRoot, theClass, user[0]);
					}
					else
						log.debug("User in class submitted already");
					result = true;
				}
				else
				{
					log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
					throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}
	
	@Before
	public void setUp()
	{
		TestProperties.setTestPropertiesFileDirectory(log);
	}
	
	@Test
	public void testAuthUserCorrectCredentials() 
	{
		String userName = new String("authWithGoodCreds");
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug("PASS: Successfully signed in as " + userName);
				return;
			}
			else
			{
				fail("Could not Authenticate as " + userName);
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Create user: " + e.toString());
			fail("Could not create user " + userName);
		}
	}
	
	@Test
	public void testAuthUserIncorrectCredentials() 
	{
		String userName = new String("authWithBadCreds");
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug("User " + userName + " exists. Checking if Auth Works with bad pass");
				if(Getter.authUser(applicationRoot, userName, userName+"wrongPassword") == null)
				{
					log.debug("PASS: Could not authenticate with bad pass for user " + userName);
					return;
				}
				else
				{
					fail("Could Authenticate With Bad Pass for User " + userName);
				}
			}
			else
			{
				fail("Couldnt verify " + userName + " could authenticate at all");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Create user: " + e.toString());
			fail("Could not create user " + userName);
		}
	}
	
	@Test
	public void testAuthUserSqlInjection() 
	{
		String userName = new String("authWithSqliCreds");
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug("User " + userName + " exists. Checking if Auth Works with bad pass");
				if(Getter.authUser(applicationRoot, userName, "'or'='1'='1") == null)
				{
					log.debug("PASS: Could not authenticate with SQL Injection for user " + userName);
					return;
				}
				else
				{
					fail("Could Authenticate With SQL Injection for User " + userName);
				}
			}
			else
			{
				fail("Couldnt verify " + userName + " could authenticate at all");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Create user: " + e.toString());
			fail("Could not create user " + userName);
		}
	}
	
	@Test
	public void testAuthUserSqlInjectionUserName() 
	{
		String userName = new String("authWithSqli+BadPassCreds");
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug("User " + userName + " exists. Checking if Auth Works with bad pass");
				if(Getter.authUser(applicationRoot, "'or'='1'='1' -- ", "wrongPassword") == null)
				{
					log.debug("PASS: Could not authenticate with SQL Injection for user " + userName);
					return;
				}
				else
				{
					fail("Could Authenticate With SQL Injection for User Name");
				}
			}
			else
			{
				fail("Couldnt verify " + userName + " could authenticate at all");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Create user: " + e.toString());
			fail("Could not create user " + userName);
		}
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleComplete() 
	{
		String userName = new String("userResultWhenComplete");
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, dataStorageLessonId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, dataStorageLessonId, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, dataStorageLessonId, userId);
							log.debug("checkPlayerResultTest" + checkPlayerResultTest);
							if(checkPlayerResultTest == null)
								return; //Pass
							else
							{
								fail("Function says user has not completed module"); //Even though this test just marked it as Completed
							}
						}
						else
							fail("Could not mark data storage lesson as complete for user");
					}
					else
						fail("Could not Mark Data Storage Lesson as Opened by Default admin");
				}
				else
					fail("Could not Open All Modules");
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}

	@Test
	public void testCheckPlayerResultWhenModuleNotComplete() 
	{
		String userName = new String("userHasModulesOpened");
		String contentProviderLeakage = new String("5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, contentProviderLeakage, Getter.getUserIdFromName(applicationRoot, userName)).isEmpty())
					{
						String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, contentProviderLeakage, Getter.getUserIdFromName(applicationRoot, userName));
						if(checkPlayerResultTest != null)
							return; //Pass
						else
						{
							fail("Function says user has not opened challenge or has completed challenge before");
						}
					}
					else
					{
						fail("Could not Content Provider Leakage Lesson as Opened by user");
					}
				}
				else
				{
					fail("Could not Mark Modules As Opened");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleNotOpened() 
	{
		String userName = new String("noModulesOpened");
		String unOpenedModuleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String test = Getter.checkPlayerResult(applicationRoot, unOpenedModuleId, userName);
				if(test != null)
				{
					log.fatal("result should be null but it was: " + test);
					fail("Function says User has opened module they should not have opened by default"); // User Should not have completed this module by default after running a fresh DB. ensure you have a fresh DB if this fails
				}
				else
				{
					log.debug("PASS: Function says user has not opened module");
					return; //Pass
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleWhenOpened() 
	{
		String userName = new String("userHasModulesOpened");
		String csrfChallengeThree = new String("5ca9115f3279b9b9f3308eb6a59a4fcd374846d6");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, csrfChallengeThree, Getter.getUserIdFromName(applicationRoot, userName)).isEmpty())
					{
						String test = Getter.checkPlayerResult(applicationRoot, csrfChallengeThree, Getter.getUserIdFromName(applicationRoot, userName));
						if(test == null)
						{
							fail("Function says " + userName + " has not opened module"); // User Should have opened and not completed CSRF Three
						}
						else
							return; //Pass
					}
					else
						fail("Could not Mark CSRF 3 as Opened by " + userName);
				}
				else
				{
					fail("Could not Mark Modules As Opened");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testFindPlayerById() 
	{ 
		String userName = new String("UserForPlayerIdSearch");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(Getter.findPlayerById(applicationRoot, userId))
				{
					log.debug("PASS: Found user");
					return;
				}
				else
				{
					fail("Could Not Find Player in Player Search");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	
	@Test
	public void testFindPlayerByIdWithAdminId() 
	{ 
		
		String userName = new String("playerSearchWithAdmin");
		try
		{
			if(verifyTestAdmin(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Getter.findPlayerById(applicationRoot, userId))
				{
					return;
				}
				else
				{
					fail("Found Admin in Player Search");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testFindPlayerByIdWithBadUserId() 
	{
		String userId = new String("DOES NOT EXIST");
		if(!Getter.findPlayerById(applicationRoot, userId))
		{
			return;
		}
		else
		{
			fail("Found Player That Does Not Exist");
		}
	}
	
	@Test
	public void testGetAllModuleInfo() 
	{
		ArrayList<String[]> modules = Getter.getAllModuleInfo(applicationRoot);
		if(modules.size() > 75) //Shepherd v3.0 has 76 Modules. If less than All are Returned, then there is a problem with the Open Modules Function or the Retrieve data function
		{
			log.debug("PASS: Found " + modules.size() + " modules");
			return;
		}
		else
		{
			log.fatal("Too Few Modules Returned to Pass Test: " + modules.size());
			fail("Only " + modules.size() + "/~76 modules returned from function");
		}
	}
	
	@Test
	public void testGetChallenges() 
	{
		String userName = new String("testGetChallengesUser");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the GetAllModuleInfo method will return data
				if(Setter.openAllModules(applicationRoot))
				{
					String modules = Getter.getChallenges(applicationRoot, userId, locale);
					if(!modules.isEmpty()) //Some Modules were included in response
					{
						//Get number of Challenges returned by getChallenges method
						int numberofChallengesReturned = (modules.length() - modules.replace("class='lesson'", "").length()) / "class='lesson'".length();
						if(numberofChallengesReturned > totalNumberOfModulesInShepherd)
						{
							log.debug("PASS: Found " + numberofChallengesReturned + " modules");
							return;
						}
						else
						{
							log.debug("Too Few Challenges Returned to pass: " + numberofChallengesReturned + " returned. Expected at least:" + totalNumberOfModulesInShepherd);
							fail("Too Few Challenges Returned to Pass");
						}
					}
					else
					{
						log.fatal("No Modules Found. Returned empty String");
						fail("No Modules Found");
					}
				}
				else
				{
					fail("Could Not Mark Modules as Open Before Test");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetChallengesWhenModulesClosed() 
	{
		String userName = new String("getChallengesCLosedUser");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the GetAllModuleInfo method will return data
				if(Setter.closeAllModules(applicationRoot))
				{
					String modules = Getter.getChallenges(applicationRoot, userId, locale);
					if(!modules.isEmpty()) //Some Modules were included in response
					{
						//Get number of Challenges returned by getChallenges method
						int numberofChallengesReturned = (modules.length() - modules.replace("class='lesson'", "").length()) / "class='lesson'".length();
						if(!(numberofChallengesReturned > 0))
						{
							log.debug("PASS: Found " + numberofChallengesReturned + " modules");
							return;
						}
						else
						{
							log.debug("Too Many Challenges Returned to pass: " + numberofChallengesReturned + " returned");
							fail("Challenges Returned when all modules were closed");
						}
					}
					else
					{
						log.fatal("No Modules Found. Returned empty String");
						fail("No Modules Found");
					}
				}
				else
				{
					fail("Could Not Mark Modules as Open Before Test");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}

	
	@Test
	public void testGetClassCount() 
	{
		String className = new String("NewClassForGetCount");
		try
		{
			findCreateClassId(className);
		}
		catch(Exception e)
		{
			log.fatal("Could not Find or Create Class : " + e.toString());
			fail("Could not Create/Find Class");
		}
		int classCount = Getter.getClassCount(applicationRoot);
		if(classCount < 1)
		{
			fail("Class Count Too Low to Pass");
		}
		else
		{
			log.debug("PASS: Atleast One Class Returned");
			return;
		}
	}
	
	
	@Test
	public void testGetClassInfoString() {
		try
		{
			findCreateClassId("NewClassForGetInfo"); //Throws Exception if Fails
			ResultSet rs = Getter.getClassInfo(applicationRoot);
			if(rs.next())
			{
				if(!rs.getString(1).isEmpty())
				{
					log.debug("PASS: Class Information was returned");
				}
				else
				{
					fail("Data in Class Info Result Set was Blank");
				}
			}
			else
			{
				fail("No Rows In Class Info Result Set");
			}
			rs.close();
		}
		catch(Exception e)
		{
			log.fatal("ClassInfo Failure: " + e.toString());
			fail("Could not open ClassInfo Result Set");
		}
	}
	
	@Test
	public void testGetClassInfoStringString() 
	{
		String classId = new String();
		String className = new String("NewClassForGetInfo2");
		try
		{
			findCreateClassId(className);
		}
		catch(Exception e)
		{
			log.fatal("Could not Find or Create Class : " + e.toString());
			fail("Could not Create/Find Class");
		}
		try
		{
			ResultSet rs = Getter.getClassInfo(applicationRoot);
			while(rs.next())
			{
				if(rs.getString(2).equalsIgnoreCase("NewClassForGetInfo2"))
				{
					classId = rs.getString(1);
					break;
				}
			}
			rs.close();
			if(classId.isEmpty())
			{
				fail("Could not Find Class ID in Get Info Result");
			}
			else
			{
				String[] classInfo = Getter.getClassInfo(applicationRoot, classId);
				if(classInfo[0].equalsIgnoreCase("NewClassForGetInfo2") && classInfo[1].equalsIgnoreCase("2015"))
				{
					log.debug("PASS: Expected Data Returned from getClassInfo");
				}
				else
				{
					if(!classInfo[0].equalsIgnoreCase("NewClassForGetInfo2"))
					{
						fail("Incorrect Class Name returned from getClassInfo");
					}
					else if(!classInfo[1].equalsIgnoreCase("2015"))
					{
						fail("Incorrect Class Year returned from getClassInfo");
					}
					else
					{
						fail("Unexpected Failure");
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("ClassInfo Failure: " + e.toString());
			fail("Could not open ClassInfo Result Set");
		}
	}
	
	@Test
	public void testGetCsrfForumWithIframe() 
	{
		String classId = new String();
		String moduleId = new String("0a37cb9296ff3763f7f3a45ff313bce47afa9384"); //CSRF Challenge 5
		Locale locale = new Locale("en_GB");
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", locale);
		try
		{
			classId = findCreateClassId("NewClassForCsrfIframeFourm");
			String userName = new String("userforiframeclass");
			if(verifyTestUser(applicationRoot, userName, userName, classId))
			{
				//Open all Modules First so that the Module Can Be Opened by the user
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, moduleId, Getter.getUserIdFromName(applicationRoot, userName)).isEmpty())
					{
						String csrfFourm = Getter.getCsrfForumWithIframe(applicationRoot, classId, moduleId, bundle);
						if(csrfFourm.indexOf(userName) > -1)
						{
							log.debug("PASS: User was found in the fourm");
							return;
						}
						else
						{
							log.error("Could not find user name '" + userName + "' in this: " + csrfFourm);
							fail("User was not contained in the CSRF iFrame Forum");
						}
					}
					else
					{
						fail("Could not open CSRF 5 as Created User");
					}
				}
				else
				{
					fail("Could not Mark All Modules as Open");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("User/Class Error: " + e.toString());
			fail("Could not Create User or Class");
		}
		log.debug("End of CSRF Iframe Forum Test");
	}
	
	
	@Test
	public void testGetCsrfForumWithImg() 
	{
		String classId = new String();
		String moduleId = new String("0a37cb9296ff3763f7f3a45ff313bce47afa9384"); //CSRF Challenge 5
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", locale);
		String className = new String("NewClassForGetInfo");
		try
		{
			classId = findCreateClassId(className);
		}
		catch(Exception e)
		{
			log.fatal("Could not Find or Create Class : " + e.toString());
		}
		if(classId.isEmpty())
		{
			fail("Could not get ClassId");
		}
		else 
		{
			String userName = new String("userforimgclass");
			try
			{
				if(verifyTestUser(applicationRoot, userName, userName, classId))
				{
					//Open all Modules First so that the Module Can Be Opened by the user
					if(Setter.openAllModules(applicationRoot))
					{
						//Simulate user Opening Level
						if(!Getter.getModuleAddress(applicationRoot, moduleId, Getter.getUserIdFromName(applicationRoot, userName)).isEmpty())
						{
							String csrfFourm = Getter.getCsrfForumWithImg(applicationRoot, classId, moduleId, bundle);
							if(csrfFourm.indexOf(userName) > -1)
							{
								log.debug("PASS: User was found in the fourm");
								return;
							}
							else
							{
								log.error("Could not find user name '" + userName + "' in this: " + csrfFourm);
								fail("User was not contained in the CSRF Img Forum");
							}
						}
						else
						{
							fail("Could not open CSRF 5 as Created User");
						}
					}
					else
					{
						fail("Could not Mark All Modules as Open");
					}
				}
				else
				{
					fail("Could not verify user (No Exception Failure)");
				}
			}
			catch(Exception e)
			{
				log.fatal("Could not Verify User: " + e.toString());
				fail("Could not Verify User " + userName);
			}
		}
	}
	
	@Test
	public void testGetFeedback() 
	{
		String userName = new String("userGetFeedback");
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, dataStorageLessonId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String feedbackSearchCode = "RwarUNiqueFeedbackCodeToSEARCHFor1182371723";
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, dataStorageLessonId, userId, feedbackSearchCode, 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, dataStorageLessonId, userId);
							log.debug("checkPlayerResultTest" + checkPlayerResultTest);
							if(checkPlayerResultTest == null)
							{
								log.debug("Checking to see if the feedback is included in the getFeeback response for the module");
								String feedback = Getter.getFeedback(applicationRoot, dataStorageLessonId);
								if(feedback.indexOf(feedbackSearchCode) > -1)
								{
									log.debug("PASS: Detected the user's feedback");
									return;
								}
								else
								{
									log.fatal("User's Feedback '" + feedbackSearchCode + "' was not found in: " + feedback);
									fail("Could not find user's feedback");
								}
							}
							else
							{
								fail("Function says user has not completed module"); //Even though this test just marked it as Completed
							}
						}
						else
							fail("Could not mark data storage lesson as complete for user");
					}
					else
						fail("Could not Mark Data Storage Lesson as Opened by Default admin");
				}
				else
					fail("Could not Open All Modules");
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}

	@Test
	public void testGetIncrementalModulesWithModulesClosed() 
	{
		String userName = new String("testIncModuleMenu2");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Close all Modules First
				if(Setter.closeAllModules(applicationRoot))
				{
					String incrementalModules = Getter.getIncrementalModules(applicationRoot, userId, lang, "testingCSRFtoken");
					if(incrementalModules.indexOf("You've Finished!") > -1) //IF no modules are open, this is the expected leading string
					{
						log.debug("PASS: Menu appears to have compiled correctly");
					}
					else
					{
						log.debug("incrementalModules returned: " + incrementalModules);
						fail("Could not Detect Finished Message");
					}
				}
				else
				{
					fail("Could not Close All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	 
	@Test
	public void testGetIncrementalModulesWithNoneComplete() 
	{
		String userName = new String("testIncModuleMenu1");
		String lowestRankModuleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //This should be changed if an easier module is made
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					String incrementalModules = Getter.getIncrementalModules(applicationRoot, userId, lang, "testingCSRFtoken");
					if(incrementalModules.indexOf("Completed") == -1) //User should not have completed any modules. The Completed Button should not be present
					{
						if(incrementalModules.indexOf(lowestRankModuleId) > -1) //The only module Id to be returned should be this one as it is the first presented (Lowest Incremental Rank)
						{
							if(incrementalModules.indexOf("Get Next Challenge") > -1) //This is the English string that should be included with the lang submitted in this unit test
							{
								log.debug("PASS: Incremental Menu Appears to have Rendered correctly with the Preconditions of this test");
								return;
							}
							else
							{
								fail("Could not Detect i18n English Values in Menu");
							}
						}
						else
						{
							fail("The Module Id Returned was not the Known First Level. Ie not: " + lowestRankModuleId);
						}
					}
					else
					{
						fail("CTF Menu Appears as if User Has Completed Modules When They Have Not");
					}
					//Wont Log unless unit doesnt pass
					log.debug(incrementalModules);
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetIncrementalModulesWithOneModuleComplete() 
	{
		String userName = new String("testIncModuleMenu3");
		String lowestRankModuleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //This should be changed if an easier module is made
		String secondLowestRankModuleId = "b9d82aa7b46ddaddb6acfe470452a8362136a31e"; //This should be changed if an easier module is made or is orded before this
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, lowestRankModuleId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, lowestRankModuleId, userId, "Feedback is Not Enabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, lowestRankModuleId, userId);
							log.debug("checkPlayerResultTest" + checkPlayerResultTest);
							if(checkPlayerResultTest == null)
							{
								String incrementalModules = Getter.getIncrementalModules(applicationRoot, userId, lang, "testingCSRFtoken");
								if(incrementalModules.indexOf("Completed") > -1) //User should  have completed one module. The Completed Button should be present
								{
									if(incrementalModules.indexOf(lowestRankModuleId) > -1) //The only completed module Id to be returned should be this one
									{
										if(incrementalModules.indexOf(secondLowestRankModuleId) > -1)
										{
											if(incrementalModules.indexOf("Get Next Challenge") > -1) //This is the English string that should be included with the lang submitted in this unit test
											{
												log.debug("PASS: Incremental Menu Appears to have Rendered correctly with the Preconditions of this test");
												return;
											}
											else
											{
												fail("Could not Detect i18n English Values in Menu");
											}
										}
										else
										{
											fail("The Module Id Returned to be Completed Next was not the Known 2nd Level. Ie not: " + secondLowestRankModuleId);
										}
									}
									else
									{
										fail("The Module Id Returned was not the Known First Level. Ie not: " + lowestRankModuleId);
									}
								}
								else
								{
									fail("CTF Menu Appears as if User Has Completed Modules When They Have Not");
								}
								//Wont Log unless unit doesnt pass
								log.debug(incrementalModules);
							}
							else
							{
								fail("checkPlayerResultTest says user has not completed module"); //Even though this test just marked it as Completed
							}
						}
						else
							fail("Could not mark data storage lesson as complete for user");
					}
					else
						fail("Could not Lowest Rank Lesson as Opened by User");
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetIncrementalModulesWithoutScriptWithModulesClosed() 
	{
		String userName = new String("testIncModuleMenuScript2");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Close all Modules First
				if(Setter.closeAllModules(applicationRoot))
				{
					String incrementalModules = Getter.getIncrementalModulesWithoutScript(applicationRoot, userId, lang, "testingCSRFtoken");
					if(incrementalModules.indexOf("You've Finished!") > -1) //IF no modules are open, this is the expected leading string
					{
						if(!incrementalModules.endsWith(";</script>"))
						{
							log.debug("PASS: Incremental Menu Appears to have Rendered correctly with the Preconditions of this test without ending in the button script");
							return;
						}
						else
						{
							log.debug("incrementalModules returned: " + incrementalModules);
							fail("Function Ended in Unexpected Script");
						}
					}
					else
					{
						log.debug("incrementalModules returned: " + incrementalModules);
						fail("Could not Detect Finished Message");
					}
				}
				else
				{
					fail("Could not Close All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetIncrementalModulesWithoutScriptWithNoneComplete() 
	{
		String userName = new String("testIncModuleMenuScript1");
		String lowestRankModuleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //This should be changed if an easier module is made
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					String incrementalModules = Getter.getIncrementalModulesWithoutScript(applicationRoot, userId, lang, "testingCSRFtoken");
					if(incrementalModules.indexOf("Completed") == -1) //User should not have completed any modules. The Completed Button should not be present
					{
						if(incrementalModules.indexOf(lowestRankModuleId) > -1) //The only module Id to be returned should be this one as it is the first presented (Lowest Incremental Rank)
						{
							if(incrementalModules.indexOf("Get Next Challenge") > -1) //This is the English string that should be included with the lang submitted in this unit test
							{
								if(!incrementalModules.endsWith(";</script>"))
								{
									log.debug("PASS: Incremental Menu Appears to have Rendered correctly with the Preconditions of this test without ending in the button script");
									return;
								}
								else
								{
									log.debug("incrementalModules returned: " + incrementalModules);
									fail("Function Ended in Unexpected Script");
								}
							}
							else
							{
								log.debug("incrementalModules returned: " + incrementalModules);
								fail("Could not Detect i18n English Values in Menu");
							}
						}
						else
						{
							log.debug("incrementalModules returned: " + incrementalModules);
							fail("The Module Id Returned was not the Known First Level. Ie not: " + lowestRankModuleId);
						}
					}
					else
					{
						log.debug("incrementalModules returned: " + incrementalModules);
						fail("CTF Menu Appears as if User Has Completed Modules When They Have Not");
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetIncrementalModulesWithoutScriptWithOneModuleComplete() 
	{
		String userName = new String("testIncModuleMenuScript3");
		String lowestRankModuleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //This should be changed if an easier module is made
		String secondLowestRankModuleId = "b9d82aa7b46ddaddb6acfe470452a8362136a31e"; //This should be changed if an easier module is made or is orded before this
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, lowestRankModuleId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, lowestRankModuleId, userId, "Feedback is Not Enabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, lowestRankModuleId, userId);
							log.debug("checkPlayerResultTest" + checkPlayerResultTest);
							if(checkPlayerResultTest == null)
							{
								String incrementalModules = Getter.getIncrementalModulesWithoutScript(applicationRoot, userId, lang, "testingCSRFtoken");
								if(incrementalModules.indexOf("Completed") > -1) //User should  have completed one module. The Completed Button should be present
								{
									if(incrementalModules.indexOf(lowestRankModuleId) > -1) //The only completed module Id to be returned should be this one
									{
										if(incrementalModules.indexOf(secondLowestRankModuleId) > -1)
										{
											if(incrementalModules.indexOf("Get Next Challenge") > -1) //This is the English string that should be included with the lang submitted in this unit test
											{
												if(!incrementalModules.endsWith(";</script>"))
												{
													log.debug("PASS: Incremental Menu Appears to have Rendered correctly with the Preconditions of this test without ending in the button script");
													return;
												}
												else
												{
													log.debug("incrementalModules returned: " + incrementalModules);
													fail("Function Ended in Unexpected Script");
												}
											}
											else
											{
												log.debug("incrementalModules returned: " + incrementalModules);
												fail("Could not Detect i18n English Values in Menu");
											}
										}
										else
										{
											log.debug("incrementalModules returned: " + incrementalModules);
											fail("The Module Id Returned to be Completed Next was not the Known 2nd Level. Ie not: " + secondLowestRankModuleId);
										}
									}
									else
									{
										log.debug("incrementalModules returned: " + incrementalModules);
										fail("The Module Id Returned was not the Known First Level. Ie not: " + lowestRankModuleId);
									}
								}
								else
								{
									log.debug("incrementalModules returned: " + incrementalModules);
									fail("CTF Menu Appears as if User Has Completed Modules When They Have Not");
								}
							}
							else
							{
								fail("checkPlayerResultTest says user has not completed module"); //Even though this test just marked it as Completed
							}
						}
						else
							fail("Could not mark data storage lesson as complete for user");
					}
					else
						fail("Could not Lowest Rank Lesson as Opened by User");
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	/**
	 * Tests to ensure that user can only see their data in the scoreboard, and cannot see the data from users in other classes in the scoreboard
	 */
	@Test
	public void testGetJsonScoreClassSpecific() 
	{
		String userName = new String("scoreUserClassSpecific");
		String className = new String("ScoreClassSpec");
		String otherUserName = new String("scoreUserClassSpecific2");
		String otherClassName = new String("ScoreClassSpec2");
		String classId = new String();
		String classId2 = new String();
		String insecureDirectObjectRefLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //Direct Object Reference Module
		try
		{
			try
			{
				classId = findCreateClassId(className);
				classId2 = findCreateClassId(otherClassName);
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Classes");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) && verifyTestUser(applicationRoot, otherUserName, otherUserName, classId2))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, userId, "Feedback is Disabled", 1, 1, 1);
					if(markLevelCompleteTest != null)
						markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, otherUserId, "Feedback is Disabled", 1, 1, 1);
					else 
						fail("Could Not Mark Level as complete by User 1");
					if (markLevelCompleteTest != null)
					{
						boolean pass = false;
						//Configure Score board for class Specific
						ScoreboardStatus.setScoreboardClassSpecific();
						//Get Score board Data
						String scoreboardData = Getter.getJsonScore(applicationRoot, classId);
						//Take the JSON String and make it Java JSON friendly
						JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
						//Loop through array to find Our user
						for(int i = 0; i < scoreboardJson.size(); i++)
						{
							JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
							if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
							{
								pass = true;
								log.debug("Found " + userName + " in scoreboard");
							}
							if(scoreRowJson.get("username").toString().compareTo(otherUserName) == 0)
							{
								log.fatal("Found Class User that shouldn't be included in the output");
								log.debug("Found " + otherUserName + " in: " + scoreboardData);
								fail("Found Class User that shouldn't be included in the Scoreboard Data");
							}
						}
						if(!pass)
						{
							log.error("Could not find " + userName + " in JSON Data: " + scoreboardData);
							fail("Could not find user in scoreboard");
						}
						else
						{
							return; //PASS
						}
					}
					else
					{
						fail("Failed to Mark Direct Object Level as Complete for 2nd User");
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify users (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify Users: " + e.toString());
			fail("Could not Verify Users " + userName);
		}
	}
	
	/**
	 * Test to ensure users that have not scored any points, or are on negative points are not shown in the scoreboard
	 */
	@Test
	public void testGetJsonScoreTotalNoneOrNegPoints() 
	{
		String userName = new String("userZero");
		String className = new String("LowScoreTeam");
		String otherUserName = new String("userMinusFive");
		String classId = new String();
		try
		{
			try
			{
				classId = findCreateClassId(className);
				log.debug("Class Found");
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Class");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) && verifyTestUser(applicationRoot, otherUserName, otherUserName, classId))
			{
				log.debug("User's Verified");
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				log.debug("UserId retrieved");
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					log.debug("Opened All Modules");
					//Not Touching User Zero, But dropping five points from other user
					if (Setter.updateUserPoints(applicationRoot, otherUserId, -5))
					{
						log.debug("Updated Points of user Minus 5");
						//Configure Score board for total open
						ScoreboardStatus.setScoreboeardOpen();
						log.debug("Scoreboard Set to Open");
						//Get Score board Data
						String scoreboardData = Getter.getJsonScore(applicationRoot, classId);
						if(scoreboardData.isEmpty())
						{
							log.debug("PASS: The Scoreboard response was empty. Therefore the users are not valid to be returned");
							return; //PASS
						}
						log.debug("Got Scoreboard Data");
						//Take the JSON String and make it Java JSON friendly
						JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
						log.debug("Parsed Scoreboard Data");
						if(scoreboardJson == null)
							log.debug("scoreboardJson is Null. json was: " + scoreboardData);
						//Loop through array to find Our user
						for(int i = 0; i < scoreboardJson.size(); i++)
						{
							log.debug("Looping through Array " + i);
							JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
							if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
							{
								fail("Found " + userName + " in scoreboard"); 
							}
							if(scoreRowJson.get("username").toString().compareTo(otherUserName) == 0)
							{
								fail("Found " + otherUserName + " in scoreboard"); 
							}
						}
						log.debug("PASS: Did not ether user's in the response, therefore they were not included");
						return; //PASS
					}
					else
					{
						fail("Failed to Subtract points from " + otherUserName);
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify users (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify Users: " + e.toString());
			fail("Could not Verify Users " + userName);
		}
	}
	
	/**
	 * Test to see if Score board returns score for entire user base regardless of class
	 */
	@Test
	public void testGetJsonScoreTotalOpen() 
	{
		String userName = new String("scoreUserTotalScore");
		String className = new String("ScoreTotalScore");
		String otherUserName = new String("scoreUserTotalScoreb2");
		String otherClassName = new String("ScoreTotalScoreb2");
		String classId = new String();
		String classId2 = new String();
		String insecureDirectObjectRefLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //Direct Object Reference Module
		try
		{
			try
			{
				classId = findCreateClassId(className);
				classId2 = findCreateClassId(otherClassName);
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Classes");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) && verifyTestUser(applicationRoot, otherUserName, otherUserName, classId2))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, userId, "Feedback is Disabled", 1, 1, 1);
					if(markLevelCompleteTest != null)
						markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, otherUserId, "Feedback is Disabled", 1, 1, 1);
					else 
						fail("Could Not Mark Level as complete by User 1");
					if (markLevelCompleteTest != null)
					{
						boolean pass = false;
						boolean user2 = false;
						//Configure Score board for class Specific
						ScoreboardStatus.setScoreboeardOpen();
						//Get Score board Data
						String scoreboardData = Getter.getJsonScore(applicationRoot, classId);
						//Take the JSON String and make it Java JSON friendly
						JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
						//Loop through array to find Our user
						for(int i = 0; i < scoreboardJson.size(); i++)
						{
							JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
							if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
							{
								pass = true;
								log.debug("Found " + userName + " in scoreboard");
							}
							if(scoreRowJson.get("username").toString().compareTo(otherUserName) == 0)
							{
								user2 = true;
								log.debug("Found " + otherUserName + " in scoreboard");
							}
						}
						if(!(pass && user2))
						{
							if(!pass)
							{
								log.error("Could not find " + userName + " in JSON Data: " + scoreboardData);
								fail("Could not find user in scoreboard");
							}
							else
							{
								log.error("Could not see users from other class in total scoreboard data");
								log.error("Could not find " + otherUserName + " in " + scoreboardData);
								fail("Could not see users from other class in total scoreboard data");
							}
						}
						else
						{
							return; //PASS
						}
					}
					else
					{
						fail("Failed to Mark Direct Object Level as Complete for 2nd User");
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify users (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify Users: " + e.toString());
			fail("Could not Verify Users " + userName);
		}
	}
	
	/**
	 * Ensuring HTML is encoded from untrusted user inputs in scoreboard
	 */
	@Test
	public void testGetJsonScoreTotalOpenHtmlChars() 
	{
		String userName = new String("<script>alert('Name');</sciprt>");
		String className = new String("Scorl<script>alert(1)</script>");
		String otherUserName = new String("\"onerror=\"alert('Name');//");
		String otherClassName = new String("\"onerror=\"alert('C');//");
		String classId = new String();
		String classId2 = new String();
		String insecureDirectObjectRefLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //Direct Object Reference Module
		try
		{
			try
			{
				classId = findCreateClassId(className);
				classId2 = findCreateClassId(otherClassName);
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Classes");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) && verifyTestUser(applicationRoot, otherUserName, otherUserName, classId2))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, userId, "Feedback is Disabled", 1, 1, 1);
					if(markLevelCompleteTest != null)
						markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, otherUserId, "Feedback is Disabled", 1, 1, 1);
					else 
						fail("Could Not Mark Level as complete by User 1");
					if (markLevelCompleteTest != null)
					{
						//Configure Score board for total open
						ScoreboardStatus.setScoreboeardOpen();
						//Get Score board Data
						String scoreboardData = Getter.getJsonScore(applicationRoot, classId);
						//Take the JSON String and make it Java JSON friendly
						JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
						//Loop through array to find Our user
						for(int i = 0; i < scoreboardJson.size(); i++)
						{
							JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
							if(scoreRowJson.get("username").toString().compareTo(userName) == 0) //Therefore not encoded for HTML
							{
								fail("Found " + userName + " in scoreboard"); 
							}
							if(scoreRowJson.get("username").toString().compareTo(otherUserName) == 0) //Therefore not encoded for HTML
							{
								fail("Found " + otherUserName + " in scoreboard"); 
							}
						}
						log.debug("PASS: Did not find HTML Strings in Scoreboard Response. Therefore they are encoded");
						return; //PASS
					}
					else
					{
						fail("Failed to Mark Direct Object Level as Complete for 2nd User");
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify users (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify Users: " + e.toString());
			fail("Could not Verify Users " + userName);
		}
	}
	
	@Test
	public void testGetLessons() 
	{
		String userName = new String("getLessonsUser");
		String inscureDirectObjectLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11";
		String poorDataValidationLesson = "b9d82aa7b46ddaddb6acfe470452a8362136a31e";
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, inscureDirectObjectLesson, userId).isEmpty())
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, inscureDirectObjectLesson, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String lessonsMenu = Getter.getLessons(applicationRoot, userId, locale);
							if(lessonsMenu.indexOf("class='lesson'") > -1) //Menu Should include this at least once
							{
								if(lessonsMenu.indexOf(inscureDirectObjectLesson) > -1) //This module should be in the response
								{
									if(lessonsMenu.indexOf("<img src='css/images/completed.png'/><a class='lesson' id='"+inscureDirectObjectLesson) > -1) //This module should be returned as completed
									{
										if(lessonsMenu.indexOf("<img src='css/images/uncompleted.png'/><a class='lesson' id='"+poorDataValidationLesson) > -1)
										{
											if(lessonsMenu.indexOf("Insecure Direct Object References") > -1) //English string should exist in output based on the submitted locale
											{
												log.debug("PASS: GetLessons Menu Appears to have Rendered correctly with the Preconditions of this test");
												return;
											}
											else
											{
												log.fatal("Could not find i18n English String in lessons Menu: " + lessonsMenu);
												fail("Could not Detect i18n Locale Strings In Lessons Menu");
											}
										}
										else
										{
											log.fatal("Could not detect Uncompleted Icon beside Poor Data Validation Lesson: " + lessonsMenu);
											fail("Uncompleted Module did not have Uncopmleted Symbol");
										}
									}
									else
									{
										log.fatal("Could not detect completed Icon beside Insecure Direct Object Reference Lesson");
										log.error("Could not find : <img src='css/images/completed.png'/><a class='lesson' id='"+inscureDirectObjectLesson + " in " + lessonsMenu);
										fail("Completed Module Did not Have Completed Symbol");
									}
								}
								else
								{
									log.fatal("Could not find Insecure Direct Object References ModuleID in: " + lessonsMenu);
									fail("Could not find Insecure Direct Object References ModuleID in Response");
								}
							}
							else
							{
								log.fatal("Could not find lesson list items in repsonse" + lessonsMenu);
								fail("Could not find Lesson List Items in Response");
							}
						}
						else
							fail("Could not mark module as complete");
					}
					else
					{
						fail("Could not simulate opening module");
					}
				}
				else
				{
					fail("Could not open All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	/**
	 * Test to see if correct meny is returned from getLessons when modules are closed
	 */
	@Test
	public void testGetLessonsWhenClosed() 
	{
		String userName = new String("getLessonsClosedUser");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First
				if(Setter.closeAllModules(applicationRoot))
				{
					String lessonsMenu = Getter.getLessons(applicationRoot, userId, locale);
					if(lessonsMenu.indexOf("class='lesson'") == -1) //Menu Should not include this when modules closed
					{
						if(lessonsMenu.indexOf("No lessons found") > -1) //English string should exist in output based on the submitted locale
						{
							log.debug("PASS: GetLessons Menu Appears to have Rendered correctly with the Preconditions of this test");
							return;
						}
						else
						{
							log.fatal("Could not find i18n English String in lessons Menu: " + lessonsMenu);
							fail("Could not Detect i18n Locale Strings In Lessons Menu");
						}
					}
				}
				else
				{
					fail("Could not close All Modules");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetModuleAddress() 
	{
		String userName = new String("userGetModuleAddress");
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, insecureCryptoLesson, userId).isEmpty())
					{
						log.debug("PASS: Could mark level open when level was marked as open");
						return;
					}
					else
						fail("Could not Insecure Crypto Lesson as Opened by user");
				}
				else
					fail("Could not Open All Modules");
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetModuleAddressWhenClosed() 
	{
		String userName = new String("userGetModuleAddressTwo");
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Close all modules first
				if(Setter.closeAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, insecureCryptoLesson, userId).isEmpty())
					{
						log.debug("PASS: Could not get Module URL when Module Closed");
					}
					else
						fail("Could Get Module Address when marked as closed");
				}
				else
					fail("Could not Close All Modules");
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetModuleCategory() 
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		if(Getter.getModuleCategory(applicationRoot, insecureCryptoLesson).compareTo("Insecure Cryptographic Storage") != 0)
		{
			fail("Incorrect Category Returned for Insecure Crypto Lesson");
		}
		else
		{
			log.debug("PASS: Expected Category Returned");
		}
	}

	@Test
	public void testGetModuleHash() 
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		if(Getter.getModuleHash(applicationRoot, insecureCryptoLesson).compareTo("if38ebb58ea2d245fa792709370c00ca655fded295c90ef36f3a6c5146c29ef2") != 0)
		{
			fail("Incorrect Hash Returned for Insecure Crypto Lesson");
		}
		else
		{
			log.debug("PASS: Expected Hash Returned");
		}
	}
	
	@Test
	public void testGetModuleIdFromHash() 
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		if(Getter.getModuleIdFromHash(applicationRoot, Getter.getModuleHash(applicationRoot, insecureCryptoLesson)).compareTo(insecureCryptoLesson) != 0)
		{
			fail("Incorrect moduleId Returned for Insecure Crypto Lesson Hash Search");
		}
		else
		{
			log.debug("PASS: Expected Id Returned");
		}
	}
	
	@Test
	public void testGetModuleKeyTypeEncryptedKey() 
	{
		String csrfChallengeThree = new String("5ca9115f3279b9b9f3308eb6a59a4fcd374846d6");
		if(!Getter.getModuleKeyType(applicationRoot, csrfChallengeThree))
		{
			log.debug("PASS: Encrypted Key Detected on Encrypted Level");
		}
		else
		{
			log.fatal("Hardcoded Key Detected On Encrypted Key Module");
			fail("Hardcoded Key Detected On Encrypted Key Module");
		}
	}
	
	@Test
	public void testGetModuleKeyTypeHardcodedKey() 
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		if(Getter.getModuleKeyType(applicationRoot, insecureCryptoLesson))
		{
			log.debug("PASS: Hardcoded Key Detected on Hardcoded Level");
		}
		else
		{
			log.fatal("Encrypted Key Detected On Hardcoded Key Module");
			fail("Encrypted Key Detected On Hardcoded Key Module");
		}
	}
	
	@Test
	public void testGetModuleNameLocaleKey()
	{
		try
		{
			String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
			String moduleName = new String("Insecure Direct Object References");
			String moduleLocalNameKey = Getter.getModuleNameLocaleKey(applicationRoot, moduleId);
			ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);
			String localName = bundle.getString(moduleLocalNameKey);
			if(localName.compareTo(moduleName) != 0)
			{
				log.error(localName + " != " + moduleName);
				fail("Name Retrieved != expected result");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testGetModuleNameLocaleKey: " + e.toString());
			fail("Could not complete testGetModuleNameLocaleKey");
		}
	}
	
	/**
	 * Test to return stored result key from DB via getModuleResult Function
	 */
	@Test
	public void testGetModuleResult()
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		String knownStoredResult = new String("base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou");
		String methodReturnResult = Getter.getModuleResult(applicationRoot, insecureCryptoLesson);
		if(knownStoredResult.compareTo(methodReturnResult) != 0)
		{
			log.fatal("Known Result (" + knownStoredResult + ") did not match returned result (" + methodReturnResult + ")");
			fail("Stored and Known Results Differed");
		}
	}
	
	/**
	 * Test to return stored result key from DB via getModuleResultFromHash Function
	 */
	@Test
	public void testGetModuleResultFromHash() 
	{
		String insecureCryptoLessonHash = new String("if38ebb58ea2d245fa792709370c00ca655fded295c90ef36f3a6c5146c29ef2");
		String knownStoredResult = new String("base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou");
		String methodReturnResult = Getter.getModuleResultFromHash(applicationRoot, insecureCryptoLessonHash);
		if(knownStoredResult.compareTo(methodReturnResult) != 0)
		{
			log.fatal("Known Result (" + knownStoredResult + ") did not match returned result (" + methodReturnResult + ")");
			fail("Stored and Known Results Differed");
		}
	}

	/**
	 * Function should return the entire list of modules regardless of status in options tags
	 */
	@Test
	public void testGetModulesInOptionTags() 
	{
		String insecureCryptoLesson = new String("201ae6f8c55ba3f3b5881806387fbf34b15c30c2");
		String modules = Getter.getModulesInOptionTags(applicationRoot);
		if(modules.indexOf(insecureCryptoLesson) == -1)
		{
			log.fatal("Insecure Crypto Lesson ID Ommited from list: " + modules);
			fail("Entire List of Modules not returned");
		}
		else if(modules.indexOf("option") == -1)
		{
			log.fatal("No Options Tags Detected in List: " + modules);
			fail("No Options Tags Detected in List");
		}
	}
	
	@Test
	public void testGetModulesInOptionTagsCTF() 
	{
		String lowestRankLevel = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		String modules = Getter.getModulesInOptionTagsCTF(applicationRoot);
		if(modules.indexOf(lowestRankLevel) == -1)
		{
			log.fatal("Insecure Crypto Lesson ID Ommited from list: " + modules);
			fail("Entire List of Modules not returned");
		}
		else if(modules.indexOf("option") == -1)
		{
			log.fatal("No Options Tags Detected in List: " + modules);
			fail("No Options Tags Detected in List");
		}
		else if(!modules.startsWith("<option value='" + lowestRankLevel))
		{
			log.fatal("Wrong Module Listed First. Should be module with lowest incremental Rank: " + modules);
			fail("First option tag was not the lowest ranking level");
		}
	}

	@Test
	public void testGetModuleSolution() 
	{
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		String[] moduleSolution = Getter.getModuleSolution(applicationRoot, insecureDirectObjectReferenceLesson, locale);
		if(moduleSolution == null)
		{
			fail("Could not retrieve module solution");
		}
		else if(moduleSolution[1].indexOf("Stop the request") == -1)
		{
			log.fatal("Could not find 'Stop the request' in the following solution: " + moduleSolution[1]);
			fail("Could not find english string in solution");
		}
	}
	
	/**
	 * Test to see if the module status menu is correct when all modules are open
	 */
	@Test
	public void testGetModuleStatusMenu() 
	{
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		if(Setter.openAllModules(applicationRoot))
		{
			String moduleStatusMenu = Getter.getModuleStatusMenu(applicationRoot);
			if(moduleStatusMenu.indexOf("<tr><th>To Open</th><th>To Close</th></tr><tr>") == -1)
			{
				log.fatal("No Menu Header in ModuleStatusMenu: " + moduleStatusMenu);
				fail("No Menu Header in moduleStatusMenu");
			}
			else if(moduleStatusMenu.indexOf("id='toOpen'") == -1)
			{
				log.fatal("No Open Menu Detected in Output: " + moduleStatusMenu);
				fail("No Open Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toClose'") == -1)
			{
				log.fatal("No Close Menu Detected in Output: " + moduleStatusMenu);
				fail("No Close Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toOpen'></select></td>") < 0) //Should be empty as all modules should be open
			{
				log.fatal("Modules are in the 'toOpen' list when all modules should already be open: " + moduleStatusMenu);
				fail("Modules are in the 'toOpen' list when all modules should already be open");
			}
			else
			{
				//Make Sub String for the toClose List
				int endOfToCloseMenu = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>")).indexOf("</select>")+(moduleStatusMenu.length() - moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>")).length());
				String toCloseList = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>"), endOfToCloseMenu);
				log.debug("Close List: " + toCloseList);
				if(toCloseList.indexOf(insecureDirectObjectReferenceLesson) == -1)
				{
					log.fatal("Could not Find Insecure Direct Object Reference in toClose List");
					fail("Could not Find Insecure Direct Object Reference in toClose List");
				}
				else if (toCloseList.indexOf(dataStorageLessonId) == -1)
				{
					log.fatal("Could not Find Insecure Data Storage in toClose List");
					fail("Could not Find Insecure Data Storage in toClose List");
				}
				else
				{
					return; //PASS
				}
			}
		}
		else
		{
			fail("Could not open all modules");
		}
	}
	
	/**
	 * Test to see if the module status menu is correct when all modules are open
	 */
	@Test
	public void testGetModuleStatusMenuWhenClosed() 
	{
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		if(Setter.closeAllModules(applicationRoot))
		{
			String moduleStatusMenu = Getter.getModuleStatusMenu(applicationRoot);
			if(moduleStatusMenu.indexOf("<tr><th>To Open</th><th>To Close</th></tr><tr>") == -1)
			{
				log.fatal("No Menu Header in ModuleStatusMenu: " + moduleStatusMenu);
				fail("No Menu Header in moduleStatusMenu");
			}
			else if(moduleStatusMenu.indexOf("id='toOpen'") == -1)
			{
				log.fatal("No Open Menu Detected in Output: " + moduleStatusMenu);
				fail("No Open Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toClose'") == -1)
			{
				log.fatal("No Close Menu Detected in Output: " + moduleStatusMenu);
				fail("No Close Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toClose'></select></td>") < 0) //Should be empty as all modules should be closed
			{
				log.fatal("Modules are in the 'toClose' list when all modules should already be closed: " + moduleStatusMenu);
				fail("Modules are in the 'toClose' list when all modules should already be closed");
			}
			else
			{
				//Make Sub String for the toOpen List
				int endOfToOpenMenu = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>")).indexOf("</select>")+(moduleStatusMenu.length() - moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>")).length());
				String toOpenList = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>"), endOfToOpenMenu);
				log.debug("Open List: " + toOpenList);
				if(toOpenList.indexOf(insecureDirectObjectReferenceLesson) == -1)
				{
					log.fatal("Could not Find Insecure Direct Object Reference in toOpen List");
					fail("Could not Find Insecure Direct Object Reference in toOpen List");
				}
				else if (toOpenList.indexOf(dataStorageLessonId) == -1)
				{
					log.fatal("Found Insecure Data Storage in toOpen List when it should already be open");
					fail("Found Insecure Data Storage in toOpen List when it should already be open");
				}
				else
				{
					return; //PASS
				}
			}
		}
		else
		{
			fail("Could not close all modules");
		}
	}
	
	/**
	 * Test to see if the module status menu is correct when all modules are open
	 */
	@Test
	public void testGetModuleStatusMenuWhenMobileOnlyOpen() 
	{
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		if(Setter.openOnlyMobileCategories(applicationRoot))
		{
			String moduleStatusMenu = Getter.getModuleStatusMenu(applicationRoot);
			if(moduleStatusMenu.indexOf("<tr><th>To Open</th><th>To Close</th></tr><tr>") == -1)
			{
				log.fatal("No Menu Header in ModuleStatusMenu: " + moduleStatusMenu);
				fail("No Menu Header in moduleStatusMenu");
			}
			else if(moduleStatusMenu.indexOf("id='toOpen'") == -1)
			{
				log.fatal("No Open Menu Detected in Output: " + moduleStatusMenu);
				fail("No Open Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toClose'") == -1)
			{
				log.fatal("No Close Menu Detected in Output: " + moduleStatusMenu);
				fail("No Close Meny Detected in Output");
			}
			else if(moduleStatusMenu.indexOf("id='toClose'></select></td>") > 0) //Should not be empty as Web Levels should be closed
			{
				log.fatal("Modules are in the 'toClose' list when web modules should already be closed: " + moduleStatusMenu);
				fail("Modules are in the 'toClose' list when web modules should already be closed");
			}
			else if(moduleStatusMenu.indexOf("id='toOpen'></select></td>") > 0) //Should not be empty as Mobile Levels should be open
			{
				log.fatal("Modules are in the 'toOpen' list when mobile modules should already be closed: " + moduleStatusMenu);
				fail("Modules are in the 'toOpen' list when mobile modules should already be closed");
			}
			else
			{
				//Make Sub String for the toOpen List
				int endOfToOpenMenu = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>")).indexOf("</select>")+(moduleStatusMenu.length() - moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>")).length());
				String toOpenList = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toOpen'>"), endOfToOpenMenu);
				//Make Sub String for the toClose List
				int endOfToCloseMenu = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>")).indexOf("</select>")+(moduleStatusMenu.length() - moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>")).length());
				String toCloseList = moduleStatusMenu.substring(moduleStatusMenu.indexOf("id='toClose'>"), endOfToCloseMenu);
				log.debug("Open List: " + toOpenList);
				log.debug("Close List: " + toCloseList);
				if(toCloseList.indexOf(dataStorageLessonId) == -1)
				{
					log.fatal("Could not Find Insecure Data Storage in toOpen List");
					fail("Could not Find Insecure Data Storage in toOpen List");
				}
				else if (toOpenList.indexOf(insecureDirectObjectReferenceLesson) == -1)
				{
					log.fatal("Could not Find Insecure Direct Object Reference in toOpen List");
					fail("Could not Find Insecure Direct Object Reference in toOpen List");
				}
				else
				{
					//Verify the correct number of modules are open/closed (At this point the Menu is fine. This is really now testing the mobile/web setter filters)
					int numberOfMobileLevelsOpen = (toCloseList.length() - toCloseList.replace("<option", "").length()) / "<option".length();
					log.debug(numberOfMobileLevelsOpen + " mobile levels detected");
					int numberOfWebLevelsClosed =(toOpenList.length() - toOpenList.replace("<option", "").length()) / "<option".length();
					log.debug(numberOfWebLevelsClosed + " web levels detected");
					int mobileDbModuleCount = 0;
					int webDbModuleCount = 0;
					Connection conn = Database.getCoreConnection(applicationRoot);
					try
					{
						log.debug("Getting Number of Mobile Levels From DB");
						PreparedStatement prepStatement = conn.prepareStatement("SELECT COUNT(*) FROM modules WHERE " + Setter.mobileModuleCategoryHardcodedWhereClause);
						log.debug("Executing Query");
						ResultSet resultSet = prepStatement.executeQuery();
						resultSet.next();
						mobileDbModuleCount = resultSet.getInt(1);
						resultSet.close();
						log.debug("Getting Number of Web Levels from DB");
						prepStatement = conn.prepareStatement("SELECT COUNT(*) FROM modules WHERE " + Setter.webModuleCategoryHardcodedWhereClause);
						resultSet = prepStatement.executeQuery();
						resultSet.next();
						webDbModuleCount = resultSet.getInt(1);
						resultSet.close();
					}
					catch(SQLException e)
					{
						log.debug("Could not query DB Failure: " + e.toString());
						fail("Failed to Query DB For Module Count");
					}
					Database.closeConnection(conn);
					if(mobileDbModuleCount != numberOfMobileLevelsOpen)
					{
						fail("There are " + numberOfMobileLevelsOpen + " mobile levels in open list, but there are " + mobileDbModuleCount + " in the DB");
						//This may mean that the where clause in the setter method is not correct and needs to be updated
					}
					else if(webDbModuleCount != numberOfWebLevelsClosed)
					{
						fail("There are " + numberOfWebLevelsClosed + " web levels in open list, but there are " + webDbModuleCount + " in the DB");
						//This may mean that the where clause in the setter method is not correct and needs to be updated
					}
					else if((mobileDbModuleCount+webDbModuleCount) != (numberOfMobileLevelsOpen+numberOfWebLevelsClosed))
					{
						fail("There are module categories missing from the Setter hardcoded module filters.");
					}
					else
					{
						return; //Pass
					}
				}
			}
		}
		else
		{
			fail("Could not close all modules");
		}
	}
	
	@Test
	public void testGetOpenCloseCategoryMenu() 
	{
		String moduleCategory = "Injection"; //This will need to be updated to a locale key when this method is enhansed to support localisation
		String categoryMenu = Getter.getOpenCloseCategoryMenu(applicationRoot);
		if(categoryMenu.indexOf("option") == -1)
		{
			fail("Category Menu does not have any option tags");
		}
		else if(categoryMenu.indexOf(moduleCategory) == -1)
		{
			fail("Category Menu does not have the " + categoryMenu + " category");
		}
	}
	
	@Test
	public void testGetPlayersByClass() 
	{
		try
		{
			String classId = findCreateClassId("playersByClass");
			String userName = new String("playersByClass");
			for(int i = 0; i <= 9; i++)
			{
				if(verifyTestUser(applicationRoot, userName+i, userName+i, classId))
				{
					log.debug("Created User " + userName+i);
				}
				else
				{
					fail("Could not create user " + userName+i);
				}
			}
			ResultSet playersByClass = Getter.getPlayersByClass(applicationRoot, classId);
			try
			{
				int i = 0;
				while(playersByClass.next())
				{
					i++; //Count the players returned
					if(!playersByClass.getString(2).startsWith(userName))
					{
						log.fatal("Found Unexpected User: " + playersByClass.getString(2));
						fail("Incorrect User from Different Class Returned");
					}
				}
				if(i != 9)
				{
					if(i < 9)
						fail("Too Few Users Returned");
					else if (i > 9)
						fail("Too Many Users Returned");
					else
					{
						log.fatal("Then surely the number WAS 9? How did this happen");
						fail("Incorrect Amount of Users Returned");
					}
				}
			}
			catch(Exception e)
			{
				log.fatal("Failed to itterate through playersByClass: " + e.toString());
				fail("Players By Class Result Set Issue");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not create Class/Users: " + e.toString());
			fail("Could not create Class/Users");
		}
	}
	
	@Test
	public void testGetProgress() 
	{
		String userName = new String("progressUser1");
		String className = new String("progressClass1");
		String otherUserName = new String("progressUser2");
		String otherClassName = new String("progressClass2");
		String anotherUserName = new String("progressClass3");
		String classId = new String();
		String classId2 = new String();
		String insecureDirectObjectRefLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //Direct Object Reference Module
		try
		{
			try
			{
				classId = findCreateClassId(className);
				classId2 = findCreateClassId(otherClassName);
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Classes");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) &&
				verifyTestUser(applicationRoot, anotherUserName, anotherUserName, classId) &&
				verifyTestUser(applicationRoot, otherUserName, otherUserName, classId2))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, insecureDirectObjectRefLesson, userId).isEmpty())
					{
						fail("Could not Simulate Opening Level for User 1");
					} 
					else if(Getter.getModuleAddress(applicationRoot, insecureDirectObjectRefLesson, otherUserId).isEmpty())
					{
						fail("Could not Simulate Opening Level for User 1");
					}
					else
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, userId, "Feedback is Disabled", 1, 1, 1);
						if(markLevelCompleteTest != null)
							markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, otherUserId, "Feedback is Disabled", 1, 1, 1);
						else 
							fail("Could Not Mark Level as complete by User 1");
						if (markLevelCompleteTest != null)
						{
							String classProgress = Getter.getProgress(applicationRoot, classId);
							if(classProgress.indexOf(otherClassName) > 0)
							{
								fail("User from wrong class is listed in getProgress response");
							}
							else if(classProgress.indexOf(userName) == -1)
							{
								fail("Could not find user from class in getProgress response");
							} 
							else if(classProgress.indexOf(anotherUserName) == -1)
							{
								fail("Could not find user who has made no progress in getProgress response");
							}
							else 
							{
								String userRowStart = new String(userName + "</td><td><div style='background-color: #A878EF; heigth: 25px; width: ");
								int startOfProgressWidth = classProgress.indexOf(userRowStart)+userRowStart.length();
								String firstCharacterOfLength = classProgress.substring(startOfProgressWidth, startOfProgressWidth+1);
								int lengthOfProgress = Integer.parseInt(firstCharacterOfLength); //We dont care what number it is. As Long as it's not 0
								if(lengthOfProgress == 0)
								{
									log.debug("Found int value: " + lengthOfProgress);
									log.debug("Was working with this string: " + firstCharacterOfLength);
									fail("Detected 0 Length for User when they have competed a level");
								}
								else
								{
									return; //PASS
								}
							}
						}
						else
						{
							fail("Could not Mark level as Complete by user 2");
						}
					}
				}
				else
				{
					fail("Could not Mark All Modules as Open");
				}
			}
			else
			{
				fail("Could not Verify Users");
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not complete getProgress use case: " + e.toString());
			fail("Could not Complete getProgress use case");
		}
	}
	
	@Test
	public void testGetProgressJSON() 
	{
		String userName = new String("jsonProgress1");
		String className = new String("jsonProgressC");
		String otherUserName = new String("jsonProgress2");
		String otherClassName = new String("jsonProgressC2");
		String anotherUserName = new String("jsonProgress3");
		String classId = new String();
		String classId2 = new String();
		String insecureDirectObjectRefLesson = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; //Direct Object Reference Module
		try
		{
			try
			{
				classId = findCreateClassId(className);
				classId2 = findCreateClassId(otherClassName);
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Classes");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) &&
				verifyTestUser(applicationRoot, anotherUserName, anotherUserName, classId) &&
				verifyTestUser(applicationRoot, otherUserName, otherUserName, classId2))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, insecureDirectObjectRefLesson, userId).isEmpty())
					{
						fail("Could not Simulate Opening Level for User 1");
					} 
					else if(Getter.getModuleAddress(applicationRoot, insecureDirectObjectRefLesson, otherUserId).isEmpty())
					{
						fail("Could not Simulate Opening Level for User 1");
					}
					else
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, userId, "Feedback is Disabled", 1, 1, 1);
						if(markLevelCompleteTest != null)
							markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, insecureDirectObjectRefLesson, otherUserId, "Feedback is Disabled", 1, 1, 1);
						else 
							fail("Could Not Mark Level as complete by User 1");
						if (markLevelCompleteTest != null)
						{
							String jsonProgressString = Getter.getProgressJSON(applicationRoot, classId);
							if(jsonProgressString.indexOf(otherClassName) > 0)
							{
								fail("User from wrong class is listed in getJsonProgress response");
							}
							else if(jsonProgressString.indexOf(userName) == -1)
							{
								fail("Could not find user from class in getJsonProgress response");
							} 
							else if(jsonProgressString.indexOf(anotherUserName) == -1)
							{
								fail("Could not find user who has made no progress in getJsonProgress response");
							}
							else 
							{
								log.debug("Going through JsonArray");
								//Take the JSON String and make it Java JSON friendly
								JSONArray jsonProgress = (JSONArray)JSONValue.parse(jsonProgressString);
								//Loop through array to find Our user
								for(int i = 0; i < jsonProgress.size(); i++)
								{
									JSONObject userProgress = (JSONObject)jsonProgress.get(i);
									if(userProgress.get("userName").toString().compareTo(userName) == 0)
									{
										int progressBar = Integer.parseInt(userProgress.get("progressBar").toString());
										if(progressBar <= 0)
										{
											fail("User has no progress according to response when they have completed a level");
										}
									}
									else if(userProgress.get("userName").toString().compareTo(anotherUserName) == 0)
									{
										int progressBar = Integer.parseInt(userProgress.get("progressBar").toString());
										if(progressBar != 0)
										{
											fail("User that has done nothing has progress != 0");
										}
									}
								}
							}
						}
						else
						{
							fail("Could not Mark level as Complete by user 2");
						}
					}
				}
				else
				{
					fail("Could not Mark All Modules as Open");
				}
			}
			else
			{
				fail("Could not Verify Users");
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not complete getJsonProgress use case: " + e.toString());
			fail("Could not Complete getJsonProgress use case");
		}
	}
	
	/**
	 * Tests the Tournament Floor Plan when all modules are opened
	 */
	@Test
	public void testGetTournamentModules() 
	{
		String userName = new String("allOpenTournUser");
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the GetAllModuleInfo method will return data
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, dataStorageLessonId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, dataStorageLessonId, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String tournamentModules = Getter.getTournamentModules(applicationRoot, userId, locale);
							if(!tournamentModules.isEmpty()) //Some Modules were included in response
							{
								//Get number of Challenges returned by getChallenges method
								int numberofChallengesReturned = (tournamentModules.length() - tournamentModules.replace("class='lesson'", "").length()) / "class='lesson'".length();
								if(numberofChallengesReturned > totalNumberOfModulesInShepherd)
								{
									log.debug("Found " + numberofChallengesReturned + " modules");
									if(!tournamentModules.contains("Corporal")) //English String Expected to be in the response when submitted with the locale for this unit test
									{
										fail("Could not detect i18n English String in Tournament Output");
									}
									else if(tournamentModules.indexOf("<img src='css/images/completed.png'/><a class='lesson' id='"+dataStorageLessonId) == -1)
									{
										fail("Data Storage Lesson was not marked as complete in Tournament Menu");
									}
									else if(tournamentModules.indexOf("<img src='css/images/uncompleted.png'/><a class='lesson' id='"+insecureDirectObjectReferenceLesson) == -1)
									{
										fail("Could not Detect Direct Object Ref Lesson Uncomplete Image");
									}
								}
								else
								{
									log.debug("Too Few Challenges Returned to pass: " + numberofChallengesReturned + " returned");
									fail("Too Few Challenges Returned to Pass");
								}
							}
							else
							{
								log.fatal("No Modules Returned. Empty String");
								fail("No Modules Returned");
							}
						}
						else
						{
							fail("Could not mark Module as Complete");
						}
					}
					else
					{
						fail("Could not simulate user opening module");
					}
				}
				else
				{
					fail("Could Not Mark Modules as Open Before Test");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	/**
	 * Tests the Tournament Floor Plan when modules are closed
	 */
	@Test
	public void testGetTournamentModulesClosed() 
	{
		String userName = new String("closedTournUser");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the GetAllModuleInfo method will return data
				if(Setter.closeAllModules(applicationRoot))
				{
					String tournamentModules = Getter.getTournamentModules(applicationRoot, userId, locale);
					if(!tournamentModules.contains("No Modules Found"))
					{
						fail("Could not detect 'No Modules Found' i18n String");
					}
				}
				else
				{
					fail("Could Not Mark Modules as Closed Before Test");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	/**
	 * Tests the Tournament Floor Plan when only one module is opened
	 */
	@Test
	public void testGetTournamentModulesOnlyOneOpen() 
	{
		String userName = new String("allOpenTournUserNone");
		String sessionManagement8 = new String("7153290d128cfdef5f40742dbaeb129a36ac2340");
		String insecureDirectObjectReferenceLesson = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11");
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the GetAllModuleInfo method will return data
				if(Setter.closeAllModules(applicationRoot))
				{
					if(!Setter.setModuleStatusOpen(applicationRoot, sessionManagement8))
					{
						fail("Could not open Session management Challenge 8");
					}
					else
					{
						String tournamentModules = Getter.getTournamentModules(applicationRoot, userId, locale);
						if(!tournamentModules.isEmpty()) //Some Modules were included in response
						{
							//Get number of Challenges returned by getChallenges method
							int numberofChallengesReturned = (tournamentModules.length() - tournamentModules.replace("class='lesson'", "").length()) / "class='lesson'".length();
							if(numberofChallengesReturned == 1)
							{
								log.debug("Found " + numberofChallengesReturned + " module");
								if(!tournamentModules.contains("Admiral")) //English String Expected to be in the response when submitted with the locale for this unit test
								{
									fail("Could not detect i18n English String Admiral in Tournament Output");
								}
								else if(tournamentModules.indexOf("<img src='css/images/uncompleted.png'/><a class='lesson' id='"+sessionManagement8) == -1)
								{
									fail("Could not Detect Session Management Challenge 8 Uncomplete Image");
								}
								else if(tournamentModules.contains(insecureDirectObjectReferenceLesson))
								{
									fail("Detected closed module returned in Tournament Response");
								}
								else if(tournamentModules.contains("Private"))
								{
									fail("Detected Private Header Even with no levels from that Band Open");
								}
							}
							else
							{
								log.debug("More than one module returned: " + tournamentModules);
								fail("More than one module returned in Single Tournament Test");
							}
						}
						else
						{
							log.fatal("No Modules Returned. Empty String");
							fail("No Modules Returned in Tournament Mode");
						}
					}
				}
				else
				{
					fail("Could Not Mark Modules as Closed Before Test");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testGetUserName()
	{
		String userName = new String("getUserNameUser");
		try 
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String testUserName = Getter.getUserName(applicationRoot, userId);
				if(testUserName.compareTo(userName) != 0)
				{
					fail("Known user name and Retrieved user names are different");
				}
			}
		} 
		catch (Exception e) 
		{
			log.fatal("Could not Verify Test User: " + e.toString());
			fail("Could not Create/Verify Test User");
		}
		
	}
	
	@Test
	public void testIsCsrfLevelCompleteIncrementedCounter() 
	{
		String userName = new String("csrfCounterIncremented");
		String csrfChallengeOne = new String("20e755179a5840be5503d42bb3711716235005ea"); //CSRF Challenge 1 (Should have CSRF Counter of 0 for new user)
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, csrfChallengeOne, userId).isEmpty())
					{
						//Increment Challenge CSRF Counter
						if(Setter.updateCsrfCounter(applicationRoot, csrfChallengeOne, userId))
						{
							if(Getter.isCsrfLevelComplete(applicationRoot, csrfChallengeOne, userId))
							{
								return; //Pass, because CSRF level is completed after the user CSRF counter was incremented
							}
							else
							{
								fail("CSRF 1 not completed after successful increment");
							}
						}
						else
						{
							fail("Could not Increment user Counter for CSRF 1");
						}
					}
					else
					{
						fail("Could not Mark CSRF 1 as opened by user");
					}
				}
				else
				{
					fail("Could not Mark Modules as Opened");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testIsCsrfLevelCompleteWithoutIncrementedCounter() 
	{
		String userName = new String("csrfCounterWithoutInc");
		String csrfChallengeTwo = new String("94cd2de560d89ef59fc450ecc647ff4d4a55c15d"); //CSRF Challenge 2 (Should have CSRF Counter of 0 for new user
		try
		{
			if(verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, csrfChallengeTwo, userId).isEmpty())
					{
						if(!Getter.isCsrfLevelComplete(applicationRoot, csrfChallengeTwo, userId))
						{
							return; //Pass, because CSRF level is not completed because the CSRF Counter for the user is 0
						}
						else
						{
							fail("CSRF 2 marked completed without increment"); // CSRF 2 Challenge should have a counter of 0 and should not return true. 
						}
					}
					else
					{
						fail("Could not Mark CSRF 2 as opened by user");
					}
				}
				else
				{
					fail("Could not mark All Modules as Opened");
				}
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
	}
	
	@Test
	public void testIsModuleOpen() 
	{
		String csrfChallengeTwo = new String("94cd2de560d89ef59fc450ecc647ff4d4a55c15d"); //CSRF Challenge 2 (Should have CSRF Counter of 0 for new user
		//Open all Modules First so that the Module Can Be Opened
		if(Setter.openAllModules(applicationRoot))
		{
			if(!Getter.isModuleOpen(applicationRoot, csrfChallengeTwo))
				fail("isModuleOpen returned False when the module should have been open");
			Setter.closeAllModules(applicationRoot);
			if(Getter.isModuleOpen(applicationRoot, csrfChallengeTwo))
				fail("isModuleOpen returned True when the module should have been closed");
		}
		else
		{
			fail("Could not mark All Modules as Opened");
		}
	}
}
