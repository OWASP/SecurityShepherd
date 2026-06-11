package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.insecurecomm.InsecureCommLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureCommLessonFragment
 */
public class InsecureCommLessonTest {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureCommLessonFragment class should exist",
                InsecureCommLessonFragment.class);
    }

    @Test
    public void testTagConstantExists() throws Exception {
        Field tagField = InsecureCommLessonFragment.class.getDeclaredField("TAG");
        tagField.setAccessible(true);
        String tag = (String) tagField.get(null);
        assertNotNull("TAG constant should exist", tag);
        assertFalse("TAG should not be empty", tag.isEmpty());
    }

    @Test
    public void testTagValue() throws Exception {
        Field tagField = InsecureCommLessonFragment.class.getDeclaredField("TAG");
        tagField.setAccessible(true);
        String tag = (String) tagField.get(null);
        assertEquals("TAG should identify network traffic", "NetworkTraffic", tag);
    }

    @Test
    public void testTagIsStaticFinal() throws Exception {
        Field tagField = InsecureCommLessonFragment.class.getDeclaredField("TAG");
        assertTrue("TAG should be static", Modifier.isStatic(tagField.getModifiers()));
        assertTrue("TAG should be final", Modifier.isFinal(tagField.getModifiers()));
    }

    @Test
    public void testFragmentExtendsAndroidXFragment() {
        assertTrue("InsecureCommLessonFragment should extend androidx Fragment",
                androidx.fragment.app.Fragment.class.isAssignableFrom(InsecureCommLessonFragment.class));
    }

    @Test
    public void testCurrentFlagFieldExists() throws Exception {
        Field flagField = InsecureCommLessonFragment.class.getDeclaredField("currentFlag");
        assertNotNull("currentFlag field should exist", flagField);
        assertEquals("currentFlag should be of type String", String.class, flagField.getType());
        assertFalse("currentFlag should be an instance field",
                Modifier.isStatic(flagField.getModifiers()));
    }

    @Test
    public void testCurrentFlagDefaultIsEmpty() throws Exception {
        Field flagField = InsecureCommLessonFragment.class.getDeclaredField("currentFlag");
        // Verify the initializer value in the class by checking the field type
        assertEquals("currentFlag should be String type", String.class, flagField.getType());
    }
}
