package com.mobshep.UDataLeakage1;

import android.graphics.Canvas;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */

public class UDataLeakage1 extends ActionBarActivity implements View.OnClickListener {

    Button submit;
    CheckBox checkbox;
    EditText secret;
    private RelativeLayout relativeLayout;
    private Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        referenceXML();

        try {
            saveStaticImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        if (((CheckBox) view).isChecked()) {
            secret.setInputType(InputType.TYPE_CLASS_TEXT);
            // secret.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            secret.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            secret.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case (R.id.bSubmit):
                submitClicked();
                secret.setText(null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_license) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("License");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App is part of the Security Shepherd Project. The Security Shepherd project is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with the Security Shepherd project.  If not, see http://www.gnu.org/licenses.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }


        if (id == R.id.action_disclaimer) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Disclaimer");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App may collect logs via various methods. By using this App you agree to this.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return true;
        }


        if (id == R.id.action_exit){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    private void referenceXML() {
        submit = (Button) findViewById(R.id.bSubmit);
        secret = (EditText) findViewById(R.id.etSecret);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        submit.setOnClickListener(this);

    }
        private void submitClicked() {

            relativeLayout = (RelativeLayout)findViewById(R.id.mainLayout);
            relativeLayout.post(new Runnable() {
                public void run() {

                    //take screenshot
                    myBitmap = captureScreen(relativeLayout);

                    Toast.makeText(getApplicationContext(), "Message Posted!", Toast.LENGTH_LONG).show();

                    try {
                        if(myBitmap!=null){
                            //save image to SD card
                            saveImage(myBitmap);
                        }
                        Toast.makeText(getApplicationContext(), "Message saved!", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });
        }

    public static Bitmap captureScreen(View v) {

        Bitmap screenshot = null;
        try {

            if(v!=null) {

                screenshot = Bitmap.createBitmap(v.getMeasuredWidth(),v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(screenshot);
                v.draw(canvas);
            }

        }catch (Exception e){
            Log.d("ScreenShotActivity", "Failed to capture screenshot because:" + e.getMessage());
        }

        return screenshot;
    }

    public static void saveImage(Bitmap bitmap) throws IOException{

        Date date = new Date();

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        String filename = "Log" + uuid;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator  + filename + " .png");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
    }


    public void saveStaticImage() throws IOException{


        String filename = "Log0b10cb9b9244ce1e1cdc34";
        String destinationDir = this.getFilesDir().getParentFile().getPath()+"/files/";


        String destinationPath = destinationDir + "Log0b10cb9b9244ce1e1cdc34";

        File f = new File(destinationPath);

        if (!f.exists()) {
            File directory = new File(destinationDir);
            directory.mkdirs();

            try {
                copyImages(getBaseContext().getAssets().open("Log0b10cb9b9244ce1e1cdc34"),
                        new FileOutputStream(destinationPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void copyImages(InputStream iStream, OutputStream oStream)
            throws IOException {
        byte[] buffer = new byte[4096];
        int i;
        while ((i = iStream.read(buffer)) > 0) {
            oStream.write(buffer, 0, i);
        }
        iStream.close();
        oStream.close();
    }

    }


