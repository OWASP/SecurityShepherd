package utils;

/**
 * Scoreboard management class
 * @author Mark Denihan
 *
 */
public class ScoreboardStatus
{
	private static boolean scoreboardEnabled = true;
	private static String scoreboardClass = new String();
	private static boolean adminOnlyScoreboard = false;
	
	/**
	 * Returns if user is authorised to see scoreboard currenly. 
	 * @param userRole Must be player or admin
	 * @return
	 */
	public static boolean canSeeScoreboard (String userRole)
	{
		boolean authorised = true;
		if(adminOnlyScoreboard)
			authorised = userRole.equalsIgnoreCase("admin");
		return authorised && scoreboardEnabled;
	}
	
	/**
	 * Disables scoreboard functions
	 */
	public static void disableScoreboard()
	{
		scoreboardEnabled = false;
		adminOnlyScoreboard = false;
		scoreboardClass = new String();
	}
	
	public static String getScoreboardClass()
	{
		return scoreboardClass;
	}
	
	/**
	 * Is the scoreboard configured to be displayed?
	 * @return 
	 */
	public static boolean isScoreboardEnabled()
	{
		return scoreboardEnabled;
	}
	
	/**
	 * Sets the scoreboard to be admin only
	 */
	public static void setScoreboardAdminOnly()
	{
		adminOnlyScoreboard = true;
	}
	
	/**
	 * Enables public Scoreboard based on scores from specific class
	 * @param theClass Class to base the Scoreboard on
	 */
	public static void setScoreboardClass(String theClass)
	{
		scoreboardClass = theClass;
		scoreboardEnabled = true;
		adminOnlyScoreboard = false;
	}
	
	/**
	 * Sets scoreboard to list all players regardless of class
	 */
	public static void setScoreboeardOpen()
	{
		scoreboardEnabled = true;
		scoreboardClass = new String();
		adminOnlyScoreboard = false;
	}
}
