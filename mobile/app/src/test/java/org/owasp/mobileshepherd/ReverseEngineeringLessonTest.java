package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.reverseengineering.ReverseEngineeringLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for ReverseEngineeringLessonFragment (Reverse Engineering Lesson)
 */
public class ReverseEngineeringLessonTest {

    @Test
    public void lesson_ClassExists() {
        assertNotNull("ReverseEngineeringLessonFragment class should exist", ReverseEngineeringLessonFragment.class);
    }

    @Test
    public void lesson_FragmentExtendsAndroidXFragment() {
        assertTrue("ReverseEngineeringLessonFragment should extend androidx Fragment",
                androidx.fragment.app.Fragment.class.isAssignableFrom(ReverseEngineeringLessonFragment.class));
    }

    @Test
    public void lesson_HasCurrentKeyField() throws Exception {
        Field keyField = ReverseEngineeringLessonFragment.class.getDeclaredField("currentKey");
        assertEquals("currentKey should be of type String", String.class, keyField.getType());
        assertFalse("currentKey should be an instance field",
                Modifier.isStatic(keyField.getModifiers()));
    }

    @Test
    public void lesson_HasBindingField() throws Exception {
        Field bindingField = ReverseEngineeringLessonFragment.class.getDeclaredField("binding");
        assertNotNull("binding field should exist", bindingField);
        assertFalse("binding should be an instance field",
                Modifier.isStatic(bindingField.getModifiers()));
    }

    @Test
    public void lesson_OnCreateViewMethodExists() throws Exception {
        Method method = ReverseEngineeringLessonFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void lesson_HasAtLeastTwoFields() {
        Field[] fields = ReverseEngineeringLessonFragment.class.getDeclaredFields();
        assertTrue("ReverseEngineeringLessonFragment should have at least 2 declared fields",
                fields.length >= 2);
    }
}
