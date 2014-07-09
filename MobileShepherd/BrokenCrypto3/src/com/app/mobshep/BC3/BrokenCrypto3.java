package com.app.mobshep.BC3;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

public class BrokenCrypto3 extends Activity {

	EditText messageOne, messageTwo, messageThree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		referenceXML();
		startTimerOne();
		startTimerTwo();
		startTimerThree();
	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		messageOne = (EditText) findViewById(R.id.tvMessage1);
		messageTwo = (EditText) findViewById(R.id.tvMessage2);
		messageThree = (EditText) findViewById(R.id.tvMessage3);
		messageOne.setVisibility(View.INVISIBLE);
		messageTwo.setVisibility(View.INVISIBLE);
		messageThree.setVisibility(View.INVISIBLE);
	}

	private void startTimerOne() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					public void run() {
						messageOne.setVisibility(View.VISIBLE);
					}
				});

			}
		};
		new Thread(runnable).start();
	}

	private void startTimerTwo() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {

				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					public void run() {
						messageTwo.setVisibility(View.VISIBLE);
					}
				});

			}
		};
		new Thread(runnable).start();
	}

	private void startTimerThree() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {

				try {
					Thread.sleep(11000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					public void run() {
						messageThree.setVisibility(View.VISIBLE);
					}
				});

			}
		};
		new Thread(runnable).start();
	}

}
