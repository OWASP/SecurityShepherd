package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.LessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for LessonFragment (Reverse Engineering Lesson)
 */
public class ReverseEngineeringLessonTest {

    @Test
    public void lesson_ClassExists() {
        assertNotNull("LessonFragment class should exist", LessonFragment.class);
    }

    @Test
    public void lesson_FragmentExtendsAndroidXFragment() {
        assertTrue("LessonFragment should extend androidx Fragment",
                androidx.fragment.app.Fragment.class.isAssignableFrom(LessonFragment.class));
    }

    @Test
    public void lesson_HasCurrentFlagField() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("currentFlag");
        assertEquals("currentFlag should be of type String", String.class, flagField.getType());
        assertFalse("currentFlag should be an instance field",
                Modifier.isStatic(flagField.getModifiers()));
    }

    @Test
    public void lesson_HasBindingField() throws Exception {
        Field bindingField = LessonFragment.class.getDeclaredField("binding");
        assertNotNull("binding field should exist", bindingField);
        assertFalse("binding should be an instance field",
                Modifier.isStatic(bindingField.getModifiers()));
    }

    @Test
    public void lesson_OnCreateViewMethodExists() throws Exception {
        Method method = LessonFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void lesson_HasAtLeastTwoFields() {
        Field[] fields = LessonFragment.class.getDeclaredFields();
        assertTrue("LessonFragment should have at least 2 declared fields",
                fields.length >= 2);
    }
}
