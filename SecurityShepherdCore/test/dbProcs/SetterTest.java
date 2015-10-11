package dbProcs;

import static org.junit.Assert.*;

import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import utils.Hash;
import utils.ScoreboardStatus;

public class SetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(SetterTest.class);
	private static String propertiesFileDirectory = new String("/site");
	private static String applicationRoot = new String();
	@Before
	public void setUp()
	{
		applicationRoot = System.getProperty("user.dir") + propertiesFileDirectory;
	}
	
	/**
	 * Test to ensure class's can be created with this method. Other Unit Tests use this method, but not nessisarily every time, as a class may already exist.
	 * This Method creates a random class name so it can run every time without failure
	 */
	@Test
	public void testClassCreate() 
	{
		String random = Hash.randomKeyLengthString();
		String className = "newC"+random;
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
	/*
	@Test
	public void testSetCoreDatabaseInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCsrfChallengeFourCsrfToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCsrfChallengeSevenCsrfToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetExposedDatabaseInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModuleCategoryStatusOpen() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModuleStatusClosed() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModuleStatusOpen() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStoredMessage() {
		fail("Not yet implemented");
	}

	@Test
	public void testSuspendUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnSuspendUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateCheatSheet() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateCsrfCounter() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePasswordAdmin() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePlayerClass() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePlayerClassToNull() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePlayerResult() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateUserPoints() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateUserRole() {
		fail("Not yet implemented");
	}

	@Test
	public void testUserCreate() {
		fail("Not yet implemented");
	}
	*/
}
