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
			if (pageName.contains("setup") || requestURL.contains("/css/") || requestURL.contains("/js/")) {
				chain.doFilter(request, response);
			} else {
				res.sendRedirect("setup.jsp");
			}
		}  else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// 
	}
}
