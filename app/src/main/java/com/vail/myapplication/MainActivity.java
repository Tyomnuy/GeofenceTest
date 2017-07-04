package com.vail.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.vail.myapplication.geofencing.GeofenceTransitionsIntentService;
import com.vail.myapplication.wifi.WifiSensor;

import java.util.List;

public class MainActivity extends FragmentActivity implements MainContract.View, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainContract.Presenter presenter;

    private PendingIntent mGeofencePendingIntent;

    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
    private TextView wifiNameTv;
    private TextView radiusTv;
    private SeekBar radiusSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        presenter = new MainPresenter(this, preferences, new WifiSensor(this, preferences),
                LocationServices.getGeofencingClient(this));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(presenter);

        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
        wifiNameTv = (TextView) findViewById(R.id.wifi_name);
        radiusTv = (TextView) findViewById(R.id.radius_tv);
        radiusSeekBar = (SeekBar) findViewById(R.id.seekBar);
        radiusSeekBar.setOnSeekBarChangeListener(this);
        radiusSeekBar.setMax(500);

        mGeofencePendingIntent = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.onStart();
    }

    public void addGeofencesButtonHandler(View view) {
        presenter.onAddGeofencesClick();
    }

    public void removeGeofencesButtonHandler(View view) {
        presenter.onRemoveGeofencesClick();
    }

    public void selectWifiButton(View view) {
        presenter.onWifiButtonClick();
    }

    @Override
    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void setButtonsEnabledState(boolean geofencesEnabled) {
        mAddGeofencesButton.setEnabled(geofencesEnabled);
        mRemoveGeofencesButton.setEnabled(!geofencesEnabled);
    }

    @Override
    public void showSnackbar(final int stringId) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, getString(stringId), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    public void showSnackbar(final int mainTextStringId, final int actionStringId,
                             View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    @Override
    public boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermissions(final int requestCode) {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    requestCode);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestCode);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPermissionDeniedSnackbar() {
        showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void showListDialog(final List<ScanResult> scanResults) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(R.string.select_wifi);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        for (ScanResult scanResult: scanResults) {
            arrayAdapter.add(scanResult.SSID);
        }

        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.onSelectItem(scanResults.get(which));
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    @Override
    public void setWifiName(String ssid) {
        wifiNameTv.setText(ssid);
    }

    @Override
    public void setRadius(int radius) {
        radiusTv.setText(String.valueOf(radius));
        radiusSeekBar.setProgress(radius - Constants.MIN_RADIUS);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int radius = Constants.MIN_RADIUS + progress;
        radiusTv.setText(String.valueOf(radius));
        presenter.onRadiusChanged(radius);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}