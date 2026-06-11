package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.supplychain.SupplyChainLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for SupplyChainLessonFragment
 * The fragment embeds a vulnerable library identifier and an EXIF debug key.
 */
public class SupplyChainLessonTest {

    @Test
    public void testVulnerableLibConstantExists() throws Exception {
        Field libField = SupplyChainLessonFragment.class.getDeclaredField("VULNERABLE_LIB");
        libField.setAccessible(true);
        String lib = (String) libField.get(null);
        assertNotNull("VULNERABLE_LIB constant should exist", lib);
        assertFalse("VULNERABLE_LIB should not be empty", lib.isEmpty());
    }

    @Test
    public void testVulnerableLibValue() throws Exception {
        Field libField = SupplyChainLessonFragment.class.getDeclaredField("VULNERABLE_LIB");
        libField.setAccessible(true);
        String lib = (String) libField.get(null);
        assertEquals("Vulnerable library should match expected value",
                "androidx.exifinterface:exifinterface:1.3.7", lib);
    }

    @Test
    public void testVulnerableLibIsMavenCoordinate() throws Exception {
        Field libField = SupplyChainLessonFragment.class.getDeclaredField("VULNERABLE_LIB");
        libField.setAccessible(true);
        String lib = (String) libField.get(null);
        assertTrue("VULNERABLE_LIB should be a Maven coordinate (group:artifact:version)",
                lib.contains(":") && lib.split(":").length == 3);
    }

    @Test
    public void testExifDebugKeyExists() throws Exception {
        Field keyField = SupplyChainLessonFragment.class.getDeclaredField("EXIF_DEBUG_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        assertNotNull("EXIF_DEBUG_KEY constant should exist", key);
        assertFalse("EXIF_DEBUG_KEY should not be empty", key.isEmpty());
    }

    @Test
    public void testExifDebugKeyValue() throws Exception {
        Field keyField = SupplyChainLessonFragment.class.getDeclaredField("EXIF_DEBUG_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        assertEquals("EXIF debug key should match expected value",
                "exif_debug_processor_key_1337", key);
    }

    @Test
    public void testExifDebugKeyContainsDebugIndicator() throws Exception {
        Field keyField = SupplyChainLessonFragment.class.getDeclaredField("EXIF_DEBUG_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        assertTrue("Key should contain 'debug' indicating it should not be in production",
                key.contains("debug"));
    }
}
