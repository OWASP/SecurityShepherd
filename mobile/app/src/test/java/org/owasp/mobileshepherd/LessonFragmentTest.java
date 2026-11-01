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
    public void testCurrentKeyFieldExists() throws Exception {
        Field keyField = ReverseEngineeringLessonFragment.class.getDeclaredField("currentKey");
        assertNotNull("currentKey field should exist", keyField);
        assertEquals("currentKey should be of type String", String.class, keyField.getType());
    }

    @Test
    public void testCurrentKeyIsInstanceField() throws Exception {
        Field keyField = ReverseEngineeringLessonFragment.class.getDeclaredField("currentKey");
        assertFalse("currentKey should be an instance field, not static",
                Modifier.isStatic(keyField.getModifiers()));
    }
}
