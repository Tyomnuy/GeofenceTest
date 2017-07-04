package com.vail.myapplication;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Vail on 04.07.17
 */

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;
    private SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private ArrayList<Geofence> mGeofenceList;

    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    public MainPresenter(MainContract.View view, SharedPreferences sharedPreferences) {
        this.view = view;
        this.sharedPreferences = sharedPreferences;

        mGeofenceList = new ArrayList<>();
        populateGeofenceList();
        view.setButtonsEnabledState(getGeofencesAdded());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
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
        if (!view.checkPermissions()) {
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            performPendingGeofenceTask();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            view.addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            view.removeGeofences();
        }
    }

    @Override
    public void onRemoveGeofencesClick() {
        if (!view.checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
            return;
        }
        view.removeGeofences();
    }

    @Override
    public void onAddGeofencesClick() {
        if (!view.checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            view.requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
            return;
        }
        view.addGeofences();
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
        //TODO
        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    @Override
    public GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();
    }
}
