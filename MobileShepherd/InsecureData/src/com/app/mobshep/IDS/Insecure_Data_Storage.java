package com.app.mobshep.IDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Insecure_Data_Storage extends Activity {

	Button login;
	EditText password;
	EditText username;

	private static final int MY_NOTIFICATION_ID = 1;

	private int mNotificationCount;

	private final CharSequence tickerText = "Login has been disabled due to a vulnerability!";
	private final CharSequence contentTitle = "Login Disabled!";
	private final CharSequence contentText = "App contains vulnerability!";
	private Intent NotificationIntent;
	private long[] vibrate = { 0, 200, 200, 300 };

	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		referenceXML();
		
	
		String destinationDir = "/data/data/" +getPackageName() + "/databases";
		
		String destinationPath = destinationDir + "Members";
		
		File f = new File(destinationPath);
		
		if (!f.exists()){
			File directory = new File(destinationDir);
			directory.mkdirs();
			//assets members.db -> /databases/
			
			try{
				copyDatabase(getBaseContext().getAssets().open("Members"), new FileOutputStream(destinationPath));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
		}
		
		NotificationIntent = new Intent(getApplicationContext(),
				Insecure_Data_Storage.class);
		PendingIntent.getActivity(getApplicationContext(), 0,
				NotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);


		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// what to do if you login
				Toast toast = Toast.makeText(Insecure_Data_Storage.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast toast2 = Toast.makeText(Insecure_Data_Storage.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					toast2.show();
				}
		

				if (CheckName.contentEquals("EpicTrees")
						|| CheckName.contentEquals("GraveyBones")
						|| CheckName.contentEquals("Admin")
						|| CheckName.contentEquals("FallenComrade")
						|| CheckName.contentEquals("IronFist")
						|| CheckName.contentEquals("Jumper")
						|| CheckName.contentEquals("99chips")
						|| CheckName.contentEquals("RegularVeg")) {
					
					
					
					Notification.Builder notificationBuilder = new Notification.Builder(
							getApplicationContext())
							.setTicker(tickerText)
							.setSmallIcon(R.drawable.ic_launcher)
							.setAutoCancel(true)
							.setContentTitle(contentTitle)
							.setContentText(
									contentText + " (" + ++mNotificationCount
											+ ")").setVibrate(vibrate);

					// Pass the Notification to the NotificationManager:
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(MY_NOTIFICATION_ID,
							notificationBuilder.build());

					

					Toast locked = Toast.makeText(Insecure_Data_Storage.this,
							"That Account is locked.", Toast.LENGTH_SHORT);
					locked.show();

				}

				else {
					Toast toast4 = Toast.makeText(Insecure_Data_Storage.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
					toast4.show();

				}
			}
		});
        
	}

	

	public void copyDatabase(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
				oStream.write(buffer, 0 , i);
		}
		iStream.close();
		oStream.close();
		
	}
	
	
	
	public void referenceXML() {
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);

	}

}
