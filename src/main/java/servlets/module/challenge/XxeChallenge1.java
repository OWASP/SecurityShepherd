package servlets.module.challenge;

import dbProcs.FileInputProperties;
import dbProcs.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.owasp.encoder.Encode;
import utils.ShepherdLogManager;
import utils.Validate;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * XXE Challenge 1
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
public class XxeChallenge1
        extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(XxeChallenge1.class);
    private static final String LEVEL_NAME = "XXE Challenge 1";
    private static final String LEVEL_HASH = "ac8f3f6224b1ea3fb8a0f017aadd0d84013ea2c80e232c980e54dd753700123e";

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ApplicationRoot = getServletContext().getRealPath("");
        String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, LEVEL_HASH);

        //Setting IpAddress To Log and taking header for original IP if forwarded from proxy
        ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
        log.debug(LEVEL_NAME + " Servlet Accessed");
        PrintWriter out = response.getWriter();
        out.print(getServletInfo());

        //Translation Stuff
        Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
        ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.injection.xxe1", locale);

        try {
            HttpSession ses = request.getSession(true);
            if (Validate.validateSession(ses))
            {
                if (Getter.isModuleOpen(getServletContext().getRealPath(""), moduleId))
                {
                    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
                            ses.getAttribute("userName").toString());
                    log.debug(LEVEL_NAME + " accessed by: " + ses.getAttribute("userName").toString());
                    Cookie tokenCookie = Validate.getToken(request.getCookies());
                    Object tokenHeader = request.getHeader("csrfToken").toString();

                    if (Validate.validateTokens(tokenCookie, tokenHeader)) {
                        InputStream json = request.getInputStream();
                        String emailAddr = readJson(json, errors);
                        emailAddr = Encode.forHtml(emailAddr);
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
                }
                else
                {
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

    public static String readJson(InputStream jsonEmail, ResourceBundle errors) {
        String result;

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;
        try
        {
            jsonObject = (JSONObject)jsonParser.parse(
                    new InputStreamReader(jsonEmail, StandardCharsets.UTF_8));
            result = jsonObject.get("email").toString();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            return errors.getString("error.funky");
        }

        return null;
    }

    /**
     * Creates the file with the solution key needed to pass the level
     */
    public static boolean createXxeChallenge1SolutionFile(){

        File lessonFile;
        String filename;
        String solution;

        try {
            filename = FileInputProperties.readPropFileClassLoader("fileSystemKeys.properties", "xxe.challenge.1.file");
            solution = FileInputProperties.readPropFileClassLoader("fileSystemKeys.properties", "xxe.challenge.1.solution");

            lessonFile = new File(filename);

            if(lessonFile.exists()) {
                log.info("XXE Challenge 1 Solution File " + filename + " already exists");
                FileUtils.deleteQuietly(lessonFile);
                log.info("XXE Challenge 1 Solution File " + filename + " deleted");
            }
            FileUtils.write(lessonFile, solution, "UTF-8");
            log.info("XXE Challenge 1 Solution File " + filename + " created");
            return true;
        }
		catch (FileNotFoundException e) {
			log.error(e);
			throw new RuntimeException(e);
			
		} catch (IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		}

    }
}
