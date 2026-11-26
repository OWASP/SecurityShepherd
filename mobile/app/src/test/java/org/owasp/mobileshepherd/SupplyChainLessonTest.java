package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.supplychain.SupplyChainLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for SupplyChainLessonFragment.
 * The fragment names a vulnerable library and logs a server-issued debug key at runtime.
 */
public class SupplyChainLessonTest {

    private String vulnerableLib() throws Exception {
        Field libField = SupplyChainLessonFragment.class.getDeclaredField("VULNERABLE_LIB");
        libField.setAccessible(true);
        return (String) libField.get(null);
    }

    @Test
    public void testVulnerableLibConstantExists() throws Exception {
        String lib = vulnerableLib();
        assertNotNull("VULNERABLE_LIB constant should exist", lib);
        assertFalse("VULNERABLE_LIB should not be empty", lib.isEmpty());
    }

    @Test
    public void testVulnerableLibValue() throws Exception {
        assertEquals("Vulnerable library should match expected value",
                "androidx.exifinterface:exifinterface:1.3.7", vulnerableLib());
    }

    @Test
    public void testVulnerableLibIsMavenCoordinate() throws Exception {
        String lib = vulnerableLib();
        assertTrue("VULNERABLE_LIB should be a Maven coordinate (group:artifact:version)",
                lib.contains(":") && lib.split(":").length == 3);
    }

    @Test
    public void testDebugLoggingTakesServerKey() throws Exception {
        Method method = SupplyChainLessonFragment.class.getDeclaredMethod("initializeAnalyticsSDK", String.class);
        assertNotNull("initializeAnalyticsSDK(String) should accept the server-issued key", method);
    }

    @Test
    public void testNoHardcodedKeyField() {
        for (Field f : SupplyChainLessonFragment.class.getDeclaredFields()) {
            assertNotEquals("Key must be server-issued, not hardcoded", "EXIF_DEBUG_KEY", f.getName());
        }
    }
}
