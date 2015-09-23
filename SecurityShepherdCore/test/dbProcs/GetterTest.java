package dbProcs;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

public class GetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GetterTest.class);
	private static String propertiesFileDirectory = new String("/site");
	private static String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
	@Test
	public void testAuthUserCorrectCredentials() {
		if(Getter.authUser(System.getProperty("user.dir")+propertiesFileDirectory, "admin", "password") == null)
			fail("Could not Authenticate Default admin");
		else
			return; //Pass
	}
	
	@Test
	public void testAuthUserIncorrectCredentials() {
		if(Getter.authUser(System.getProperty("user.dir")+propertiesFileDirectory, "admin", "wrongPassword") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with incorrect Password");
	}
	
	@Test
	public void testAuthUserSqlInjection() {
		if(Getter.authUser(System.getProperty("user.dir")+propertiesFileDirectory, "admin", "wrongPassword'or'1'='1") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with SQL Injection");
	}
	
	@Test
	public void testAuthUserSqlInjectionUserName() {
		if(Getter.authUser(System.getProperty("user.dir")+propertiesFileDirectory, "admin'or'1'='1", "wrongPassword") == null)
			return; //Pass
		else
			fail("Could authenticate as Default admin with SQL Injection (UserName)");
	}

	@Test
	public void testCheckPlayerResultWhenModuleNotOpened() {
		String test = Getter.checkPlayerResult(System.getProperty("user.dir")+propertiesFileDirectory, moduleId, Getter.getUserIdFromName(System.getProperty("user.dir")+propertiesFileDirectory, "admin"));
		if(test != null)
		{
			log.fatal("result should be null but it was: " + test);
			fail("Function says Admin has opened module they should not have opened by default"); // Admin Should not have completed this module by default after running a fresh DB. ensure you have a fresh DB if this fails
		}
		else
			return; //Pass
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleNotComplete() {
		String contentProviderLeakage = new String("5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a");
		String applicationRoot = System.getProperty("user.dir") + propertiesFileDirectory;
		String userId = Getter.getUserIdFromName(applicationRoot, "admin");
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, contentProviderLeakage, userId).isEmpty())
		{
				String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, contentProviderLeakage, userId);
				if(checkPlayerResultTest != null)
					return; //Pass
				else
				{
					fail("Function says Admin has not opened challenge or has completed challenge before");
				}
		}
		else
			fail("Could not Mark Data Storage Lesson as Opened by Default admin");
	}
	
	@Test
	public void testCheckPlayerResultWhenModuleComplete() {
		String dataStorageLessonId = new String("53a53a66cb3bf3e4c665c442425ca90e29536edd");
		String applicationRoot = System.getProperty("user.dir") + propertiesFileDirectory;
		String userId = Getter.getUserIdFromName(applicationRoot, "admin");
		//Simulate user Opening Level
		if(!Getter.getModuleAddress(applicationRoot, dataStorageLessonId, userId).isEmpty())
		{
			//Then, Mark the Challenge Complete for Default Admin (Insecure Data Storage Lesson)
			String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, dataStorageLessonId, userId, "Feedback is Disabled", 1, 1, 1);
			if (markLevelCompleteTest != null)
			{
				String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, dataStorageLessonId, userId);
				if(checkPlayerResultTest == null)
					return; //Pass
				else
				{
					fail("Function says Admin has completed module before");
				}
			}
			else
				fail("Could not mark data storage lesson as complete Default admin");
		}
		else
			fail("Could not Mark Data Storage Lesson as Opened by Default admin");
	}
	
	/*
	@Test
	public void testIsCsrfLevelComplete() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindPlayerById() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllModuleInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChallenges() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClassCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClassInfoString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClassInfoStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCsrfForumWithIframe() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCsrfForumWithImg() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFeedback() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIncrementalModules() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIncrementalModulesWithoutScript() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetJsonScore() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLessons() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleAddress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleCategory() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleIdFromHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetModuleKeyType() {
		fail("Not yet implemented");
	}

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
