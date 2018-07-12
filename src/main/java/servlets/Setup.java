package servlets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import dbProcs.Constants;
import dbProcs.Database;
import utils.InstallationException;

public class Setup extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(Setup.class);
	private static final long serialVersionUID = -892181347446991016L;

	public void doPost (HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
		HttpSession ses = request.getSession(true);

		String dbHost = request.getParameter("dbhost");
		String dbPort = request.getParameter("dbport");
		String dbUser = request.getParameter("dbuser");
		String dbPass = request.getParameter("dbpass");
		String dbAuth = request.getParameter("dbauth");
		String dbOverride = request.getParameter("dboverride");

		String auth = new String(Files.readAllBytes(Paths.get(Constants.SETUP_AUTH)));

		StringBuffer dbProp = new StringBuffer();
		dbProp.append("databaseConnectionURL=jdbc:mysql://" + dbHost + ":" + dbPort + "/");
		dbProp.append("\n");
		dbProp.append("DriverType=org.gjt.mm.mysql.Driver");
		dbProp.append("\n");
		dbProp.append("databaseSchema=core");
		dbProp.append("\n");
		dbProp.append("databaseUsername=" + dbUser);
		dbProp.append("\n");
		dbProp.append("databasePassword=" + dbPass);
		dbProp.append("\n");

		if (!auth.equals(dbAuth)) {
			String dbAuthFailed = "Authentication failure. Please paste the content of the file " + Constants.SETUP_AUTH
					+ " into the authentication field.";
			ses.setAttribute("dbAuthFailed", dbAuthFailed);
		} else {
			Files.write(Paths.get(Constants.DBPROP), dbProp.toString().getBytes(), StandardOpenOption.CREATE);	
			if (Database.getDatabaseConnection(null) == null) {
				String dbConnectionFailed = "Connection to Database failed";
				ses.setAttribute("dbConnectionFailed", dbConnectionFailed);
			} else {
				try {
					if (dbOverride != null) {
						executeSqlScript();
					}
				} catch (InstallationException e) {
					String dbSetupFailed = "Failed setting up the database: " +  e.getMessage();
					ses.setAttribute("dbSetupFailed", dbSetupFailed);
					FileUtils.deleteQuietly(new File(Constants.DBPROP));
				}
			}

		}

		response.sendRedirect("setup.jsp");

	}

	public static boolean isInstalled() {
		boolean isInstalled;
		Connection coreConnection = Database.getDatabaseConnection(null);
		if (coreConnection == null) {
			isInstalled = false;
			generateAuth();
		} else {
			isInstalled = true;
		}

		return isInstalled;
	}

	private static void generateAuth() {
		try {
			if (!Files.exists(Paths.get(Constants.SETUP_AUTH), LinkOption.NOFOLLOW_LINKS)) {
				UUID randomUUID = UUID.randomUUID();
				Files.write(Paths.get(Constants.SETUP_AUTH), randomUUID.toString().getBytes(), StandardOpenOption.CREATE);
				log.info("genrated UUID " + randomUUID + " in " + Constants.SETUP_AUTH);
			}
		} catch (IOException e) {
			log.fatal("Unable to generate auth");
			e.printStackTrace();
		}
	}

	private synchronized void executeSqlScript() throws InstallationException {

		try {
			File file = new File(getClass().getClassLoader().getResource("/database/coreSchema.sql").getFile());
			String data = FileUtils.readFileToString(file, Charset.defaultCharset() );
			
			Connection databaseConnection = Database.getDatabaseConnection(null, true);
			Statement psProcToexecute = databaseConnection.createStatement();
			psProcToexecute.executeUpdate(data);
			
			file = new File(getClass().getClassLoader().getResource("/database/moduleSchemas.sql").getFile());
			data = FileUtils.readFileToString(file, Charset.defaultCharset() );
			psProcToexecute = databaseConnection.createStatement();
			psProcToexecute.executeUpdate(data);

		} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
			throw new InstallationException(e);
		}
	}
}
