package com.mobshep.UDataLeakage2;

import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;


public class UDL3Test extends ActivityInstrumentationTestCase2<UDL3>{

    private Solo solo;



    public UDL3Test(){
        super(UDL3.class);
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

        solo.clickOnMenuItem("Disclaimer");
        solo.clickOnButton("OK");

    }

    public void testPostMessage(){
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.setActivityOrientation(Solo.PORTRAIT);

        EditText etSecret = (EditText) solo.getView(R.id.etSecret);
        solo.enterText(etSecret, String.valueOf("TestMessage1"));

        solo.clickOnButton("Submit");

        solo.clickOnCheckBox(0);
        solo.enterText(etSecret, String.valueOf("TestMessage2"));

        solo.clickOnButton("Submit");


    //    solo.clickOnMenuItem("Exit");

    }

}