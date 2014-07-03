package com.app.mobshep.BC;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

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

public class BrokenCrypto extends Activity{

	EditText messageOne, messageTwo, messageThree, messageFour, messageFive;
	


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
		messageOne = (EditText)findViewById(R.id.tvMessage1);
		messageTwo = (EditText)findViewById(R.id.tvMessage2);
		messageThree = (EditText)findViewById(R.id.tvMessage3);
		messageFour = (EditText)findViewById(R.id.tvMessage4);
		messageFive = (EditText)findViewById(R.id.tvMessage5);
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


