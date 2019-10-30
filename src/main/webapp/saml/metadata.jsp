<%@ page contentType="application/xml; charset=UTF-8" pageEncoding="UTF-8"
	language="java"
	import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*, java.util.*,com.onelogin.saml2.Auth,com.onelogin.saml2.settings.Saml2Settings"%><%
	Auth auth = new Auth();
	Saml2Settings settings = auth.getSettings();
	settings.setSPValidationOnly(true);
	String metadata = settings.getSPMetadata();
	List<String> errors = Saml2Settings.validateMetadata(metadata);
	if (errors.isEmpty()) {
		out.println(metadata);
	} else {
		response.setContentType("text/html; charset=UTF-8");

		for (String error : errors) {
			out.println("<p>" + error + "</p>");
		}
	}
%>