package utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * Each request gets its own random token, in its own cookie (named {@value #COOKIE} plus a hash of
 * the request ID), so logins started at once in several tabs can't overwrite each other's binding.
 * The cookie is HttpOnly, Secure and SameSite=None so it survives the IdP's cross-site POST back,
 * scoped to the SAML endpoint directory (the ACS URL's directory, e.g. /saml/), lives as long as
 * its request and is deleted once the login completes. The session cookie's policy is left alone.
 * <br>
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

  /** Prefix of the binding cookies' names */
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

  /** The name of the cookie binding a request to its browser */
  static String cookieName(String requestId) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(requestId.getBytes(StandardCharsets.UTF_8));
      return COOKIE
          + "_"
          + Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0, 22);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  /** Forgets a request once its response has been accepted, so each request is used once */
  void forget(String requestId) {
    if (requestId != null) {
      pending.remove(requestId);
    }
  }

  /** As {@link #forget(String)}, also deleting the request's binding cookie from the browser */
  void complete(String requestId, HttpServletResponse response) {
    forget(requestId);
    if (requestId != null) {
      response.addCookie(bindingCookie(requestId, "", 0));
    }
  }

  private Cookie bindingCookie(String requestId, String token, int maxAge) {
    Cookie cookie = new Cookie(cookieName(requestId), token);
    cookie.setPath(cookiePath);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setMaxAge(maxAge);
    cookie.setAttribute("SameSite", "None");
    return cookie;
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
      byte[] bytes = new byte[32];
      random.nextBytes(bytes);
      String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
      response.addCookie(bindingCookie(id, token, (int) LIFETIME.getSeconds()));
      pending.put(id, new Pending(token, message, now.plus(LIFETIME)));
    }

    @Override
    public Optional<XMLObject> get(String id) {
      Pending entry = id == null ? null : pending.get(id);
      String token = id == null ? null : cookieToken(cookieName(id));
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

    private String cookieToken(String name) {
      Cookie[] cookies = request.getCookies();
      if (cookies == null) {
        return null;
      }
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
          return cookie.getValue();
        }
      }
      return null;
    }
  }
}
