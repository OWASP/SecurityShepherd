package com.mobshep.shepherdlogin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

public class LoggedIn extends Activity {

    SharedPreferences storedPref;
    SharedPreferences.Editor toEdit;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loggedin);

        checkNullSession();
    }

    public void getKeyClicked() {
            //return the key
    }

    private void checkNullSession() {

        SharedPreferences prefs = this.getSharedPreferences("Sessions", MODE_PRIVATE);
        String sessionId = prefs.getString("sessionId", "null");

        if (sessionId.equals("null")){
            finish();
            Intent intent = new Intent(LoggedIn.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void logoutClicked(View v){

        storedPref = getSharedPreferences("Sessions", MODE_PRIVATE);
        toEdit = storedPref.edit();
        toEdit.clear();
        toEdit.commit();

        //delete * from sessions table



        checkNullSession();


    }
}
