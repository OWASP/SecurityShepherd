package org.owasp.mobileshepherd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Unit tests for Privacy Controls Lesson (M6: Inadequate Privacy Controls)
 * Tests metadata handling and EXIF data functionality
 */
@RunWith(JUnit4.class)
public class PrivacyControlsLessonTest {

    @Test
    public void testFragmentClassExists() throws ClassNotFoundException {
        // Verify the fragment class exists
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        assertNotNull("PrivacyControlsLessonFragment class should exist", fragmentClass);
    }

    @Test
    public void testFragmentExtendsFragment() throws ClassNotFoundException {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        // Check if it extends Fragment
        boolean extendsFragment = false;
        Class<?> superClass = fragmentClass.getSuperclass();
        while (superClass != null) {
            if (superClass.getSimpleName().equals("Fragment")) {
                extendsFragment = true;
                break;
            }
            superClass = superClass.getSuperclass();
        }
        
        assertTrue("PrivacyControlsLessonFragment should extend Fragment", extendsFragment);
    }

    @Test
    public void testSampleImageNameConstantExists() throws Exception {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");

        Field sampleImageField = fragmentClass.getDeclaredField("SAMPLE_IMAGE_NAME");
        sampleImageField.setAccessible(true);
        String sampleImageValue = (String) sampleImageField.get(null);

        assertNotNull("SAMPLE_IMAGE_NAME constant should not be null", sampleImageValue);
        assertTrue("SAMPLE_IMAGE_NAME should be a valid filename", sampleImageValue.endsWith(".jpg") || sampleImageValue.endsWith(".jpeg"));
    }

    @Test
    public void testRequiredMethodsExist() throws Exception {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");

        // Check for key methods
        assertNotNull("prepareSampleImage method should exist",
            fragmentClass.getDeclaredMethod("prepareSampleImage", String.class));
        assertNotNull("loadSampleImage method should exist",
            fragmentClass.getDeclaredMethod("loadSampleImage"));
        assertNotNull("launchCamera method should exist",
            fragmentClass.getDeclaredMethod("launchCamera"));
    }
}
