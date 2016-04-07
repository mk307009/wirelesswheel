package pl.m4.wirelesswheel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MobileActivity extends AppCompatActivity {
    public static final String TAG = "MobileActivity";
    public static final String ADDRESS_SHARED_KEY = "address";
    public static final String ADDRESS_DEFAULT_VALUE = "localhost:8833";
    private TcpClient client;
    private Accelerometer accelerator;
    PreferencesManager preferences;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);
        preferences = new PreferencesManager(this);
        client = new TcpClient();
        address = preferences.loadSavedPreferences(ADDRESS_SHARED_KEY, ADDRESS_DEFAULT_VALUE);
        client.connect(this, address);
        accelerator = new Accelerometer(this, client);
    }

    @Override
    protected void onResume() {
        accelerator.registerSensor();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        accelerator.unregisterSensor();
        client.disconnect(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wheel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_address) {
            makeAlertMessage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(View view) {
        address = preferences.loadSavedPreferences(ADDRESS_SHARED_KEY, ADDRESS_DEFAULT_VALUE);
        client.connect(this, address);
    }

    public void disconnect(View view){
        client.disconnect(this);
    }

    public void calibrate(View view){
        accelerator.calibrate();
    }

    /**
     * function to create alert dialog window with input.
     */
    public void makeAlertMessage(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View ipView = layoutInflater.inflate(R.layout.ip_settings, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set ip_settings.xml to be the layout file of the alertdialog builder
        alertDialogBuilder.setView(ipView);
        final EditText input = (EditText) ipView.findViewById(R.id.userInput);
        input.setText(preferences.loadSavedPreferences(ADDRESS_SHARED_KEY, ADDRESS_DEFAULT_VALUE));
        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        preferences.savePreferences(ADDRESS_SHARED_KEY,input.getText().toString());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,	int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }
}
