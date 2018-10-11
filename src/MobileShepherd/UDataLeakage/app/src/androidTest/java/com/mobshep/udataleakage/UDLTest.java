package com.mobshep.udataleakage;

import com.robotium.solo.Solo;
import com.mobshep.udataleakage.*;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.Button;


public class UDLTest extends ActivityInstrumentationTestCase2<UDataLeakage> {

    private Solo solo;


    public UDLTest() {
        super(UDataLeakage.class);
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

    //  public void testActionBar() throws Exception{
    //    solo.unlockScreen();
    //  solo.clickOnMenuItem("License");
    // solo.clickOnButton("OK");

    //}

    public void testLoginFail() throws Exception{

        EditText etNote = (EditText) solo.getView(R.id.miniNote);

        //no credentials


        //wrong credentials
        solo.enterText(etNote, String.valueOf("Test String"));

        solo.clickOnButton("Login");

    }


}
