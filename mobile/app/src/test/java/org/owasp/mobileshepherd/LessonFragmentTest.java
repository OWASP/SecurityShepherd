package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.LessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for LessonFragment
 */
public class LessonFragmentTest {

    @Test
    public void testClassExists() {
        assertNotNull("LessonFragment class should exist", LessonFragment.class);
    }

    @Test
    public void testFragmentExtendsAndroidXFragment() {
        assertTrue("LessonFragment should extend androidx Fragment",
                androidx.fragment.app.Fragment.class.isAssignableFrom(LessonFragment.class));
    }

    @Test
    public void testCurrentFlagFieldExists() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("currentFlag");
        assertNotNull("currentFlag field should exist", flagField);
        assertEquals("currentFlag should be of type String", String.class, flagField.getType());
    }

    @Test
    public void testCurrentFlagIsInstanceField() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("currentFlag");
        assertFalse("currentFlag should be an instance field, not static",
                Modifier.isStatic(flagField.getModifiers()));
    }
}
