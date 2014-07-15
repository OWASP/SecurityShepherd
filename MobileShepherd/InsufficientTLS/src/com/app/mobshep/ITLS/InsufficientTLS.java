package com.app.mobshep.ITLS;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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

public class InsufficientTLS extends Activity implements OnClickListener {

	Button save;
	Button send;
	EditText IP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure);
		referenceXML();

	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		IP = (EditText) findViewById(R.id.etIP);
		send = (Button) findViewById(R.id.bSecret);
		save = (Button) findViewById(R.id.bSave);
		send.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case (R.id.bSave):

			break;

		case (R.id.bSecret):

			// send the message to be intercepted to the Shepherd server

			Toast sendingMessage = Toast.makeText(InsufficientTLS.this,
					"Sent Message!", Toast.LENGTH_SHORT);
			sendingMessage.show();
			break;

		}
	}
}
