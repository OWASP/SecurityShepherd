package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServlet;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** A POST to the SAML login or metadata endpoint redirects to the application's index (#948) */
public class SsoPostRedirectTest {

  private static String redirectPathAfterPost(HttpServlet servlet, String requestPath)
      throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", requestPath);
    MockHttpServletResponse response = new MockHttpServletResponse();

    servlet.service(request, response);

    // The browser resolves a relative Location against the URL it requested
    return URI.create("https://shepherd.test" + requestPath)
        .resolve(response.getRedirectedUrl())
        .getPath();
  }

  @Test
  public void ssoLogin_Post_RedirectsToApplicationIndex() throws Exception {
    assertEquals("/index.jsp", redirectPathAfterPost(new SSOLogin(), "/saml/dologin"));
  }

  @Test
  public void ssoMetadata_Post_RedirectsToApplicationIndex() throws Exception {
    assertEquals("/index.jsp", redirectPathAfterPost(new SSOMetadata(), "/saml/metadata.xml"));
  }
}
