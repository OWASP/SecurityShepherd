package servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class SetupFilter implements Filter {
	private static org.apache.log4j.Logger log = Logger.getLogger(SetupFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String requestURL = req.getRequestURL().toString();
		String pageName = FilenameUtils.getBaseName(requestURL);
		
		if (!pageName.contains("setup") && !Setup.isInstalled()) {
			log.info("System not setup. Forwarding to Setup");
			res.sendRedirect("setup.jsp");
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// 
	}
}
