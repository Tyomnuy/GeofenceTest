package com.vail.myapplication;

import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;

/**
 * Created by Vail on 04.07.17
 */

public class MainContract {

    interface View  {
        void setButtonsEnabledState(boolean geofencesAdded);

        void showPermissionDeniedSnackbar();

        boolean checkPermissions();

        void requestPermissions(int requestPermissionsRequestCode);

        void addGeofences();

        void removeGeofences();

        void showToast(int messageId);
    }

    interface Presenter extends OnMapReadyCallback, OnCompleteListener<Void> {
        void onStart();

        void onAddGeofencesClick();

        GeofencingRequest getGeofencingRequest();

        void onRemoveGeofencesClick();

        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }
}
