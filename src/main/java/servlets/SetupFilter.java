package servlets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

public class SetupFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    //
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    String requestURL = req.getRequestURL().toString();
    String pageName = FilenameUtils.getBaseName(requestURL);

    if (!Setup.isInstalled()) {
      if (pageName.contains("setup")
          || requestURL.contains("/css/")
          || requestURL.contains("/js/")) {
        chain.doFilter(request, response);
      } else {
        res.sendRedirect("setup.jsp");
      }
    } else if ("/setup".equals(req.getServletPath())) {
      // Setup reconfigures the database, so it's refused once the app is installed (#1043)
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    //
  }
}
