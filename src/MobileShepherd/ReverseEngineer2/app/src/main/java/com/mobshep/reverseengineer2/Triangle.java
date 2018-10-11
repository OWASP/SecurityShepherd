package com.mobshep.reverseengineer2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class Triangle extends Activity implements OnClickListener {

	TextView jdiewjdwjddewdewdw;
	Button joiuwhiunwjdewniuewnmiux;
	EditText iourewsdxc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverse);

		joiuwhiunwjdewniuewnmiux = (Button) findViewById(R.id.bRevealKey);
		iourewsdxc = (EditText) findViewById(R.id.etPassword);
		jdiewjdwjddewdewdw = (TextView) findViewById(R.id.tvKey1);

		joiuwhiunwjdewniuewnmiux.setOnClickListener(this);


	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bRevealKey:
			String Check = iourewsdxc.getText().toString();
			jdiewjdwjddewdewdw.setText(Check);
																																															if (Check.contentEquals("huyfr65rtgydedsjk09y76tr")) {jdiewjdwjddewdewdw.setText("FireStoneElectric");
			} else {
				jdiewjdwjddewdewdw.setText("Invalid Password Entered");
			}

		}

	}

}
