package utils;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tracks which HTTP sessions were created by which SAML login, so an IdP-initiated logout can end
 * exactly the sessions it names (by NameID and SessionIndex), whichever browser delivers the logout
 * message, and whether or not it carries a session cookie. <br>
 * Entries are removed when their session ends (logout, invalidation or timeout). The registry is
 * held in memory, so sessions restored after a server restart are not tracked. <br>
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
public class SamlSessionRegistry {

  private static final Logger log = LogManager.getLogger(SamlSessionRegistry.class);

  static final String REGISTRATION_ATTRIBUTE = "samlSessionRegistration";

  private static final Map<String, Entry> sessions = new ConcurrentHashMap<>();

  private static class Entry {
    final HttpSession session;
    final String nameId;
    final String sessionIndex;

    Entry(HttpSession session, String nameId, String sessionIndex) {
      this.session = session;
      this.nameId = nameId;
      this.sessionIndex = sessionIndex;
    }
  }

  /** Session attribute that removes the registry entry when its session ends */
  private static class Registration implements HttpSessionBindingListener, Serializable {
    private static final long serialVersionUID = 1L;
    private final String key;

    Registration(String key) {
      this.key = key;
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
      sessions.remove(key);
    }
  }

  /**
   * Records that a session belongs to a SAML login.
   *
   * @param session The post-login session
   * @param nameId The complete NameID identity from the assertion (see
   *     SamlSso.registerLoginSession)
   * @param sessionIndex The SessionIndex from the assertion, if any
   */
  public static void register(HttpSession session, String nameId, String sessionIndex) {
    String key = UUID.randomUUID().toString();
    sessions.put(key, new Entry(session, nameId, sessionIndex));
    session.setAttribute(REGISTRATION_ATTRIBUTE, new Registration(key));
  }

  /**
   * Invalidates the sessions belonging to a SAML login.
   *
   * @param nameId The complete NameID identity named in the logout request
   * @param sessionIndex The SessionIndex named in the logout request, or null to end every session
   *     for the NameID
   * @return How many sessions were invalidated
   */
  public static int invalidate(String nameId, String sessionIndex) {
    if (nameId == null) {
      return 0;
    }
    int invalidated = 0;
    for (Map.Entry<String, Entry> item : sessions.entrySet()) {
      Entry entry = item.getValue();
      if (!nameId.equals(entry.nameId)
          || (sessionIndex != null && !sessionIndex.equals(entry.sessionIndex))) {
        continue;
      }
      sessions.remove(item.getKey());
      try {
        entry.session.invalidate();
        invalidated++;
      } catch (IllegalStateException alreadyInvalidated) {
        log.debug("Session for SAML logout was already invalidated");
      }
    }
    return invalidated;
  }

  /** Number of tracked sessions; for tests */
  static int size() {
    return sessions.size();
  }
}
