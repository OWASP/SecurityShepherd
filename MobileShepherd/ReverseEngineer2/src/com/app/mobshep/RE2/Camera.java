package com.app.mobshep.RE2;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Camera extends Activity implements View.OnClickListener
{
	ImageView iv;
	Button b;
	ImageButton ib;
	Intent intent;
	final static int cameraData = 0;
	Bitmap bmp;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reverse);
		InputStream is = getResources().openRawResource(R.drawable.ic_launcher);

		bmp = BitmapFactory.decodeStream(is); 

	}
	
	
	

				
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK )
		{
			Bundle extras = data.getExtras();
			bmp = (Bitmap) extras.get("data");
			iv.setImageBitmap(bmp);
		}
	}





	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

}
