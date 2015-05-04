package com.mobshep.brokencrypto1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

/**
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Sean Duggan
 */

public class BrokenCrypto1 extends Activity {


    Button messageOne, messageTwo, messageThree;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(com.mobshep.brokencrypto1.R.layout.broken);
		referenceXML();



        String destinationDir = "/data/data/" + getPackageName() + "/encrypt/";

		String destinationPath = destinationDir + "desKey";

		File f = new File(destinationPath);

		if (!f.exists()) {
			File directory = new File(destinationDir);
			directory.mkdirs();
			// assets members.db -> /databases/

			try {
				copyKey(getBaseContext().getAssets().open("desKey"),
						new FileOutputStream(destinationPath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		startTimerOne();
		startTimerTwo();
		startTimerThree();

	}



	private void startTimerOne() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {

				try {
					Thread.sleep(2000);
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
					Thread.sleep(5000);
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
					Thread.sleep(7000);
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

	public void copyKey(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
			oStream.write(buffer, 0, i);
		}
		iStream.close();
		oStream.close();

	}

	public void copyMessage1(View v) {

		String copiedMessage = messageOne.getText().toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("message1", copiedMessage);
		clipboard.setPrimaryClip(clip);

	    showToast();

	}



    public void copyMessage2(View v) {

		String copiedMessage2 = messageTwo.getText().toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("message2", copiedMessage2);
		clipboard.setPrimaryClip(clip);

        showToast();

	}

	public void copyMessage3(View v) {

		String copiedMessage3 = messageThree.getText().toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("message3", copiedMessage3);
		clipboard.setPrimaryClip(clip);

        showToast();
	}

    private void showToast() {

        Toast copied = Toast.makeText(BrokenCrypto1.this,
                "Message copied to clipboard.", Toast.LENGTH_LONG);
        copied.show();

    }

    private void referenceXML() {
        // TODO Auto-generated method stub
        messageOne = (Button) findViewById(com.mobshep.brokencrypto1.R.id.Message1);
        messageTwo = (Button) findViewById(com.mobshep.brokencrypto1.R.id.Message2);
        messageThree = (Button) findViewById(com.mobshep.brokencrypto1.R.id.Message3);
        messageOne.setVisibility(View.INVISIBLE);
        messageTwo.setVisibility(View.INVISIBLE);
        messageThree.setVisibility(View.INVISIBLE);


    }

}
