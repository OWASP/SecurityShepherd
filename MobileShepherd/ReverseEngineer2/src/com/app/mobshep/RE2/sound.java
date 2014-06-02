package com.app.mobshep.RE2;

import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;

public class sound extends Activity {
	SoundPool sp;
	int horn = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View v = new View(this);
		v.setOnClickListener((android.view.View.OnClickListener) this);
		setContentView(v);
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		//Include that sound file/
	}
	
	public void onClick(View v)
	{
		sp.play(horn, 1, 1, 0, 0, 1);
	}

}
