package com.app.mobshep.UDL2;

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
