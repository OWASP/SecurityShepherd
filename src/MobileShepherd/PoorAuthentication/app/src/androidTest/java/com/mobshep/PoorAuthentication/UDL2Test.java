package com.mobshep.PoorAuthentication;

import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;


public class UDL2Test extends ActivityInstrumentationTestCase2<UDataLeakage2> {

    private Solo solo;


    public UDL2Test() {
        super(UDataLeakage2.class);
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

        EditText etName = (EditText) solo.getView(R.id.etName);
        EditText etPass = (EditText) solo.getView(R.id.etPass);



        //no credentials
        solo.clickOnButton("Login");

        //wrong credentials
        solo.enterText(etName, String.valueOf("Stephen"));
        solo.enterText(etPass, String.valueOf("incorrectPassword"));

        solo.clickOnButton("Login");

    }

    public void testPasswordResetFail() throws Exception{

       EditText etAnswer1 = (EditText) solo.getView(R.id.etQuestion1);
       EditText etAnswer2 = (EditText) solo.getView(R.id.etQuestion2);

       //Button pFail = (Button) solo.getView(R.id.bForgot);

        solo.clickOnButton("Reset");



    }

}
