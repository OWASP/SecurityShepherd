package servlets.module.lesson;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import utils.FileSystem;
import utils.ShepherdLogManager;
import utils.Validate;
import utils.XmlDocumentBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Cross Site Scripting Lesson
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * <p>
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * <p>
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author ismisepaul
 */
public class XxeLesson
        extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(XxeLesson.class);
    private static String levelName = "XXE Lesson";
    private static String levelHash = "57dda1bf9a2ca1c34e04f815491ef40836d9b710179cd19754ec5b3c31f27d1a";

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Setting IpAddress To Log and taking header for original IP if forwarded from proxy
        ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
        log.debug(levelName + " Servlet Accessed");
        PrintWriter out = response.getWriter();
        out.print(getServletInfo());

        //Translation Stuff
        Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
        ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.xxe", locale);

        try {
            HttpSession ses = request.getSession(true);
            if (Validate.validateSession(ses)) {
                ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
                        ses.getAttribute("userName").toString());
                log.debug(levelName + " accessed by: " + ses.getAttribute("userName").toString());
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
                        htmlOutput += "<p>" + bundle.getString("response.invalid.email") + ": "
                                + emailAddr + "</p>";
                        out.write(htmlOutput);
                    }
                }
            } else {
                log.error(levelName + " accessed with no session");
                out.write(errors.getString("error.noSession"));
            }
        } catch (Exception e) {
            out.write(errors.getString("error.funky"));
            log.fatal(levelName + " - " + e.toString());
        }
        log.debug("End of " + levelName + " Servlet");
    }

    public static String readXml(InputStream xmlEmail) {

        Document doc;
        String result = null;

        XmlDocumentBuilder db = new XmlDocumentBuilder();
        DocumentBuilder dBuilder = db.xmlDocBuilder(false, true, true, true, true, true);
        InputSource is = new InputSource(xmlEmail);

        try {
            doc = dBuilder.parse(is);
            Element root = doc.getDocumentElement();
            result = root.getTextContent();
            return Encode.forHtml(result.toString());
        } catch (SAXException e) {
            log.warn(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }

        return result;
    }

    /**
     * Creates the file with the solution key needed to pass the level
     */
    public static boolean createXxeLessonSolutionFile(){

        String filename = null;
        String solution;

        try {
            filename = FileSystem.readPropertiesFile("/lessons/xxe.properties", "xxe.lesson.file");
            solution = FileSystem.readPropertiesFile("/lessons/xxe.properties", "xxe.lesson.solution");

            if(FileSystem.isFileExists(filename)) {
                log.info("XXE Lesson Solution File " + filename + " already exists");
                FileSystem.deleteFile(filename);
                log.info("XXE Lesson Solution File " + filename + " deleted");
            }
            FileSystem.createFile(filename);
            FileSystem.writeFile(filename, solution);
            log.info("XXE Lesson Solution File " + filename + " created");
            return true;
        }
        catch (IOException e){
            log.error(e);
            return false;
        }

    }
}
