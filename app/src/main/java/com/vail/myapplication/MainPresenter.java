package com.vail.myapplication;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.vail.myapplication.wifi.WifiSensor;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Vail on 04.07.17
 */

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private SharedPreferences sharedPreferences;
    private WifiSensor wifiSensor;
    private GeofencingClient geofencingClient;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    private int radius;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private ArrayList<Geofence> mGeofenceList;

    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    public MainPresenter(MainContract.View view, SharedPreferences sharedPreferences,
                         WifiSensor wifiSensor, GeofencingClient geofencingClient) {
        this.view = view;
        this.sharedPreferences = sharedPreferences;
        this.wifiSensor = wifiSensor;
        this.geofencingClient = geofencingClient;

        radius = sharedPreferences.getInt(Constants.RADIUS_KEY, 30);
        mGeofenceList = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                view.enableMyLocation();
                performPendingGeofenceTask();
            } else {
                // Permission denied.
                view.showPermissionDeniedSnackbar();
                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }

    @Override
    public void onStart() {
        view.setWifiName(sharedPreferences.getString(Constants.WIFI_NAME_KEY, "None"));
        view.setRadius(radius);
        view.setButtonsEnabledState(getGeofencesAdded());

        if (!view.checkPermissions()) {
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            performPendingGeofenceTask();
        }
    }

    @SuppressWarnings("MissingPermission")
    public void addGeofences() {
        if (!view.checkPermissions()) {
            view.showSnackbar(R.string.insufficient_permissions);
            return;
        }

        wifiSensor.start();
        geofencingClient.addGeofences(getGeofencingRequest(), view.getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    public void removeGeofences() {
        if (!view.checkPermissions()) {
            view.showSnackbar(R.string.insufficient_permissions);
            return;
        }

        wifiSensor.stop();
        view.setWifiName("None");
        sharedPreferences.edit()
                .remove(Constants.WIFI_NAME_KEY)
                .remove(Constants.WIFI_BSSID_KEY)
                .apply();
        geofencingClient.removeGeofences(view.getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }

    @Override
    public void onRemoveGeofencesClick() {
        if (!view.checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
            return;
        }
        removeGeofences();
    }

    @Override
    public void onAddGeofencesClick() {
        if (!view.checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
            return;
        }
        addGeofences();
    }

    @Override
    public void onWifiButtonClick() {
        List<ScanResult> resultList = wifiSensor.getScanResults();
        if (resultList.isEmpty()) {
            view.showToast(R.string.no_wifi_points);
        } else {
            view.showListDialog(resultList);
        }
    }

    @Override
    public void onSelectItem(ScanResult scanResult) {
        String ssid = scanResult.SSID;
        String bssid = scanResult.BSSID;
        sharedPreferences.edit()
                .putString(Constants.WIFI_NAME_KEY, ssid)
                .putString(Constants.WIFI_BSSID_KEY, bssid)
                .apply();

        view.setWifiName(ssid);
    }

    @Override
    public void onRadiusChanged(int radius) {
        this.radius = radius;
        view.updateMarker();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            view.setButtonsEnabledState(getGeofencesAdded());

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            view.showToast(messageId);
        }
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        sharedPreferences
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return sharedPreferences.getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }

    private void populateGeofenceList() {
        mGeofenceList.clear();
        LatLng latLng = view.getLatLng();
        if (latLng == null) return;

        sharedPreferences.edit()
                .putString(Constants.LATITUDE_KEY, String.valueOf(latLng.latitude))
                .putString(Constants.LONGITUDE_KEY, String.valueOf(latLng.longitude))
                .putInt(Constants.RADIUS_KEY, radius)
                .apply();

        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("First")
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        radius
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    @Override
    public GeofencingRequest getGeofencingRequest() {
        populateGeofenceList();
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();
    }

    @Override
    public void onMapReady() {
        if (sharedPreferences.contains(Constants.LATITUDE_KEY)) {
            double latitude = Double.parseDouble(sharedPreferences.getString(Constants.LATITUDE_KEY,"0"));
            double longitude = Double.parseDouble(sharedPreferences.getString(Constants.LONGITUDE_KEY,"0"));
            view.navigateMap(new LatLng(latitude, longitude));
        } else {
            view.updateMarker();
        }

        if (!view.checkPermissions()) {
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
            return;
        }

        view.enableMyLocation();
    }

    @Override
    public void onCameraPositionChanged() {
        view.updateMarker();
    }
}
