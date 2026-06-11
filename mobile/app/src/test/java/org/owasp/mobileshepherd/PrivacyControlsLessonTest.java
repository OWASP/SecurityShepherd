package org.owasp.mobileshepherd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
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
    public void testPreloadedImageConstantExists() throws Exception {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        Field preloadedImageField = fragmentClass.getDeclaredField("PRELOADED_IMAGE");
        preloadedImageField.setAccessible(true);
        String preloadedImageValue = (String) preloadedImageField.get(null);
        
        assertNotNull("PRELOADED_IMAGE constant should not be null", preloadedImageValue);
        assertTrue("PRELOADED_IMAGE should be a valid filename", preloadedImageValue.endsWith(".jpg") || preloadedImageValue.endsWith(".jpeg"));
    }

    @Test
    public void testShowDetailedInfoMethodExists() throws Exception {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        try {
            fragmentClass.getDeclaredMethod("showDetailedInfo");
        } catch (NoSuchMethodException e) {
            fail("showDetailedInfo() method should exist for FAB functionality");
        }
    }

    @Test
    public void testRequiredMethodsExist() throws Exception {
        Class<?> fragmentClass = Class.forName("org.owasp.mobileshepherd.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        // Check for key methods
        assertNotNull("loadPreloadedImage method should exist", 
            fragmentClass.getDeclaredMethod("loadPreloadedImage"));
        assertNotNull("analyzeCurrentImage method should exist", 
            fragmentClass.getDeclaredMethod("analyzeCurrentImage"));
        assertNotNull("createPreloadedImageWithFlag method should exist", 
            fragmentClass.getDeclaredMethod("createPreloadedImageWithFlag", File.class));
    }
}
