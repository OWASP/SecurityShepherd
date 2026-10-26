package org.owasp.mobileshepherd.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class ModuleAvailabilityTest {

    @Test
    public void parse_returnsOnlyModulesThatAreNotAllowed() throws Exception {
        String body = "{\"available\":true,\"modules\":{"
                + "\"open_module\":{\"status\":\"ALLOWED\"},"
                + "\"closed_module\":{\"status\":\"CLOSED\",\"message\":\"Module is not open\"},"
                + "\"blocked_module\":{\"status\":\"BLOCKED\"}}}";

        Map<String, String> denied = ModuleAvailability.parse(body);

        assertEquals(2, denied.size());
        assertFalse(denied.containsKey("open_module"));
        assertEquals("Module is not open", denied.get("closed_module"));
        assertTrue(denied.containsKey("blocked_module"));
    }

    @Test
    public void parse_whenServerSaysUnavailable_greysNothingOut() throws Exception {
        String body = "{\"available\":false,\"message\":\"Not authenticated\"}";

        assertTrue(ModuleAvailability.parse(body).isEmpty());
    }

    @Test
    public void parse_withGarbage_greysNothingOut() throws Exception {
        assertTrue(ModuleAvailability.parse("<html>502 Bad Gateway</html>").isEmpty());
    }
}
