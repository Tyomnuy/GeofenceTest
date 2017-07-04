package com.vail.myapplication;

import android.app.PendingIntent;
import android.net.wifi.ScanResult;

import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.List;

/**
 * Created by Vail on 04.07.17
 */

public class MainContract {

    interface View  {
        void setButtonsEnabledState(boolean geofencesAdded);

        void showPermissionDeniedSnackbar();

        boolean checkPermissions();

        void requestPermissions(int requestPermissionsRequestCode);

        void showToast(int messageId);

        void showSnackbar(int stringId);

        PendingIntent getGeofencePendingIntent();

        void showListDialog(List<ScanResult> resultList);

        void setWifiName(String ssid);
    }

    interface Presenter extends OnMapReadyCallback, OnCompleteListener<Void> {
        void onStart();

        void onAddGeofencesClick();

        GeofencingRequest getGeofencingRequest();

        void onRemoveGeofencesClick();

        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

        void onWifiButtonClick();

        void onSelectItem(ScanResult scanResult);
    }
}
