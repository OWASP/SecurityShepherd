package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maps the attributes released by the SAML identity provider to a Security Shepherd user, using the
 * attribute names and affiliations configured in sso.properties. <br>
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
public class SsoUserMapper {

  /** Result of mapping: either a user or an error message for the login page */
  public static class Result {
    public final String ssoName;
    public final String userName;
    public final String userRole;
    public final String error;

    private Result(String ssoName, String userName, String userRole, String error) {
      this.ssoName = ssoName;
      this.userName = userName;
      this.userRole = userRole;
      this.error = error;
    }

    public boolean isValid() {
      return error == null;
    }
  }

  /**
   * Maps IdP attributes to a user.
   *
   * @param attributes Attributes from the SAML assertion. Values may be single objects or
   *     collections.
   * @param ssoProperties sso.properties contents
   * @return The mapped user, or an error if a required attribute is missing or the user has none of
   *     the configured affiliations
   */
  public static Result map(Map<String, Object> attributes, Properties ssoProperties) {
    if (attributes == null || attributes.isEmpty()) {
      return error("You don't have any attributes");
    }
    String ssoName = first(attributes, ssoProperties.getProperty("sso.saml.ssoName"));
    String userName = first(attributes, ssoProperties.getProperty("sso.saml.userName"));
    if (ssoName == null || userName == null) {
      return error("Required SSO attributes are missing");
    }

    List<String> affiliations =
        values(attributes, ssoProperties.getProperty("sso.saml.affiliation"));
    List<String> adminAffiliations =
        splitList(ssoProperties.getProperty("sso.saml.adminAffiliation"));
    List<String> playerAffiliations =
        splitList(ssoProperties.getProperty("sso.saml.playerAffiliation"));

    if (!Collections.disjoint(affiliations, adminAffiliations)) {
      return new Result(ssoName, userName, "admin", null);
    }
    if (!Collections.disjoint(affiliations, playerAffiliations)) {
      return new Result(ssoName, userName, "player", null);
    }

    StringBuilder message =
        new StringBuilder(
            "Authorization failed. Please ensure that you are a member of one of the"
                + " following groups: ");
    for (String affiliation : adminAffiliations) {
      message.append(affiliation).append(", ");
    }
    for (String affiliation : playerAffiliations) {
      message.append(affiliation).append(", ");
    }
    return error(message.toString());
  }

  private static Result error(String message) {
    return new Result(null, null, null, message);
  }

  private static String first(Map<String, Object> attributes, String key) {
    List<String> values = values(attributes, key);
    return values.isEmpty() ? null : values.get(0);
  }

  private static List<String> values(Map<String, Object> attributes, String key) {
    List<String> values = new ArrayList<>();
    Object value = key == null ? null : attributes.get(key);
    if (value instanceof Collection) {
      for (Object item : (Collection<?>) value) {
        if (item != null) {
          values.add(item.toString());
        }
      }
    } else if (value != null) {
      values.add(value.toString());
    }
    return values;
  }

  private static List<String> splitList(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(value.trim().split("\\s*,\\s*"));
  }
}
