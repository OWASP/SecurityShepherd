package com.app.mobshep.RE2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class miuewonmxiwohnx extends Activity implements OnClickListener {

	TabHost wqiuoeuwoiqjmxxk;
	TextView jdiewjdwjddewdewdw;
	Button joiuwhiunwjdewniuewnmiux;
	EditText iourewsdxc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverse);
		wqiuoeuwoiqjmxxk = (TabHost) findViewById(R.id.tabhost);

		joiuwhiunwjdewniuewnmiux = (Button) findViewById(R.id.bRevealKey);
		iourewsdxc = (EditText) findViewById(R.id.etPassword);
		jdiewjdwjddewdewdw = (TextView) findViewById(R.id.tvKey1);

		joiuwhiunwjdewniuewnmiux.setOnClickListener(this);

		wqiuoeuwoiqjmxxk.setup();

		// Set up each tab
		TabSpec djiuwqjniuwqnwq = wqiuoeuwoiqjmxxk.newTabSpec("tag1");
		djiuwqjniuwqnwq.setContent(R.id.tab1);
		djiuwqjniuwqnwq.setIndicator("Summary"); 
		wqiuoeuwoiqjmxxk.addTab(djiuwqjniuwqnwq);

		djiuwqjniuwqnwq = wqiuoeuwoiqjmxxk.newTabSpec("tag2");
		djiuwqjniuwqnwq.setContent(R.id.tab2);
		djiuwqjniuwqnwq.setIndicator("Key");
		wqiuoeuwoiqjmxxk.addTab(djiuwqjniuwqnwq);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bRevealKey:
			String Check = iourewsdxc.getText().toString();
			jdiewjdwjddewdewdw.setText(Check);
																																															if (Check.contentEquals("Winterbones")) {jdiewjdwjddewdewdw.setText("FireStone32Electric11");
			} else {
				jdiewjdwjddewdewdw.setText("Invalid Password Entered");
			}

		}

	}

}
