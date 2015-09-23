package dbProcs;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

public class GetterTest 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GetterTest.class);
	private static String propertiesFileDirectory = new String("/site");
	@Test
	public void testAuthUser() {
		if(Getter.authUser(System.getProperty("user.dir")+propertiesFileDirectory, "admin", "password") == null)
			fail("Could not Authenticate Default admin");
		else
			return; //Pass
	}

	@Test
	public void testCheckPlayerResult() {
		fail("Not yet implemented");
	}

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

}
