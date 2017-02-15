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

public class BrokenCrypto1 extends MainActivity implements NavigationView.OnNavigationItemSelectedListener {


    Button messageOne, messageTwo, messageThree;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken_layout1);
		referenceXML();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

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
			Intent gotoIDS = new Intent(BrokenCrypto1.this, Insecure_Data_Storage.class);
			startActivity(gotoIDS);
		} else if (id == R.id.nav_ids1) {
			Intent gotoIDS1 = new Intent(BrokenCrypto1.this, Insecure_Data_Storage1.class);
			startActivity(gotoIDS1);
		} else if (id == R.id.nav_ids2) {
			Intent gotoIDS2 = new Intent(BrokenCrypto1.this, Insecure_Data_Storage2.class);
			startActivity(gotoIDS2);
		} else if (id == R.id.nav_ids3) {
			Intent gotoIDS3 = new Intent(BrokenCrypto1.this, ids3Login.class);
			startActivity(gotoIDS3);
		}else if (id == R.id.nav_bc) {
			Intent gotoBC = new Intent(BrokenCrypto1.this, BrokenCrypto.class);
			startActivity(gotoBC);
		}else if (id == R.id.nav_bc1) {
			Intent gotoBC1 = new Intent(BrokenCrypto1.this, BrokenCrypto1.class);
			startActivity(gotoBC1);
		}else if (id == R.id.nav_bc2) {
			Intent gotoBC2 = new Intent(BrokenCrypto1.this, BrokenCrypto2.class);
			startActivity(gotoBC2);
		}else if (id == R.id.nav_bc3) {
			Intent gotoBC3 = new Intent(BrokenCrypto1.this, BrokenCrypto3.class);
			startActivity(gotoBC3);
		}else if (id == R.id.nav_csi) {
			Intent gotoCSI = new Intent(BrokenCrypto1.this, CSInjection.class);
			startActivity(gotoCSI);
		}else if (id == R.id.nav_csi1) {
			Intent gotoCSI1 = new Intent(BrokenCrypto1.this, CSInjection.class);
			startActivity(gotoCSI1);
		}else if (id == R.id.nav_csi2) {
			Intent gotoCSI2 = new Intent(BrokenCrypto1.this, CSInjection.class);
			startActivity(gotoCSI2);
		} if (id == R.id.nav_csi2) {
			Intent gotoCSI2 = new Intent(BrokenCrypto1.this, CSInjection.class);
			startActivity(gotoCSI2);
		}if (id == R.id.nav_udl) {
			Intent gotoUDL = new Intent(BrokenCrypto1.this, UDataLeakage.class);
			startActivity(gotoUDL);
		}if (id == R.id.nav_udl1) {
			Intent gotoUDL1 = new Intent(BrokenCrypto1.this, UDataLeakage1.class);
			startActivity(gotoUDL1);
		}if (id == R.id.nav_pl) {
				Intent gotoPL = new Intent(BrokenCrypto1.this, providerLeakage.class);
				startActivity(gotoPL);

		}else if (id == R.id.nav_scoreboard) {
			//link to shepherd or webview?
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
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
        messageOne = (Button) findViewById(R.id.bc1Message1);
        messageTwo = (Button) findViewById(R.id.bc1Message2);
        messageThree = (Button) findViewById(R.id.bc1Message3);
        messageOne.setVisibility(View.INVISIBLE);
        messageTwo.setVisibility(View.INVISIBLE);
        messageThree.setVisibility(View.INVISIBLE);


    }



}
