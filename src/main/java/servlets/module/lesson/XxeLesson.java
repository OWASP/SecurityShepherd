package servlets.module.lesson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dbProcs.Getter;
import utils.ShepherdLogManager;
import utils.Validate;
import utils.XmlDocumentBuilder;

/**
 * XXE Lesson
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * <p>
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * <p>
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author ismisepaul
 */
public class XxeLesson extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(XxeLesson.class);
	private static final String LEVEL_NAME = "XXE Lesson";
	private static final String LEVEL_HASH = "57dda1bf9a2ca1c34e04f815491ef40836d9b710179cd19754ec5b3c31f27d1a";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String ApplicationRoot = getServletContext().getRealPath("");
		String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, LEVEL_HASH);

		// Setting IpAddress To Log and taking header for original IP if forwarded from
		// proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(LEVEL_NAME + " Servlet Accessed");
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());

		// Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.xxe", locale);

		try {
			HttpSession ses = request.getSession(true);
			if (Validate.validateSession(ses)) {
				if (Getter.isModuleOpen(getServletContext().getRealPath(""), moduleId)) {
					ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
							ses.getAttribute("userName").toString());
					log.debug(LEVEL_NAME + " accessed by: " + ses.getAttribute("userName").toString());
					Cookie tokenCookie = Validate.getToken(request.getCookies());
					Object tokenHeader = request.getHeader("csrfToken").toString();

					if (Validate.validateTokens(tokenCookie, tokenHeader)) {
						InputStream xml = request.getInputStream();
						String emailAddr = readXml(xml);
						log.debug("Email Addr: " + emailAddr);

						String htmlOutput = new String();

						if (emailAddr == null) {
							htmlOutput += "<p>" + bundle.getString("response.blank.email") + "</p>";
							out.write(htmlOutput + emailAddr);
						} else if (Validate.isValidEmailAddress(emailAddr)) {
							log.debug("User Submitted - " + emailAddr);

							htmlOutput += "<p>" + bundle.getString("response.success.reset") + ": " + emailAddr
									+ " has been reset</p>";
							out.write(htmlOutput);
						} else {
							htmlOutput += "<p>" + bundle.getString("response.invalid.email") + ": " + emailAddr
									+ "</p>";
							out.write(htmlOutput);
						}
					}
				} else {
					log.error(LEVEL_NAME + " accessed but level is closed");
					out.write(errors.getString("error.notOpen"));
				}
			} else {
				log.error(LEVEL_NAME + " accessed with no session");
				out.write(errors.getString("error.noSession"));
			}
		} catch (Exception e) {
			out.write(errors.getString("error.funky"));
			log.fatal(LEVEL_NAME + " - " + e.toString());
		}
		log.debug("End of " + LEVEL_NAME + " Servlet");
	}

	public static String readXml(InputStream xmlEmail) {

		Document doc;
		String result = null;

		DocumentBuilder dBuilder = XmlDocumentBuilder.xmlDocBuilder(false, true, true, true, true, true);
		InputSource is = new InputSource(xmlEmail);

		try {
			doc = dBuilder.parse(is);
			Element root = doc.getDocumentElement();
			result = root.getTextContent();
			return Encode.forHtml(result.toString());
		} catch (SAXException e) {
			log.error(e.toString());
		} catch (IOException e) {
			log.error(e.toString());
		}

		return result;
	}

	/**
	 * Creates the file with the solution key needed to pass the level
	 */
	public static boolean createXxeLessonSolutionFile() {

		File lessonFile;

		Properties prop = new Properties();

		try (InputStream xxe_input = new FileInputStream(
				System.getProperty("user.dir") + "/src/main/resources/fileSystemKeys.properties")) {

			prop.load(xxe_input);

		} catch (IOException e) {
			log.error("Could not load properties file: " + e.toString());
			throw new RuntimeException(e);
		}

		String errorBase = "Missing property :";

		String filename = prop.getProperty("xxe.lesson.file");
		if (filename == null) {
			throw new RuntimeException(errorBase + "xxe.lesson.file");
		}
		String solution = prop.getProperty("xxe.lesson.solution");
		if (solution == null) {
			throw new RuntimeException(errorBase + "xxe.lesson.solution");
		}

		lessonFile = new File(filename);

		if (lessonFile.exists()) {
			log.info("XXE Lesson Solution File " + filename + " already exists");
			FileUtils.deleteQuietly(lessonFile);
			log.info("XXE Lesson Solution File " + filename + " deleted");
		}
		try {
			FileUtils.write(lessonFile, solution, "UTF-8");
		} catch (IOException e) {
			log.error("Could not load properties file: " + e.toString());
			throw new RuntimeException(e);
		}
		log.info("XXE Lesson Solution File " + filename + " created");
		return true;

	}
}
