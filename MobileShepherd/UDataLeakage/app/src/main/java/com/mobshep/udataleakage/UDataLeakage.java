package com.mobshep.udataleakage;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

public class UDataLeakage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udl);
		
		
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

        if (id == R.id.action_license) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("License");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App is part of the Security Shepherd Project. The Security Shepherd project is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with the Security Shepherd project.  If not, see http://www.gnu.org/licenses.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }


        if (id == R.id.action_disclaimer) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Disclaimer");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App may collect logs via various methods. By using this App you agree to this.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }


        if (id == R.id.action_exit){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}
