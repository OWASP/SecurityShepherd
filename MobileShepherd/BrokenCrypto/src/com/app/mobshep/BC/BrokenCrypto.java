package com.app.mobshep.BC;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class BrokenCrypto extends Activity{

	TextView messageOne, messageTwo, messageThree, messageFour, messageFive;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broken);
		referenceXML();
		startTimerOne();
		startTimerTwo();
		startTimerThree();
		startTimerFour();
		startTimerFive();
		
	}
		
		
	

	private void referenceXML() {
		// TODO Auto-generated method stub
		messageOne = (TextView)findViewById(R.id.tvMessage1);
		messageTwo = (TextView)findViewById(R.id.tvMessage2);
		messageThree = (TextView)findViewById(R.id.tvMessage3);
		messageFour = (TextView)findViewById(R.id.tvMessage4);
		messageFive = (TextView)findViewById(R.id.tvMessage5);
		messageOne.setVisibility(View.INVISIBLE);
		messageTwo.setVisibility(View.INVISIBLE);
		messageThree.setVisibility(View.INVISIBLE);
		messageFour.setVisibility(View.INVISIBLE);
		messageFive.setVisibility(View.INVISIBLE);


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
	                    Thread.sleep(4000);
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
	                    Thread.sleep(6000);
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
	
	private void startTimerFour() {
	    final Handler handler = new Handler();
	    Runnable runnable = new Runnable() {
	        public void run() {
	           
	                try {
	                    Thread.sleep(8000);
	                }    
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                handler.post(new Runnable(){
	                    public void run() {
	                    	messageFour.setVisibility(View.VISIBLE);
	                }
	            });
	            
	        }
	    };
	    new Thread(runnable).start();
	}
	
	private void startTimerFive() {
	    final Handler handler = new Handler();
	    Runnable runnable = new Runnable() {
	        public void run() {
	           
	                try {
	                    Thread.sleep(10000);
	                }    
	                catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                handler.post(new Runnable(){
	                    public void run() {
	                    	messageFive.setVisibility(View.VISIBLE);
	                }
	            });
	            
	        }
	    };
	    new Thread(runnable).start();
	}
	
	
	
	}


