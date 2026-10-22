package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class SsoUserMapperTest {

  private static Properties ssoProperties() {
    Properties props = new Properties();
    props.setProperty("sso.saml.ssoName", "uid");
    props.setProperty("sso.saml.userName", "displayName");
    props.setProperty("sso.saml.affiliation", "groups");
    props.setProperty("sso.saml.adminAffiliation", "staff, teachers");
    props.setProperty("sso.saml.playerAffiliation", "students");
    return props;
  }

  private static Map<String, Object> attributes(Object groups) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("uid", Collections.singletonList("jdoe"));
    attributes.put("displayName", Collections.singletonList("Jane Doe"));
    attributes.put("groups", groups);
    return attributes;
  }

  @Test
  public void map_AdminAffiliation_IsAdmin() {
    SsoUserMapper.Result result =
        SsoUserMapper.map(attributes(Arrays.asList("students", "teachers")), ssoProperties());

    assertTrue(result.isValid());
    assertEquals("jdoe", result.ssoName);
    assertEquals("Jane Doe", result.userName);
    assertEquals("admin", result.userRole);
  }

  @Test
  public void map_PlayerAffiliation_IsPlayer() {
    SsoUserMapper.Result result =
        SsoUserMapper.map(attributes(Collections.singletonList("students")), ssoProperties());

    assertTrue(result.isValid());
    assertEquals("player", result.userRole);
  }

  @Test
  public void map_SingleValuedAttributes_AreRead() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("uid", "jdoe");
    attributes.put("displayName", "Jane Doe");
    attributes.put("groups", "staff");

    SsoUserMapper.Result result = SsoUserMapper.map(attributes, ssoProperties());

    assertTrue(result.isValid());
    assertEquals("jdoe", result.ssoName);
    assertEquals("admin", result.userRole);
  }

  @Test
  public void map_NoMatchingAffiliation_ListsAllowedGroups() {
    SsoUserMapper.Result result =
        SsoUserMapper.map(attributes(Collections.singletonList("alumni")), ssoProperties());

    assertFalse(result.isValid());
    assertEquals(
        "Authorization failed. Please ensure that you are a member of one of the following"
            + " groups: staff, teachers, students, ",
        result.error);
  }

  @Test
  public void map_NoAffiliationAttribute_IsRejected() {
    Map<String, Object> attributes = attributes(null);
    attributes.remove("groups");

    assertFalse(SsoUserMapper.map(attributes, ssoProperties()).isValid());
  }

  @Test
  public void map_MissingUserName_IsRejected() {
    Map<String, Object> attributes = attributes(Collections.singletonList("staff"));
    attributes.remove("displayName");

    SsoUserMapper.Result result = SsoUserMapper.map(attributes, ssoProperties());

    assertFalse(result.isValid());
    assertEquals("Required SSO attributes are missing", result.error);
  }

  @Test
  public void map_NoAttributes_IsRejected() {
    SsoUserMapper.Result result = SsoUserMapper.map(Collections.emptyMap(), ssoProperties());

    assertFalse(result.isValid());
    assertEquals("You don't have any attributes", result.error);
  }
}
