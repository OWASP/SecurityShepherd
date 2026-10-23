package utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.opensaml.core.xml.XMLObject;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.store.SAMLMessageStore;
import org.pac4j.saml.store.SAMLMessageStoreFactory;

/**
 * Remembers the SAML requests this SP sends, each bound to the browser that started it, so pac4j
 * only accepts a Response whose InResponseTo names a request started in the same browser. This
 * stops login CSRF: a valid response for one account can't be replayed through another browser's
 * assertion consumer service. <br>
 * The binding is a random token in a dedicated cookie ({@value #COOKIE}), HttpOnly, Secure and
 * SameSite=None so it survives the IdP's cross-site POST back, and scoped to the SAML endpoint
 * directory (the ACS URL's directory, e.g. /saml/), so a second login in the same browser (another
 * tab) reuses the token instead of replacing it. The session cookie's policy is left alone. <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 */
public class SamlRequestStore implements SAMLMessageStoreFactory {

  static final String COOKIE = "shepherd_saml_request";

  /** How long a started login may take at the IdP */
  static final Duration LIFETIME = Duration.ofMinutes(10);

  /** Upper bound on pending requests; the oldest are dropped beyond it */
  static int maxPending = 20000;

  private static final SecureRandom random = new SecureRandom();

  private static final class Pending {
    final String token;
    final XMLObject message;
    final Instant expires;

    Pending(String token, XMLObject message, Instant expires) {
      this.token = token;
      this.message = message;
      this.expires = expires;
    }
  }

  private final Map<String, Pending> pending = new ConcurrentHashMap<>();
  private final String cookiePath;

  /**
   * @param assertionConsumerServiceUrl The ACS URL, whose directory (including any context prefix)
   *     scopes the binding cookie, so both the login start and the ACS receive it
   */
  public SamlRequestStore(String assertionConsumerServiceUrl) {
    String path = URI.create(assertionConsumerServiceUrl).getPath();
    this.cookiePath =
        path == null || path.lastIndexOf('/') < 0
            ? "/"
            : path.substring(0, path.lastIndexOf('/') + 1);
  }

  /** The binding cookie's Path */
  String cookiePath() {
    return cookiePath;
  }

  @Override
  public SAMLMessageStore getMessageStore(WebContext context, SessionStore sessionStore) {
    JEEContext jee = (JEEContext) context;
    return new BoundStore(jee.getNativeRequest(), jee.getNativeResponse());
  }

  /** Forgets a request once its response has been accepted, so each request is used once */
  void forget(String requestId) {
    if (requestId != null) {
      pending.remove(requestId);
    }
  }

  private final class BoundStore implements SAMLMessageStore {
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    BoundStore(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    @Override
    public void set(String id, XMLObject message) {
      Instant now = Instant.now();
      pending.values().removeIf(p -> p.expires.isBefore(now));
      while (pending.size() >= maxPending) {
        pending.entrySet().stream()
            .min(Comparator.comparing(e -> e.getValue().expires))
            .ifPresent(oldest -> pending.remove(oldest.getKey()));
      }
      pending.put(id, new Pending(bindingToken(), message, now.plus(LIFETIME)));
    }

    @Override
    public Optional<XMLObject> get(String id) {
      Pending entry = id == null ? null : pending.get(id);
      String token = cookieToken();
      if (entry == null || token == null || entry.expires.isBefore(Instant.now())) {
        return Optional.empty();
      }
      boolean sameBrowser =
          MessageDigest.isEqual(
              entry.token.getBytes(StandardCharsets.US_ASCII),
              token.getBytes(StandardCharsets.US_ASCII));
      return sameBrowser ? Optional.of(entry.message) : Optional.empty();
    }

    @Override
    public void remove(String id) {
      forget(id);
    }

    /**
     * This browser's binding token, creating one if it has none yet. The cookie is (re)sent either
     * way, so it lasts as long as the newest pending request and an earlier one's expiry can't drop
     * it while a later login is still in progress.
     */
    private String bindingToken() {
      Object issued = request.getAttribute(COOKIE);
      if (issued instanceof String) {
        return (String) issued;
      }
      String token = cookieToken();
      if (token == null) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
      }
      Cookie cookie = new Cookie(COOKIE, token);
      cookie.setPath(cookiePath);
      cookie.setHttpOnly(true);
      cookie.setSecure(true);
      cookie.setMaxAge((int) LIFETIME.getSeconds());
      cookie.setAttribute("SameSite", "None");
      response.addCookie(cookie);
      request.setAttribute(COOKIE, token);
      return token;
    }

    private String cookieToken() {
      Cookie[] cookies = request.getCookies();
      if (cookies == null) {
        return null;
      }
      for (Cookie cookie : cookies) {
        if (COOKIE.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
          return cookie.getValue();
        }
      }
      return null;
    }
  }
}
