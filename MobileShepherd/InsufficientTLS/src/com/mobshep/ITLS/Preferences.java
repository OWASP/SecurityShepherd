package com.mobshep.ITLS;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
 
public class Preferences extends PreferenceActivity {
 
 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
    new PrefsFragment()).commit();
  PreferenceManager.setDefaultValues(Preferences.this, R.layout.preferences, false);
   
    }
  
 public static class PrefsFragment extends PreferenceFragment {
   
  @Override
  public void onCreate(Bundle savedInstanceState) {
    
   super.onCreate(savedInstanceState);
   addPreferencesFromResource(R.layout.preferences);
  }
 }
  
}
