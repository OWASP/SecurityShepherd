package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.insecuredata.InsecureDataLessonFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureDataLessonFragment
 * This lesson doesn't have exposed constants but tests verify the class structure exists
 */
public class InsecureDataLessonTest {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureDataLessonFragment class should exist", 
                     InsecureDataLessonFragment.class);
    }

    @Test
    public void testOpenDatabaseMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod("openDatabase");
        assertNotNull("openDatabase method should exist", method);
    }

    @Test
    public void testInsertUserMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod(
                "insertUser", String.class, String.class, String.class);
        assertNotNull("insertUser method should exist", method);
    }

    @Test
    public void testDisplayUsersMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod("displayUsers");
        assertNotNull("displayUsers method should exist", method);
    }
    
    @Test
    public void testFragmentHasOnCreateViewMethod() throws Exception {
        Method method = InsecureDataLessonFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testFragmentHasOnDestroyViewMethod() throws Exception {
        Method method = InsecureDataLessonFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }
}
