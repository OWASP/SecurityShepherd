package com.mobshep.mobileshepherd;

import java.io.File;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class Insecure_Data_Storage2 extends MainActivity {

	private static String DB_PATH = "/data/data/com.mobshep.mobileshepherd/databases/InsecureDataStorage2/";
	private static String DB_NAME = "PasswordDB";
	SQLiteDatabase passwordDB = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle(R.string.ids2);
		setContentView(R.layout.ids_layout);
		createDatabase();
        insertKey();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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


	public void createDatabase() {
		try {
			String path = DB_PATH + DB_NAME;
			passwordDB = this.openOrCreateDatabase(path, MODE_PRIVATE, null);
			passwordDB.execSQL("CREATE TABLE IF NOT EXISTS passwordDB " +
							"(id integer primary key, name VARCHAR, password VARCHAR);"
			);

			File database = getApplication().getDatabasePath("passwordDB.db");

			if (!database.exists()) {
				Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Log.e("DB ERROR", "Error Creating Database");
		}
	}

	public void insertKey(){
		passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('Admin','0e3a0c8c3a571a855c958813d9b851a1');");
	}

}
