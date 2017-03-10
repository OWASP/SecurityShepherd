package com.mobshep.mobileshepherd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BrokenCrypto3 extends MainActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broken_layout3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        generateKey();
        generateDB(this);

        String destinationDir = this.getFilesDir().getParentFile().getPath() + "/crypto/";

        String destinationPath = destinationDir + "key.txt";

        File f = new File(destinationPath);

        if (!f.exists()) {
            File directory = new File(destinationDir);
            directory.mkdirs();

            try {
                copyDatabase(getBaseContext().getAssets().open("key.txt"),
                        new FileOutputStream(destinationPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ids) {
            Intent gotoIDS = new Intent(BrokenCrypto3.this, Insecure_Data_Storage.class);
            startActivity(gotoIDS);
        } else if (id == R.id.nav_ids1) {
            Intent gotoIDS1 = new Intent(BrokenCrypto3.this, Insecure_Data_Storage1.class);
            startActivity(gotoIDS1);
        } else if (id == R.id.nav_ids2) {
            Intent gotoIDS2 = new Intent(BrokenCrypto3.this, Insecure_Data_Storage2.class);
            startActivity(gotoIDS2);
        } else if (id == R.id.nav_ids3) {
            Intent gotoIDS3 = new Intent(BrokenCrypto3.this, ids3Login.class);
            startActivity(gotoIDS3);
        }else if (id == R.id.nav_bc) {
            Intent gotoBC = new Intent(BrokenCrypto3.this, BrokenCrypto.class);
            startActivity(gotoBC);
        }else if (id == R.id.nav_bc1) {
            Intent gotoBC1 = new Intent(BrokenCrypto3.this, BrokenCrypto1.class);
            startActivity(gotoBC1);
        }else if (id == R.id.nav_bc2) {
            Intent gotoBC2 = new Intent(BrokenCrypto3.this, BrokenCrypto2.class);
            startActivity(gotoBC2);
        }else if (id == R.id.nav_bc3) {
            Intent gotoBC3 = new Intent(BrokenCrypto3.this, BrokenCrypto3.class);
            startActivity(gotoBC3);
        }else if (id == R.id.nav_csi) {
            Intent gotoCSI = new Intent(BrokenCrypto3.this, CSInjection.class);
            startActivity(gotoCSI);
        }else if (id == R.id.nav_csi1) {
            Intent gotoCSI1 = new Intent(BrokenCrypto3.this, CSInjection.class);
            startActivity(gotoCSI1);
        }else if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(BrokenCrypto3.this, CSInjection.class);
            startActivity(gotoCSI2);
        } if (id == R.id.nav_csi2) {
            Intent gotoCSI2 = new Intent(BrokenCrypto3.this, CSInjection.class);
            startActivity(gotoCSI2);
        }if (id == R.id.nav_udl) {
            Intent gotoUDL = new Intent(BrokenCrypto3.this, UDataLeakage.class);
            startActivity(gotoUDL);
        }if (id == R.id.nav_udl1) {
            Intent gotoUDL1 = new Intent(BrokenCrypto3.this, UDataLeakage1.class);
            startActivity(gotoUDL1);
        }if (id == R.id.nav_udl2) {
            Intent gotoUDL2 = new Intent(BrokenCrypto3.this, UnintendedDataLeakage2.class);
            startActivity(gotoUDL2);
        }if (id == R.id.nav_pl) {
            Intent gotoPL = new Intent(BrokenCrypto3.this, providerLeakage.class);
            startActivity(gotoPL);

        }else if (id == R.id.nav_scoreboard) {
            //link to shepherd or webview?
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void copyDatabase(InputStream iStream, OutputStream oStream)
            throws IOException {
        byte[] buffer = new byte[1024];
        int i;
        while ((i = iStream.read(buffer)) > 0) {
            oStream.write(buffer, 0, i);
        }
        iStream.close();
        oStream.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement



        if (id == R.id.action_exit)
        {
            finish();
        }

        if (id == R.id.action_info) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Information");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This App has encrypted it's data using SQLCipher! This will stop hackers from stealing data on this App.")
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


        return super.onOptionsItemSelected(item);
    }



    public void generateDB(Context context) {
        try {
            SQLiteDatabase.loadLibs(context);

            String dbPath = context.getDatabasePath("key4.db").getPath();

            File dbPathFile = new File(dbPath);
            if (!dbPathFile.exists())
                dbPathFile.getParentFile().mkdirs();

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath,
                    "Pa88w0rd1234", null);

            db.execSQL("DROP TABLE IF EXISTS key4");
            db.execSQL("CREATE TABLE key4(key VARCHAR)");

            db.execSQL("INSERT INTO key4 VALUES('The Key is ShaveTheSkies.')");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Toast error = Toast.makeText(BrokenCrypto3.this,
                    "An error occurred.", Toast.LENGTH_LONG);
            error.show();

        }

    }

    public void generateKey()
    {
        String destinationDir = this.getFilesDir().getParentFile().getPath() + "/crypto/encrypt/";

        String destinationPath = destinationDir + "key";


        File f = new File(destinationPath);

        if (!f.exists()) {
            File directory = new File(destinationDir);
            directory.mkdirs();

            try {
                copyKey(getBaseContext().getAssets().open("key"),
                        new FileOutputStream(destinationPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void copyKey(InputStream iStream, OutputStream oStream)
            throws IOException {
        byte[] buffer = new byte[1024];
        int i;
        while ((i = iStream.read(buffer)) > 0) {
            oStream.write(buffer, 0, i);
        }
        iStream.close();
        oStream.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
