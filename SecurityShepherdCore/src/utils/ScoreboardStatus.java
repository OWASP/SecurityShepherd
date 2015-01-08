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
	
	/**
	 * Disables scoreboard functions
	 */
	public static void disableScoreboard()
	{
		scoreboardEnabled = false;
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
	 * Enables public Scoreboard based on scores from specific class
	 * @param theClass Class to base the Scoreboard on
	 */
	public static void setScoreboardClass(String theClass)
	{
		scoreboardClass = theClass;
		scoreboardEnabled = true;
	}
	
	/**
	 * Sets scoreboard to list all players regardless of class
	 */
	public static void setScoreboeardOpen()
	{
		scoreboardEnabled = true;
		scoreboardClass = new String();
	}
}
