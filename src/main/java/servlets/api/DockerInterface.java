package servlets.api;


import org.apache.log4j.Logger;
import utils.DockerController;
import utils.ScoreboardStatus;
import utils.ShepherdLogManager;
import utils.Validate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/docker")
public class DockerInterface extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = Logger.getLogger(DockerInterface.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Setting IpAddress To Log and taking header for original IP if forwarded from proxy
        ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
        log.debug("/api/docker GET called");
        PrintWriter out = response.getWriter();
        HttpSession ses = request.getSession(true);
        if (Validate.validateSession(ses)) {
            ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
            log.debug("Docker interface called by " + ses.getAttribute("userName").toString());
            Cookie tokenCookie = Validate.getToken(request.getCookies());
            Object tokenParmeter = request.getParameter("csrfToken");
            if (Validate.validateTokens(tokenCookie, tokenParmeter)) {
                log.debug("Valid session");
                DockerController docker = new DockerController();
                String dockerID = request.getParameter("dockerID");
                if ( dockerID != null){
                    String logs = request.getParameter("logs");
                    if (logs != null){
                        out.print(docker.getContainerLogs(dockerID));
                        return;
                    }
                    response.setHeader("Content-Type", "application/json");
                    log.info("Request for docker information received for " + dockerID + " by " + ses.getAttribute("userName").toString());
                    out.print(docker.getContainerData(dockerID));
                    //out.print(docker.getContainerData("058afa9b1566f31b8ebae22ec2d48476156be229c15eecbd3f175d57834d4c35"));
                    return;
                }else {
                    log.info("Request received for all docker information from " + ses.getAttribute("userName").toString());
                    out.print(docker.getContainerData());
                    return;
                }
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DockerController docker = new DockerController();
        docker.stopContainer("058afa9b1566f31b8ebae22ec2d48476156be229c15eecbd3f175d57834d4c35");
        PrintWriter out = resp.getWriter();
        out.write("Stopping Container");
    }


}
