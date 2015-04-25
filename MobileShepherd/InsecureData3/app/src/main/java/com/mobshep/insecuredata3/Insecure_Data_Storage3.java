package com.mobshep.insecuredata3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Insecure_Data_Storage3 extends Activity {

	Button insert;
	Button select;
	Button login;
	EditText password;
	EditText username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);

		String destinationDir = "/data/data/" + getPackageName() + "/databases/";

		String destinationPath = destinationDir + "PasswordDB";

		File f = new File(destinationPath);

		if (!f.exists()) {
			File directory = new File(destinationDir);
			directory.mkdirs();

			try {
				copyDatabase(getBaseContext().getAssets().open("PasswordDB"),
						new FileOutputStream(destinationPath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

	public void copyDatabase(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
			oStream.write(buffer, 0, i);
		}
		iStream.close();
		oStream.close();

	}


}
