package com.app.mobshep.BC2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

public class BrokenCrypto extends Activity{

	EditText messageOne, messageTwo, messageThree;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		referenceXML();
		
String destinationDir = "/data/data/" +getPackageName() + "/encrypt/";
		
		String destinationPath = destinationDir + "desKey";
		
		File f = new File(destinationPath);
		
		if (!f.exists()){
			File directory = new File(destinationDir);
			directory.mkdirs();
			//assets members.db -> /databases/
			
			try{
				copyKey(getBaseContext().getAssets().open("desKey"), new FileOutputStream(destinationPath));
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
		}
		
		startTimerOne();
		startTimerTwo();
		startTimerThree();
		
	}
		
		
	

	private void referenceXML() {
		// TODO Auto-generated method stub
		messageOne = (EditText)findViewById(R.id.tvMessage1);
		messageTwo = (EditText)findViewById(R.id.tvMessage2);
		messageThree = (EditText)findViewById(R.id.tvMessage3);
		messageOne.setVisibility(View.INVISIBLE);
		messageTwo.setVisibility(View.INVISIBLE);
		messageThree.setVisibility(View.INVISIBLE);


	}
	
	private void startTimerOne() {
	    final Handler handler = new Handler();
	    Runnable runnable = new Runnable() {
	        public void run() {
	           
	                try {
	                    Thread.sleep(2000);
	                }    
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                handler.post(new Runnable(){
	                    public void run() {
	                    	messageOne.setVisibility(View.VISIBLE);
	                }
	            });
	            
	        }
	    };
	    new Thread(runnable).start();
	}

	private void startTimerTwo() {
	    final Handler handler = new Handler();
	    Runnable runnable = new Runnable() {
	        public void run() {
	           
	                try {
	                    Thread.sleep(5000);
	                }    
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                handler.post(new Runnable(){
	                    public void run() {
	                    	messageTwo.setVisibility(View.VISIBLE);
	                }
	            });
	            
	        }
	    };
	    new Thread(runnable).start();
	}
	
	private void startTimerThree() {
	    final Handler handler = new Handler();
	    Runnable runnable = new Runnable() {
	        public void run() {
	           
	                try {
	                    Thread.sleep(7000);
	                }    
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                handler.post(new Runnable(){
	                    public void run() {
	                    	messageThree.setVisibility(View.VISIBLE);
	                }
	            });
	            
	        }
	    };
	    new Thread(runnable).start();
	}
	
	public void copyKey(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
				oStream.write(buffer, 0 , i);
		}
		iStream.close();
		oStream.close();
		
	}
	
	}


