package com.mobshep.PoorAuthentication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
public class Main extends Activity {

	String Log;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.activity_unintended__data__leakage);
	
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
					
					Log = miniNote.getText().toString();
					
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
	
	
	

}
