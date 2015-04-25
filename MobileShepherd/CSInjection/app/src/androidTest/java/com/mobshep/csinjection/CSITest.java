package com.mobshep.csinjection;

import com.robotium.solo.Solo;
import com.mobshep.csinjection.CSInjection;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;


public class CSITest extends ActivityInstrumentationTestCase2<CSInjection>{

    private Solo solo;

    public CSITest(){
        super(CSInjection.class);
    }


    @Override
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }

    public void testActionBar() throws Exception{
        solo.unlockScreen();
        solo.clickOnMenuItem("License");
        solo.clickOnButton("OK");
    }

    public void testBlankLogin(){
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.setActivityOrientation(Solo.PORTRAIT);

        EditText etName = (EditText) solo.getView(R.id.etName);
        EditText etPass = (EditText) solo.getView(R.id.etPass);

        solo.clickOnButton("Login");

        solo.enterText(etName, String.valueOf("Admin"));
        solo.enterText(etPass, String.valueOf("Password"));

        solo.clickOnButton("Login");

        solo.enterText(etName, String.valueOf("Test"));
        solo.enterText(etPass, String.valueOf("Password 1 (*&^%$"));


    }

}