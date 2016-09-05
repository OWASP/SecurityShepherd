package com.mobshep.mobileshepherd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */

public class UDataLeakage extends MainActivity implements NavigationView.OnNavigationItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udl_layout);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		
		
String destinationDir = "/data/data/" +getPackageName() + "/files/";
		
		String destinationPath = destinationDir + "Tue Jul 08 172618 EDT 2014";
		
		File f = new File(destinationPath);
		
		if (!f.exists()){
			File directory = new File(destinationDir);
			directory.mkdirs();
			//assets members.db -> /databases/
			
			try{
				copyKey(getBaseContext().getAssets().open("Tue Jul 08 172618 EDT 2014"), new FileOutputStream(destinationPath));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
		}
			

		ListView noteList = (ListView)findViewById(R.id.noteList);
		final EditText miniNote = (EditText)findViewById(R.id.miniNote);
		final ArrayList<String> noteItems = new ArrayList<String>();
		final ArrayAdapter<String> arrayAdapter;
		
		
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noteItems);

		noteList.setAdapter(arrayAdapter);
		
		miniNote.setOnKeyListener(new View.OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event){
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				if((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || keyCode == KeyEvent.KEYCODE_ENTER){
					
					String Log = miniNote.getText().toString();
					
					logDetails(Log);
					
					noteItems.add(0, miniNote.getText().toString());
					arrayAdapter.notifyDataSetChanged();
					miniNote.setText("");
					miniNote.setTextColor(Color.WHITE);
					
					return true;
				}
				return false;
			}
		}
		
				
				);
		
		
		
	}

	private void logDetails(String content) {
		// TODO Auto-generated method stub
		Date date = new Date();

		String filename = "LogFile" + date.toString();
		String EOL = System.getProperty("line.seperator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(
					filename, Context.MODE_WORLD_READABLE)));
			writer.write(content + EOL);
			writer.write(date + EOL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

		if (id == R.id.action_settings) {
			Intent goToSettings = new Intent(this, Preferences.class);
			startActivity(goToSettings);
			return true;
		}

        if (id == R.id.action_exit){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}
