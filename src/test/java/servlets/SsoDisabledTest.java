package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import utils.SamlTestSupport;

/** With sso.enabled off, a retained SAML configuration must not make the SSO endpoints usable */
public class SsoDisabledTest {

  private SamlTestSupport saml;

  @BeforeEach
  public void installClientWithSsoDisabled() throws Exception {
    saml = new SamlTestSupport();
    saml.installClient();
    SamlTestSupport.setSsoEnabled(false);
  }

  @AfterEach
  public void uninstallClient() {
    SamlTestSupport.uninstallClient();
  }

  @Test
  public void acs_ValidAssertion_DoesNotAuthenticate() throws Exception {
    Map<String, List<String>> attributes = new LinkedHashMap<>();
    attributes.put("uid", Collections.singletonList("jdoe"));
    attributes.put("displayName", Collections.singletonList("Jane Doe"));
    attributes.put("groups", Arrays.asList("staff"));
    String samlResponse = saml.signedResponse("user-123", attributes, saml.idpKeys, saml.idpCert);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/saml/acs");
    request.setScheme("https");
    request.setSecure(true);
    request.setServerName("shepherd.test");
    request.setServerPort(443);
    request.addParameter("SAMLResponse", samlResponse);
    request.setCookies(saml.browserCookie());
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();

    new ACS().doPost(request, response);

    assertEquals(404, response.getStatus());
    assertNull(session.getAttribute("userName"));
  }

  @Test
  public void ssoLogin_IsNotFound() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new SSOLogin().doGet(new MockHttpServletRequest("GET", "/saml/dologin"), response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void sls_IsNotFound() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new SLS().doGet(new MockHttpServletRequest("GET", "/saml/sls"), response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void ssoMetadata_IsNotFound() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new SSOMetadata().doGet(new MockHttpServletRequest("GET", "/saml/metadata.xml"), response);
    assertEquals(404, response.getStatus());
  }
}
