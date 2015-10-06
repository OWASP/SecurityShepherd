package dbProcs;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

import utils.ScoreboardStatus;

public class GetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GetterTest.class);
	private static String propertiesFileDirectory = new String("/site");
	private static String lang = new String("en_GB");
	private static Locale locale = new Locale(lang);
	private static String applicationRoot = new String();
	
	public GetterTest()
	{
		applicationRoot = System.getProperty("user.dir") + propertiesFileDirectory;
	}
	
	/**
	 * Searches for class based on class name. If nothing is found, the class is created and the new class Id is returned
	 * @param className Name of the class you wish to search / create
	 * @return The Identifier of the class owning the name submitted
	 * @throws Exception If the class cannot be created or found
	 */
	private static String findCreateClassId(String className) throws Exception
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
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	private static boolean verifyTestUser(String applicationRoot, String userName, String password) throws Exception
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
	private static boolean verifyTestUser(String applicationRoot, String userName, String password, String theClass) throws Exception
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
					if(!user[4].equalsIgnoreCase(theClass))
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
	
	/**
	 * This method will sign in as an admin, or create the admin and sign in as them. If this fails it will throw an Exception.
	 * This function will pass if correct user credentials are passed as well
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the admin you want to create or sign in as
	 * @param password The password of the admin you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If admin Create function fails, an exception will be passed up
	 */
	private static boolean verifyTestAdmin(String applicationRoot, String userName, String password) throws Exception
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
						if(numberofChallengesReturned > 58)
						{
							log.debug("PASS: Found " + numberofChallengesReturned + " modules");
							return;
						}
						else
						{
							log.debug("Too Few Challenges Returned to pass: " + numberofChallengesReturned + " returned");
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
			}
			catch(Exception e)
			{
				log.fatal("Could not Find or Create Class : " + e.toString());
				fail("Could not Create or Find Class");
			}
			if(verifyTestUser(applicationRoot, userName, userName, classId) && verifyTestUser(applicationRoot, otherUserName, otherUserName, classId))
			{
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				//Open all Modules First
				if(Setter.openAllModules(applicationRoot))
				{
					//Not Touching User Zero, But dropping five points from other user
					if (Setter.updateUserPoints(applicationRoot, otherUserId, -5))
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
	
	/*
	@Test
	public void testGetLessons() {
		fail("Not yet implemented");
	}
	*/
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

	/*
	@Test
	public void testGetModuleResult() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleResultFromHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModulesInOptionTags() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModulesInOptionTagsCTF() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleSolution() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleStatusMenu() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOpenCloseCategoryMenu() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlayersByClass() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgressJSON() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTournamentModules() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserName() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUserLocked() {
		fail("Not yet implemented");
	}
	*/

}
