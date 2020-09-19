package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;

import dbProcs.MongoDatabase;
import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;

import utils.ShepherdLogManager;
import utils.Validate;

import static dbProcs.MongoDatabase.getMongoDatabase;
import static dbProcs.MongoDatabase.getMongoDbConnection;

/**
 * NoSQL Injection Challenge One - Does not use user specific key <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Paul McCann
 *
 */
public class NoSqlInjection1 extends HttpServlet {
	// Sql Challenge 3
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(NoSqlInjection1.class);
	private static String levelName = "NoSQL Injection Challenge One";
	public static String levelHash = "d63c2fb5da9b81ca26237f1308afe54491d1bacf9fffa0b21a072b03c5bafe66";

	// private static String levelResult = ""; // Stored in Vulnerable DB. Not User
	// Specific
	/**
	 * Users have to use NoSQL injection to get a specific user (Marlo) gamer ID.
	 * The query they are injecting into by default only outputs usernames. The
	 * input they enter is also been filtered. theGamerName User name used in
	 * database look up.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting IpAddress To Log and taking header for original IP if forwarded from
		// proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		// Attempting to recover user name of session that made request
		HttpSession ses = request.getSession(true);
		if (Validate.validateSession(ses)) {
			Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
			ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.injection.nosql", locale);

			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
					ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());

			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			String htmlOutput = new String();

			DBCollection dbCollection;
			MongoCredential credential;
			MongoClient mongoClient = null;
			DB mongoDb;
			String dbCollectionName;
			DBCursor cursor;
			Object id;
			Object name;
			Object address;

			try {
				String applicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + applicationRoot);

				credential = MongoDatabase.getMongoChallengeCredentials(applicationRoot, "NoSqlInjection1");
				log.debug("Credentials created");
				mongoClient = getMongoDbConnection(applicationRoot, credential);
				log.debug("Client created");
				mongoDb = getMongoDatabase(mongoClient);
				log.debug("Database DB connection acquired");
				dbCollectionName = MongoDatabase.getMongoChallengeCollName(applicationRoot, "NoSqlInjection1");
				dbCollection = mongoDb.getCollection(dbCollectionName);

				String gamerId = request.getParameter("theGamerName");
				log.debug("User Submitted: " + gamerId);

				DBObject whereQuery = new BasicDBObject("$where", "this._id == '" + gamerId + "'");
				cursor = dbCollection.find(whereQuery);

				try {
					int i = 0;
					htmlOutput = "<h2 class='title'>Gamer Info</h2>";
					htmlOutput += "<table><tr><th>GamerId</th><th>Name</th><th>Address</th>";

					log.debug("Opening Result Set from query");

					while (cursor.hasNext()) {
						DBObject result = cursor.next();
						id = result.get("_id");
						name = result.get("name");
						address = result.get("address");

						log.debug(bundle.getString("results.queryResult") + result.toString());
						htmlOutput += "<tr><td>" + Encode.forHtml(id.toString()) + "</td><td>"
								+ Encode.forHtml(name.toString()) + "</td><td>" + Encode.forHtml(address.toString())
								+ "</td></tr>";
						i++;
					}
					htmlOutput += "</table>";
					if (i == 0) {
						htmlOutput = "<p>" + bundle.getString("result.none") + "</p>";
					}

				} catch (MongoTimeoutException e) {
					log.fatal(bundle.getString("result.mongoError") + e.toString());
					htmlOutput += "<p>Mongo Timeout Occured</p>" + "<p>" + Encode.forHtml(e.toString()) + "</p>";
				} catch (MongoException e) {
					log.error(bundle.getString("result.mongoError") + e.toString());
					htmlOutput += "<p>An error was detected!</p>" + "<p>" + Encode.forHtml(e.toString()) + "</p>";
				} catch (Exception e) {
					out.write("An Error Occurred! You must be getting funky!");
					log.fatal(levelName + " - " + e.toString());
				} finally {
					cursor.close();
					mongoClient.close();
				}
			} catch (MongoSocketException e) {
				log.error(bundle.getString("result.mongoError") + e.toString());
				htmlOutput += "<p>An error was detected!</p>" + "<p>" + Encode.forHtml(e.toString()) + "</p>";
			} catch (MongoException e) {
				log.fatal("MongoDb Error caught - " + e.toString());
				htmlOutput += "<p>An error was detected!</p>" + "<p>" + Encode.forHtml(e.toString()) + "</p>";
			} finally {
				mongoClient.close();
			}

			log.debug("Outputting HTML");
			out.write(htmlOutput);
		} else {
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
