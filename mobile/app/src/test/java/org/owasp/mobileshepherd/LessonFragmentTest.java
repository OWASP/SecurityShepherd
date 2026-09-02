package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.reverseengineering.ReverseEngineeringLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for ReverseEngineeringLessonFragment
 */
public class LessonFragmentTest {

    @Test
    public void testClassExists() {
        assertNotNull("ReverseEngineeringLessonFragment class should exist", ReverseEngineeringLessonFragment.class);
    }

    @Test
    public void testFragmentExtendsAndroidXFragment() {
        assertTrue("ReverseEngineeringLessonFragment should extend androidx Fragment",
                androidx.fragment.app.Fragment.class.isAssignableFrom(ReverseEngineeringLessonFragment.class));
    }

    @Test
    public void testCurrentFlagFieldExists() throws Exception {
        Field flagField = ReverseEngineeringLessonFragment.class.getDeclaredField("currentFlag");
        assertNotNull("currentFlag field should exist", flagField);
        assertEquals("currentFlag should be of type String", String.class, flagField.getType());
    }

    @Test
    public void testCurrentFlagIsInstanceField() throws Exception {
        Field flagField = ReverseEngineeringLessonFragment.class.getDeclaredField("currentFlag");
        assertFalse("currentFlag should be an instance field, not static",
                Modifier.isStatic(flagField.getModifiers()));
    }
}
