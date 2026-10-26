package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import servlets.MobileModuleGate.Decision;

class MobileModuleStatusTest {

  @Test
  void toJson_reportsEveryDecisionWithItsStatus() {
    Map<String, Decision> decisions = new TreeMap<>();
    decisions.put("open_module", Decision.ALLOWED);
    decisions.put("closed_module", Decision.CLOSED);
    decisions.put("not_running_module", Decision.NOT_RUNNING);
    decisions.put("blocked_module", Decision.BLOCKED);

    JSONObject json = new JSONObject(MobileModuleStatus.toJson(decisions));
    JSONObject modules = json.getJSONObject("modules");

    assertTrue(json.getBoolean("available"));
    assertEquals("ALLOWED", modules.getJSONObject("open_module").getString("status"));
    assertEquals("CLOSED", modules.getJSONObject("closed_module").getString("status"));
    assertEquals("NOT_RUNNING", modules.getJSONObject("not_running_module").getString("status"));
    assertEquals("BLOCKED", modules.getJSONObject("blocked_module").getString("status"));
  }

  @Test
  void toJson_addsAMessageOnlyWhenTheModuleIsDenied() {
    Map<String, Decision> decisions = new TreeMap<>();
    decisions.put("open_module", Decision.ALLOWED);
    decisions.put("closed_module", Decision.CLOSED);

    JSONObject modules =
        new JSONObject(MobileModuleStatus.toJson(decisions)).getJSONObject("modules");

    assertFalse(modules.getJSONObject("open_module").has("message"));
    assertEquals(
        Decision.CLOSED.message(), modules.getJSONObject("closed_module").getString("message"));
  }

  @Test
  void toJson_withNoModules_isAnEmptyObject() {
    JSONObject json = new JSONObject(MobileModuleStatus.toJson(new TreeMap<>()));

    assertTrue(json.getBoolean("available"));
    assertEquals(0, json.getJSONObject("modules").length());
  }
}
