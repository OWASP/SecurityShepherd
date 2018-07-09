package com.mobshep.mobileshepherd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BrokenCrypto2 extends MainActivity implements NavigationView.OnNavigationItemSelectedListener{

	Button messageOne, messageTwo, messageThree;
	
	/*
	Toast copied = Toast.makeText(BrokenCrypto3.this,
			"Message Copied to Clipboard!", Toast.LENGTH_SHORT);
*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken_layout2);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		referenceXML();
		startTimerOne();
		startTimerTwo();
		startTimerThree();

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

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
			
		
		
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_ids) {
			Intent gotoIDS = new Intent(BrokenCrypto2.this, Insecure_Data_Storage.class);
			startActivity(gotoIDS);
		} else if (id == R.id.nav_ids1) {
			Intent gotoIDS1 = new Intent(BrokenCrypto2.this, Insecure_Data_Storage1.class);
			startActivity(gotoIDS1);
		} else if (id == R.id.nav_ids2) {
			Intent gotoIDS2 = new Intent(BrokenCrypto2.this, Insecure_Data_Storage2.class);
			startActivity(gotoIDS2);
		} else if (id == R.id.nav_ids3) {
			Intent gotoIDS3 = new Intent(BrokenCrypto2.this, ids3Login.class);
			startActivity(gotoIDS3);
		}else if (id == R.id.nav_bc) {
			Intent gotoBC = new Intent(BrokenCrypto2.this, BrokenCrypto.class);
			startActivity(gotoBC);
		}else if (id == R.id.nav_bc1) {
			Intent gotoBC1 = new Intent(BrokenCrypto2.this, BrokenCrypto1.class);
			startActivity(gotoBC1);
		}else if (id == R.id.nav_bc2) {
			Intent gotoBC2 = new Intent(BrokenCrypto2.this, BrokenCrypto2.class);
			startActivity(gotoBC2);
		}else if (id == R.id.nav_bc3) {
			Intent gotoBC3 = new Intent(BrokenCrypto2.this, BrokenCrypto3.class);
			startActivity(gotoBC3);
		}else if (id == R.id.nav_csi) {
			Intent gotoCSI = new Intent(BrokenCrypto2.this, CSInjection.class);
			startActivity(gotoCSI);
		}else if (id == R.id.nav_csi1) {
			Intent gotoCSI1 = new Intent(BrokenCrypto2.this, CSInjection.class);
			startActivity(gotoCSI1);
		}else if (id == R.id.nav_csi2) {
			Intent gotoCSI2 = new Intent(BrokenCrypto2.this, CSInjection.class);
			startActivity(gotoCSI2);
		} if (id == R.id.nav_csi2) {
			Intent gotoCSI2 = new Intent(BrokenCrypto2.this, CSInjection.class);
			startActivity(gotoCSI2);
		}if (id == R.id.nav_udl) {
			Intent gotoUDL = new Intent(BrokenCrypto2.this, UDataLeakage.class);
			startActivity(gotoUDL);
		}if (id == R.id.nav_udl1) {
			Intent gotoUDL1 = new Intent(BrokenCrypto2.this, UDataLeakage1.class);
			startActivity(gotoUDL1);
		}if (id == R.id.nav_udl2) {
			Intent gotoUDL2 = new Intent(BrokenCrypto2.this, UnintendedDataLeakage2.class);
			startActivity(gotoUDL2);
		}if (id == R.id.nav_pl) {
			Intent gotoPL = new Intent(BrokenCrypto2.this, providerLeakage.class);
			startActivity(gotoPL);

		}else if (id == R.id.nav_scoreboard) {
			//link to shepherd or webview?
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
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
