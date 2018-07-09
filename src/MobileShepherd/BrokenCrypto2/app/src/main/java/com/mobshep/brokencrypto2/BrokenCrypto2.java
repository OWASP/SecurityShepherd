package com.mobshep.brokencrypto2;

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
import android.widget.Button;
import android.widget.Toast;

public class BrokenCrypto2 extends Activity {

	Button messageOne, messageTwo, messageThree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		
		referenceXML();

		String destinationDir = "/data/data/" +getPackageName() + "/encrypt/";

		String destinationPath1 = destinationDir + "key1";

		File f = new File(destinationPath1);

		if (!f.exists()){
			File directory = new File(destinationDir);
			directory.mkdirs();
			//assets members.db -> /databases/

			try{
				copyKey(getBaseContext().getAssets().open("key1"), new FileOutputStream(destinationPath1));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}

			String destinationPath2 = destinationDir + "key2";

			File g = new File(destinationPath2);

			if (!g.exists()){
				File directory2 = new File(destinationDir);
				directory2.mkdirs();
				//assets members.db -> /databases/

				try{
					copyKey(getBaseContext().getAssets().open("key2"), new FileOutputStream(destinationPath2));
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			}

				String destinationPath3 = destinationDir + "key3";

				File e = new File(destinationPath3);

				if (!e.exists()){
					File directory3 = new File(destinationDir);
					directory3.mkdirs();
					//assets members.db -> /databases/

					try{
						copyKey(getBaseContext().getAssets().open("key3"), new FileOutputStream(destinationPath3));
					}catch(FileNotFoundException e1){
						e1.printStackTrace();
					}catch(IOException e1){
						e1.printStackTrace();
					}
				}

        startTimerOne();
        startTimerTwo();
        startTimerThree();


	}



	private void referenceXML() {
		// TODO Auto-generated method stub
		messageOne = (Button) findViewById(R.id.Message1);
		messageTwo = (Button) findViewById(R.id.Message2);
		messageThree = (Button) findViewById(R.id.Message3);
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

        Toast copied = Toast.makeText(BrokenCrypto2.this,
                "Message copied to clipboard.", Toast.LENGTH_LONG);
        copied.show();

    }


}
