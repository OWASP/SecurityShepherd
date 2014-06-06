package com.app.mobshep.ITLS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InsufficientTLS extends Activity implements OnClickListener {

	Button send;
	Button Config;
	EditText key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		referenceXML();

	}

	private void referenceXML() {
		// TODO Auto-generated method stub
		send = (Button) findViewById(R.id.bSend);
		Config=(Button)findViewById(R.id.bConfig);
		send.setOnClickListener(this);
		Config.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		
		case (R.id.bConfig):
			
			
			Intent gotoMain = new Intent(this, Preferences.class);
			startActivity(gotoMain);
			
			break;
		
		case (R.id.bSend):
			
			
			
			
			//send the message to be intercepted to the Shepherd server
			
			
			Toast sendingMessage = Toast.makeText(InsufficientTLS.this,
					"Sent Message!", Toast.LENGTH_SHORT);
			sendingMessage.show();
			

		}
	}
}
