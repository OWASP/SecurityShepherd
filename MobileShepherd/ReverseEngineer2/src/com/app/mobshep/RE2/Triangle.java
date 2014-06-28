package com.app.mobshep.RE2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
																																															if (Check.contentEquals("Winterbones")) {jdiewjdwjddewdewdw.setText("FireStoneElectric");
			} else {
				jdiewjdwjddewdewdw.setText("Invalid Password Entered");
			}

		}

	}

}
