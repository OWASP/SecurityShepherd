package dbProcs;

import static org.junit.Assert.*;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.BeforeClass;
import org.junit.Test;

import testUtils.TestProperties;
import utils.InstallationException;
import utils.ScoreboardStatus;

public class SetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(SetterTest.class);
	private static String applicationRoot = new String();

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
	 * Test to ensure class's can be created with this method. Other Unit Tests use this method, but not nessisarily every time, as a class may already exist.
	 * This Method creates a random class name so it can run every time without failure
	 */
	@Test
	public void testClassCreate() 
	{
		Random rand = new Random();
		String className = "newC"+rand.nextInt(50)+rand.nextInt(50)+rand.nextInt(50);
		if(!Setter.classCreate(applicationRoot, className, "2015"))
		{
			fail("Could not Create Class");
		}
		else
		{
			try
			{
				boolean pass = false;
				ResultSet rs = Getter.getClassInfo(applicationRoot);
				while(rs.next())
				{
					if(rs.getString(2).equalsIgnoreCase(className))
					{
						pass = true;
						break;
					}
				}
				if(!pass)
				{
					fail("Could not find class in DB");
				}
				else
					return; // PASS
			}
			catch(Exception e)
			{
				log.fatal("Could not Find Created Class: " + e.toString());
				fail("Could not Find Created Class");
			}
		}
	}

	@Test
	public void testIncrementBadSubmission() 
	{
		String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; //CSRF7
		String userName = new String("BadSubUser");
		try
		{
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.openAllModules(applicationRoot))
				{
					fail("Could not mark all modules as open");
				}
				else
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty())
					{
						fail("Could not Simulate Opening First Level for User");
					} 
					else
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							//Giving the User a Score Bump in case they have already completed CSRF7 and this is the 20th time the unit test has run
							if(!Setter.updateUserPoints(applicationRoot, userId, 20))
								fail("Could not give user extra points");
							
							int scoreBefore = 0;
							ScoreboardStatus.setScoreboeardOpen();
							String scoreboardData = Getter.getJsonScore(applicationRoot, "");
							if(scoreboardData.isEmpty())
							{
								fail("Could not detect user in scoreboard before bad submission test");
							}
							else
							{
								JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
								//Loop through array to find Our user
								for(int i = 0; i < scoreboardJson.size(); i++)
								{
									log.debug("Looping through Array " + i);
									JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
									if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
									{
										log.debug("Found user with score: " + scoreRowJson.get("score"));
										scoreBefore = Integer.parseInt(scoreRowJson.get("score").toString());
										break;
									}
								}
								if(scoreBefore == 0)
								{
									log.fatal("Could not find user " + userName + " with score > 0: " + scoreboardData);
									fail("User has score of 0 before BadSubmission Emulation");
								}
								
								//Resetting resetBadSubmission count back to 0
								if(!Setter.resetBadSubmission(applicationRoot, userId))
									fail("Could not Reset bad submission count");
								//Simulating 41 bad submissions
								for(int i = 0; i <= 40; i++)
									Setter.incrementBadSubmission(applicationRoot, userId);
								
								//Check Score again
								int scoreAfter = 0;
								scoreboardData = Getter.getJsonScore(applicationRoot, "");
								scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
								//Loop through array to find Our user
								for(int i = 0; i < scoreboardJson.size(); i++)
								{
									log.debug("Looping through Array " + i);
									JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
									if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
									{
										log.debug("Found user with score: " + scoreRowJson.get("score"));
										scoreAfter = Integer.parseInt(scoreRowJson.get("score").toString());
										break;
									}
								}
								
								int expectedAfter = scoreBefore-(scoreBefore/10);
								log.debug("expected score: " + expectedAfter);
								if(scoreAfter != expectedAfter)//Checking exact number should be equal to and number below as well incase rounded d
								{
									log.debug("score before: " + scoreBefore);
									log.debug("score after : " + scoreAfter);
									log.debug("Expected After: " + expectedAfter);
									int roundedUp = scoreAfter+1; 
									if(roundedUp != expectedAfter)
										fail("Invalid Score Deduction Detected");
									else
										return; // PASS
								}
								else
								{
									return; //Pass
								}
							}
						}
						else
						{
							fail("Could not Mark First level as complete");
						}
					}
				}
			}
			else
			{
				fail("Could not Create/Verify User");
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not complete badSubmission Test: " + e.toString());
			fail("Could not complete badSubmission Test");
		}
	}
	
	@Test
	public void testOpenOnlyMobileCategories() 
	{
		if(!Setter.openOnlyMobileCategories(applicationRoot))
			fail("Could not Open Only Mobile Categories");
	}
	
	@Test
	public void testOpenOnlyWebCategories() 
	{
		if(!Setter.openOnlyWebCategories(applicationRoot))
			fail("Could not Open Only Web Categories");
	}
	

	@Test
	public void testResetBadSubmission() 
	{
		String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; //CSRF7
		String userName = new String("BadSubResetUser");
		try
		{
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.openAllModules(applicationRoot))
				{
					fail("Could not mark all modules as open");
				}
				else
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty())
					{
						fail("Could not Simulate Opening First Level for User");
					} 
					else
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{					
							int scoreBefore = 0;
							ScoreboardStatus.setScoreboeardOpen();
							String scoreboardData = Getter.getJsonScore(applicationRoot, "");
							if(scoreboardData.isEmpty())
							{
								fail("Could not detect user in scoreboard before bad submission test");
							}
							else
							{
								JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
								//Loop through array to find Our user
								for(int i = 0; i < scoreboardJson.size(); i++)
								{
									JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
									if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
									{
										log.debug("Found user with score: " + scoreRowJson.get("score"));
										scoreBefore = Integer.parseInt(scoreRowJson.get("score").toString());
										break;
									}
								}
								if(scoreBefore == 0)
								{
									log.fatal("Could not find user " + userName + " with score > 0: " + scoreboardData);
									fail("User has score of 0 before BadSubmission Emulation");
								}
								
								//Resetting resetBadSubmission count back to 0
								if(!Setter.resetBadSubmission(applicationRoot, userId))
									fail("Could not Reset bad submission count");
								//Simulating 40 bad submissions
								for(int i = 0; i < 40; i++)
								{
									if(!Setter.incrementBadSubmission(applicationRoot, userId))
										fail("Could not Increment Bad Submission Counter");
								}
								//Resetting Bad Submission Count back to 0 again
								if(!Setter.resetBadSubmission(applicationRoot, userId))
									fail("Could not Reset bad submission count");
								//Incrementing one more time (Should set user bad submission counter to 1)
								if(!Setter.incrementBadSubmission(applicationRoot, userId))
									fail("Could not Increment Bad Submission Counter");
								
								//Check Score again
								int scoreAfter = 0;
								scoreboardData = Getter.getJsonScore(applicationRoot, "");
								scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
								//Loop through array to find Our user
								for(int i = 0; i < scoreboardJson.size(); i++)
								{
									log.debug("Looping through Array " + i);
									JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
									if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
									{
										log.debug("Found user with score: " + scoreRowJson.get("score"));
										scoreAfter = Integer.parseInt(scoreRowJson.get("score").toString());
										break;
									}
								}
								
								if(scoreAfter != scoreBefore)//Checking exact number should be equal to and number below as well incase rounded d
								{
									log.debug("score before: " + scoreBefore);
									log.debug("score after : " + scoreAfter);
									fail("Invalid Score Deduction Detected");
								}
								else
								{
									return; //Pass
								}
							}
						}
						else
						{
							fail("Could not Mark First level as complete");
						}
					}
				}
			}
			else
			{
				fail("Could not Create/Verify User");
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not complete badSubmission Test: " + e.toString());
			fail("Could not complete badSubmission Test");
		}
	}

	@Test
	public void testSetCsrfChallengeFourCsrfToken() 
	{
		String userName = new String("csrfFourUser");
		try
		{
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String csrfTokenValue = new String("CsrfTokenTest");
				String csrfToken = Setter.setCsrfChallengeFourCsrfToken(userId, csrfTokenValue, applicationRoot);
				if(csrfToken.compareTo(csrfTokenValue) != 0)
					fail("Retrieved CSRF token did not Match the Set Value");
			}
			else
			{
				fail("Could not Verify User");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete setCsrfChallengeFourCsrfToken test: " + e.toString());
			fail("Could not complete setCsrfChallengeFourCsrfToken test");
		}
	}
	
	@Test
	public void testSetCsrfChallengeSevenCsrfToken() 
	{
		String userName = new String("csrfSevenUser");
		try
		{
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String csrfToken = new String("CsrfTokenTest");
				if(!Setter.setCsrfChallengeSevenCsrfToken(userId, csrfToken, applicationRoot))
					fail("Could not Set CSRF Chalenge 7 Token");
			}
			else
			{
				fail("Could not Verify User");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete setCsrfChallengeSevenCsrfToken test: " + e.toString());
			fail("Could not complete setCsrfChallengeSevenCsrfToken test");
		}
	}
	
	@Test
	public void testSetModuleCategoryStatusOpen()
	{
		String moduleCategory = new String("Injection");
		if(!Setter.closeAllModules(applicationRoot))
			fail("Could not Mark all modules as closed");
		else if (!Setter.setModuleCategoryStatusOpen(applicationRoot, moduleCategory, "open"))
			fail("Could not Open module Category");
		else
		{
			Connection conn = Database.getCoreConnection(applicationRoot);
			try
			{
				log.debug("Getting Number of Mobile Levels From DB");
				PreparedStatement prepStatement = conn.prepareStatement("SELECT DISTINCT moduleCategory FROM modules WHERE moduleStatus = 'open';");
				ResultSet rs = prepStatement.executeQuery();
				while(rs.next())
				{
					if(rs.getString(1).compareTo(moduleCategory) != 0)
					{
						log.debug("Found Category that wa snot injection: " + rs.getString(1));
						fail("Detected Category that was not Injection Open");
					}
				}
			}
			catch(SQLException e)
			{
				log.fatal("Could not Query DB: " + e.toString());
				fail("Could not Query DB for Module Status");
			}
		}
	}
	
	@Test
	public void testSetModuleCategoryStatusClosed()
	{
		String moduleCategory = new String("Injection");
		if(!Setter.openAllModules(applicationRoot))
			fail("Could not Mark all modules as open");
		else if (!Setter.setModuleCategoryStatusOpen(applicationRoot, moduleCategory, "closed"))
			fail("Could not close module Category");
		else
		{
			Connection conn = Database.getCoreConnection(applicationRoot);
			try
			{
				log.debug("Getting Number of Mobile Levels From DB");
				PreparedStatement prepStatement = conn.prepareStatement("SELECT DISTINCT moduleCategory FROM modules WHERE moduleStatus = 'closed';");
				ResultSet rs = prepStatement.executeQuery();
				while(rs.next())
				{
					if(rs.getString(1).compareTo(moduleCategory) != 0)
					{
						log.debug("Found Category that wa snot injection: " + rs.getString(1));
						fail("Detected Category that was not Injection Closed");
					}
				}
			}
			catch(SQLException e)
			{
				log.fatal("Could not Query DB: " + e.toString());
				fail("Could not Query DB for Module Status");
			}
		}
	}
	
	@Test
	public void testSetModuleStatusClosed()
	{
		String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); //CSRF 7
		if(!Setter.openAllModules(applicationRoot))
			fail("Could not Mark all modules as open");
		else if (!Setter.setModuleStatusClosed(applicationRoot, moduleId))
			fail("Could not close CSRF 7 Module");
		else
		{
			Connection conn = Database.getCoreConnection(applicationRoot);
			try
			{
				log.debug("Getting Number of Mobile Levels From DB");
				PreparedStatement prepStatement = conn.prepareStatement("SELECT moduleStatus FROM modules WHERE moduleId = ?");
				prepStatement.setString(1, moduleId);
				ResultSet rs = prepStatement.executeQuery();
				if(rs.next())
				{
					if(rs.getString(1).compareTo("closed") != 0)
					{
						log.debug("Module was not closed by method");
						fail("Module was not closed by method");
					}
				}
			}
			catch(SQLException e)
			{
				log.fatal("Could not Query DB: " + e.toString());
				fail("Could not Query DB for Module Status");
			}
		}
	}
	
	@Test
	public void testSetModuleStatusOpen()
	{
		String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); //CSRF 7
		if(!Setter.closeAllModules(applicationRoot))
			fail("Could not Mark all modules as closed");
		else if (!Setter.setModuleStatusOpen(applicationRoot, moduleId))
			fail("Could not close CSRF 7 Module");
		else
		{
			Connection conn = Database.getCoreConnection(applicationRoot);
			try
			{
				log.debug("Getting Number of Mobile Levels From DB");
				PreparedStatement prepStatement = conn.prepareStatement("SELECT moduleStatus FROM modules WHERE moduleId = ?");
				prepStatement.setString(1, moduleId);
				ResultSet rs = prepStatement.executeQuery();
				if(rs.next())
				{
					if(rs.getString(1).compareTo("open") != 0)
					{
						log.debug("Module was not opened by method");
						fail("Module was not opened by method");
					}
				}
			}
			catch(SQLException e)
			{
				log.fatal("Could not Query DB: " + e.toString());
				fail("Could not Query DB for Module Status");
			}
		}
	}
	
	@Test
	public void testSetStoredMessage() 
	{
		log.debug("Testing Set Stored message");
		String userName = new String("storedMessageUser");
		String className = new String("sMessageClass");
		String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); //CSRF 7
		String message = new String("TestStoredMessage");
		try
		{
			log.debug("Getting class id");
			String classId = GetterTest.findCreateClassId(className, applicationRoot);
			log.debug("Checking User Name in DB");
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName, classId))
			{
				//Open all Modules First so that the Module Can Be Opened
				if(!Setter.openAllModules(applicationRoot))
				{
					fail("Could not open all modules");
				}
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Simulate user Opening Level
				if(Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty())
				{
					fail("Could not Simulate Opening First Level for User");
				} 
				else
				{
					Setter.setStoredMessage(applicationRoot, message, userId, moduleId);
					Connection conn = Database.getCoreConnection(applicationRoot);
					try
					{
						CallableStatement callstmt = conn.prepareCall("call resultMessageByClass(?, ?)");
						log.debug("Gathering resultMessageByClass ResultSet");
						callstmt.setString(1, classId);
						callstmt.setString(2, moduleId);
						ResultSet resultSet = callstmt.executeQuery();
						log.debug("resultMessageByClass executed");
						while(resultSet.next())
						{
							if(resultSet.getString(1).compareTo(userName) == 0)
							{
								if(resultSet.getString(2).compareTo(message) != 0)
									fail("Stored Message does not equal the one set");
								else
									return; //Pass
							}
						}
						fail("Could not find user stored message");
					}
					catch(SQLException e)
					{
						log.fatal("Could not Query DB: " + e.toString());
						fail("Could not Query DB For Stored Message");
					}
				}
			}
			else
			{
				fail("Could not verify test User");
			}
		}
		catch(Exception e)
		{
			String error = "Could not complete testSetStoredMessage";
			log.fatal(error + ": " + e.toString());
			fail(error);
		}
	}

	@Test
	public void testSuspendUser() 
	{
		String userName = new String("suspendedUser");
		try
		{
			log.debug("Checking User Name in DB");
			boolean loggedIn = false;
			try
			{
				log.debug("Trying to Verify User");
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, userName);
			}
			catch(Exception e)
			{
				log.debug("Could not verify. May be suspended. Unsuspending");
				//Might need to unsuspend player
				Setter.unSuspendUser(applicationRoot, Getter.getUserIdFromName(applicationRoot, userName));
				//Gotta Sleep for a sec otherwise the time setting for suspension will fail test. Must be 1 sec after unsuspend function ran
				Thread.sleep(1000);
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, userName);
			}
			if(!loggedIn)
			{
				fail("Could not Verify User");
			}
			else
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.suspendUser(applicationRoot, userId, 10))
				{
					fail("Could not suspend User");
				}
				else
				{
					String user[] = Getter.authUser(applicationRoot, userName, userName);
					if(user == null || user[0].isEmpty())
					{
						return;// PASS: User Could not Authenticate after suspension
					}
					else
					{
						fail("Could still authenticate as user after suspension");
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testSuspendUser: " + e.toString());
			fail("Could not complete testSuspendUser");
		}
	}
	
	@Test
	public void testUnSuspendUser() 
	{
		String userName = new String("UnsuspendedUser");
		try
		{
			log.debug("Checking User Name in DB");
			if(!GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				fail("Could not Verify User");
			}
			else
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.suspendUser(applicationRoot, userId, 10))
				{
					fail("Could not suspend User");
				}
				else
				{
					if(!Setter.unSuspendUser(applicationRoot, userId))
					{
						fail("Could not unsusepend user");
					}
					else
					{
						//Gotta Sleep for a sec, otherwise the time compair will round down and user auth will fail. User is unsuspended 1 second after unsuspend funciton
						Thread.sleep(1000);
						String user[] = Getter.authUser(applicationRoot, userName, userName);
						if(user == null || user[0].isEmpty())
						{
							fail("Could not Authenticate after unsuspension");
						}
						else
						{
							return;// PASS: User Could not Authenticate after unsuspension
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testUnSuspendUser: " + e.toString());
			fail("Could not complete testUnSuspendUser");
		}
	}
	
	@Test
	public void testUpdatePassword() 
	{
		log.debug("Testing update Password");
		String userName = new String("updatePassword");
		String currentPass = new String();
		String newPass = new String();
		boolean loggedIn = false;
		try
		{
			try
			{
				currentPass = userName;
				newPass = userName+userName;
				log.debug("Logging in with default Pass");
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
			}
			catch(Exception e)
			{
				newPass = userName;
				currentPass = userName+userName;
				log.debug("Could not log in with default pass: " + e.toString());
				log.debug("Logging in with alternative pass: " + currentPass);
				String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
				loggedIn = auth != null;
			}
			if(!loggedIn)
			{
				log.debug("Could not sign in with any pass.");
				fail("Could not Verify User");
			}
			else
			{
				log.debug("Logged in! Updating Password now");
				if(!Setter.updatePassword(applicationRoot, userName, currentPass, newPass))
				{
					log.debug("Could not update password");
					fail("Could not update password");
				}
				else
				{
					log.debug("Password Updated. Authenticating with new pass: " + newPass);
					String[] auth = Getter.authUser(applicationRoot, userName, newPass);
					if(auth == null)
					{
						fail("Could Not Auth With New Pass");
					}
					else
					{
						return; //PASS: Authenticated With New Pass
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testUpdatePassword: " + e.toString());
			fail("Could not complete testUpdatePassword");
		}
	}

	@Test
	public void testUpdatePasswordAdmin() 
	{
		log.debug("Testing update Password");
		String userName = new String("adminPassUp");
		String currentPass = new String();
		String newPass = new String();
		boolean loggedIn = false;
		try
		{
			try
			{
				currentPass = userName;
				newPass = userName+userName;
				log.debug("Logging in with default Pass");
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
			}
			catch(Exception e)
			{
				newPass = userName;
				currentPass = userName+userName;
				log.debug("Could not log in with default pass: " + e.toString());
				log.debug("Logging in with alternative pass: " + currentPass);
				String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
				loggedIn = auth != null;
			}
			if(!loggedIn)
			{
				log.debug("Could not sign in with any pass.");
				fail("Could not Verify User");
			}
			else
			{
				log.debug("Logged in! Updating Password now");
				if(!Setter.updatePasswordAdmin(applicationRoot, Getter.getUserIdFromName(applicationRoot, userName), newPass))
				{
					log.debug("Could not update password");
					fail("Could not update password");
				}
				else
				{
					log.debug("Password Updated. Authenticating with new pass: " + newPass);
					String[] auth = Getter.authUser(applicationRoot, userName, newPass);
					if(auth == null)
					{
						fail("Could Not Auth With New Pass");
					}
					else
					{
						return; //PASS: Authenticated With New Pass
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testUpdatePasswordAdmin: " + e.toString());
			fail("Could not complete testUpdatePasswordAdmin");
		}
	}

	@Test
	public void testUpdatePlayerClass() 
	{
		String userName = new String("UpdateClassUser");
		String className = new String("Old Class");
		String otherClassName = new String("Other Class");
		String classId = new String();
		String otherClassId = new String();
		String newClass = new String();
		try
		{
			log.debug("Getting class ids");
			classId = GetterTest.findCreateClassId(className, applicationRoot);
			otherClassId = GetterTest.findCreateClassId(otherClassName, applicationRoot);
			log.debug("Verifying User");
			if(!GetterTest.verifyTestUser(applicationRoot, userName, userName, classId))
			{
				fail("Could not verify user");
			}
			else
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String currentClass = Getter.getUserClassFromName(applicationRoot, userName);
				newClass = otherClassId;

				log.debug("Current Class: " + currentClass);
				log.debug("New Class: " + newClass);
				if(!Setter.updatePlayerClass(applicationRoot, newClass, userId).equalsIgnoreCase(userName))
				{
					fail("Could not update player class");
				}
				else
				{
					String latestClass = Getter.getUserClassFromName(applicationRoot, userName);
					if(latestClass.compareTo(newClass) != 0)
					{
						log.debug("Latest Class: " + latestClass);
						log.debug("New Class: " + newClass);
						fail("Retrieved Class is not the Set Class");
					}
					else
					{
						return; // PASS
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testUpdatePlayerClass: " + e.toString());
			fail("Could not complete testUpdatePlayerClass");
		}
	}

	@Test
	public void testUpdatePlayerClassToNull()
	{
		String userName = new String("UpdateClassUserFromNull");
		String className = new String("WutClass");
		String classId = new String();
		try
		{
			log.debug("Getting class ids");
			classId = GetterTest.findCreateClassId(className, applicationRoot);
			if(!GetterTest.verifyTestUser(applicationRoot, userName, userName, classId))
			{
				fail("Could not verify user");
			}
			else
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String currentClass = Getter.getUserClassFromName(applicationRoot, userName);
				log.debug("Current Class: " + currentClass);
				if(!Setter.updatePlayerClassToNull(applicationRoot, userId).equalsIgnoreCase(userName))
				{
					fail("Could not update player class to null");
				}
				else
				{
					String latestClass = Getter.getUserClassFromName(applicationRoot, userName);
					if(latestClass == null || latestClass.isEmpty())
					{
						return;// PASS
					}
					else
					{
						log.debug("Latest Class: " + latestClass);
						fail("Retrieved Class is not null");
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not complete testUpdatePlayerClass: " + e.toString());
			fail("Could not complete testUpdatePlayerClass");
		}
	}

	@Test
	public void testUpdateUserRole() 
	{
		String userName = new String("WasUserNowAdmin");
		String currentRole = new String();
		String newRole = new String();
		try
		{
			if(GetterTest.verifyTestUser(applicationRoot, userName, userName))
			{
				Connection conn = Database.getCoreConnection(applicationRoot);
				PreparedStatement ps = conn.prepareStatement("SELECT userRole FROM users WHERE userName = ?");
				ps.setString(1, userName);
				ResultSet rs = ps.executeQuery();
				if(rs.next())
				{
					currentRole = rs.getString(1);
					if(currentRole.equalsIgnoreCase("admin"))
					{
						log.debug("User is currently an admin. Changing to player");
						newRole = new String("player");
					}
					else
					{
						log.debug("User is currently a player. Changing to admin");
						newRole = new String("admin");
					}
				}
				else
				{
					fail("User not found in DB after it was created");
				}
				rs.close();
				conn.close();
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.updateUserRole(applicationRoot, userId, newRole).equalsIgnoreCase(userName))
				{
					fail("Could not update user role from " + currentRole + " to " + newRole);
				}
				else
				{
					log.debug("Checking if change occurred");
					conn = Database.getCoreConnection(applicationRoot);
					ps = conn.prepareStatement("SELECT userRole FROM users WHERE userName = ?");
					ps.setString(1, userName);
					rs = ps.executeQuery();
					if(rs.next())
					{
						if(!newRole.equalsIgnoreCase(rs.getString(1)))
						{
							fail("User Role was not updated in DB");
						}
						else
						{
							//Pass
						}
					}
					else
					{
						fail("Could not find user after creating and updating");
					}
					rs.close();
					conn.close();
				}
			}
			else
			{
				fail("Could not Create User");
			}
			
		}
		catch(SQLException e)
		{
			log.fatal("DB Error: " + e.toString());
			fail("Could not Complete testUpdateUserRole because DB Error");
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Complete testUpdateUserRole");
		}
	}
	
	@Test
	public void testMutipleClassMedals() 
	{
		String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; //CSRF7
		String userName = new String("classUserOne");
		String otherUserName = new String("difClassUserTwo");
		try
		{
			String classOne = TestProperties.findCreateClassId(log, "classA2737", applicationRoot);
			String classTwo = TestProperties.findCreateClassId(log, "classB2737", applicationRoot);
			log.debug("classOne: " + classOne);
			log.debug("classTwo: " + classTwo);
			if(TestProperties.verifyTestUser(log, applicationRoot, userName, userName, classOne) && TestProperties.verifyTestUser(log, applicationRoot, otherUserName, otherUserName, classTwo))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);
				if(!Setter.openAllModules(applicationRoot))
				{
					fail("Could not mark all modules as open");
				}
				else
				{
					//Simulate user Opening Level
					if(Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty() || Getter.getModuleAddress(applicationRoot, moduleId, otherUserId).isEmpty())
					{
						fail("Could not Simulate Opening Level for Users");
					} 
					else
					{
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
						if (markLevelCompleteTest != null)
						{					
							String markLevelCompleteTestOtherUser = Setter.updatePlayerResult(applicationRoot, moduleId, otherUserId, "Feedback is Disabled", 1, 1, 1);
							//Do both Users have a gold medal?
							if (markLevelCompleteTestOtherUser != null)
							{					
								ScoreboardStatus.setScoreboeardOpen();
								String scoreboardData = Getter.getJsonScore(applicationRoot, "");
								if(scoreboardData.isEmpty())
								{
									fail("Could not detect user in scoreboard before bad submission test");
								}
								else
								{
									JSONArray scoreboardJson = (JSONArray)JSONValue.parse(scoreboardData);
									//Loop through array to find Our first user
									boolean goldMedal = false;
									for(int i = 0; i < scoreboardJson.size(); i++)
									{
										//log.debug("Looping through Array " + i);
										JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
										if(scoreRowJson.get("username").toString().compareTo(userName) == 0)
										{
											log.debug("Found user with goldMedalCount: " + scoreRowJson.get("goldMedalCount"));
											goldMedal = Integer.parseInt(scoreRowJson.get("goldMedalCount").toString()) > 0;
											break;
										}
									}
									if(!goldMedal)
									{
										String message = userName + " should have a gold medal and does not. They were first in their class to complete challenge " + moduleId;
										log.fatal(message);
										fail(message);
									}
									else
									{
										//Search for the other user
										goldMedal = false;
										for(int i = 0; i < scoreboardJson.size(); i++)
										{
											//log.debug("Looping through Array " + i);
											JSONObject scoreRowJson = (JSONObject)scoreboardJson.get(i);
											if(scoreRowJson.get("username").toString().compareTo(otherUserName) == 0)
											{
												log.debug("Found user with goldMedalCount: " + scoreRowJson.get("goldMedalCount"));
												goldMedal = Integer.parseInt(scoreRowJson.get("goldMedalCount").toString()) > 0;
												break;
											}
										}
										if(!goldMedal)
										{
											String message = otherUserName + " should have a gold medal and does not. They were first in their class to complete challenge " + moduleId;
											log.fatal(message);
											fail(message);
										}
									}
								}
							}
							else
							{
								fail("Could not Mark First level as complete for Second User");
							}
						}
						else
						{
							fail("Could not Mark First level as complete");
						}
					}
				}
			}
			else
			{
				fail("Could not Create/Verify User");
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not complete badSubmission Test: " + e.toString());
			fail("Could not complete badSubmission Test");
		}
	}

	
	@Test
	public void testUserDelete()
	{
		String testUsername = "testuserdelete";
		String testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
		
		try {
			if(testuserId == null || testuserId.isEmpty())
				assert(Setter.userCreate(applicationRoot, null, testUsername, testUsername, "player", testUsername+"@test.com", false));
			
			testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
			assert(testuserId != null && !testuserId.isEmpty());
			
			assert(Setter.userDelete(applicationRoot, testuserId));
			
			testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
			assert(testuserId == null || testuserId.isEmpty());
			
		} catch (SQLException sqlEx) {
			log.fatal("DB Error: " + sqlEx.toString());
			fail("Could not Complete testUserDelete because DB Error");
		}
	}
}
