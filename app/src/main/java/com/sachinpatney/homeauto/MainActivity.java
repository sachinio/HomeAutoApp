package com.sachinpatney.homeauto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sachinpatney.homeauto.notification.RegistrationIntentService;
import com.sachinpatney.homeauto.tasks.Devices;
import com.sachinpatney.homeauto.tasks.HomeDevice;
import com.sachinpatney.homeauto.tasks.HomeDeviceConnector;
import com.sachinpatney.homeauto.tasks.PostMessageTask;
import com.sachinpatney.homeauto.tasks.TaskHandler;
import com.sachinpatney.homeauto.tasks.UpdateImageTask;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private Handler mHandler = new Handler();
    private Handler doorHandler = new Handler();
    Runnable mStatusChecker;
    Runnable doorStatusChecker;
    boolean laserOn = false;
    boolean local = true;
    boolean paused = false;
    int camIp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                local = !local;
                SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
                String external_ip = settings.getString("external_ip", "None");
                String local_ip = settings.getString("local_ip", "None");
                HomeTempStorage.setIP(local ? local_ip : external_ip);
                Snackbar.make(view, "Switched to " + (local ? "Local" : "External"),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Start IntentService to register this application with GCM.
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        setLaserButtonColor();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
        whenPaused();
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        whenResumed();
    }

    void whenResumed() {
        ((Button) findViewById(R.id.garage))
                .setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.unknown));
        startImageFetcher();
        startDoorStateUpdater();
        getTemperature();
    }

    void whenPaused() {
        stopImageFetcher();
        stopDoorStateUpdater();
    }

    public void garage(View view) {
        HomeDeviceConnector.runAsync(new HomeDevice(Devices.GarageDoor), null);
    }

    public void laser(View view) {
        laserOn = !laserOn;
        setLaserButtonColor();
        HomeDeviceConnector.runAsync(new HomeDevice(Devices.Laser, laserOn ? "1" : "0"), null);
    }

    private void setLaserButtonColor() {
        ((Button) findViewById(R.id.laser))
                .setTextColor(ContextCompat.getColor(getApplicationContext(),
                        laserOn ? R.color.green : R.color.red));
    }

    void startImageFetcher() {
        if (mStatusChecker != null) {
            stopImageFetcher();
        }

        mStatusChecker = new Runnable() {
            @Override
            public void run() {
                new UpdateImageTask(new TaskHandler<Bitmap>() {
                    @Override
                    public void done(Bitmap result) {
                        if(result != null)
                            ((ImageView) findViewById(R.id.camera)).setImageBitmap(result);
                        if(!paused)
                            mHandler.postDelayed(mStatusChecker, 1000);
                    }
                }).execute(HomeTempStorage.getFlaskUrl() + "/camera?ip="+ (camIp+1));
            }
        };
        mStatusChecker.run();
    }

    void stopImageFetcher() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    void startDoorStateUpdater() {
        if (doorStatusChecker != null) {
            stopDoorStateUpdater();
        }

        doorStatusChecker = new Runnable() {
            @Override
            public void run() {
                HomeDeviceConnector.runAsync(new HomeDevice(Devices.GarageDoorState),
                        new TaskHandler<JSONObject>() {
                            @Override
                            public void done(JSONObject result) {
                                try {
                                    Log.i("Door State", result.getString("result"));
                                    int val = Integer.parseInt(result.getString("result"));

                                    ((Button) findViewById(R.id.garage))
                                            .setTextColor(ContextCompat.getColor(getApplicationContext(),
                                                    (val < 100) ? R.color.green : R.color.red));
                                } catch (Exception ex) {
                                    Log.e("UpdateDoorStateError", ex.getMessage());
                                }
                            }
                        });
                if(!paused)
                    doorHandler.postDelayed(doorStatusChecker, 12000);
            }
        };
        doorStatusChecker.run();
    }

    void stopDoorStateUpdater() {
        doorHandler.removeCallbacks(doorStatusChecker);
    }

    private void getTemperature() {
        HomeDeviceConnector.runAsync(new HomeDevice(Devices.Temperature),
                new TaskHandler<JSONObject>() {
                    @Override
                    public void done(JSONObject result) {
                        try {
                            Log.i("Temperature", result.getString("result"));
                            int val = Integer.parseInt(result.getString("result"));
                            ((TextView) findViewById(R.id.temp)).setText(val + "F");

                        } catch (Exception ex) {
                            Log.e("UpdateDoorStateError", ex.getMessage());
                        }
                    }
                });
    }

    public void camclick(View view){
        camIp = (++camIp)%2;
        Log.i("CAM IP", camIp+"");
    }
}
