package com.app.mobshep.ITLS;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle Mobile) {
		// TODO Auto-generated method stub
		super.onCreate(Mobile);
		setContentView(R.layout.splash);
		// implement a thread to move on from the intro screen to input screen
		Thread timer = new Thread() {
			public void run() {
				// catch exceptions
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally

				{
					Intent gotoMain = new Intent("com.app.mobshep.ITLS.InsufficientTLS");
					startActivity(gotoMain);
				}

			}
		};

		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}
