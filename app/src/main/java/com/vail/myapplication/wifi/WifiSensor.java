package com.vail.myapplication.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.vail.myapplication.Constants;
import com.vail.myapplication.geofencing.GeofenceTransitionsIntentService;

import java.util.List;

/**
 * Created by Vail on 04.07.17
 */
public class WifiSensor extends BroadcastReceiver {

    public static final String WIFI_ACCESSIBILITY_KEY = "WIFI_ACCESSIBILITY";
    private final WifiManager wifiManager;

    private Context context;
    private SharedPreferences sharedPreferences;

    public WifiSensor(Context context, SharedPreferences sharedPreferences) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = sharedPreferences;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(this, intentFilter);

        wifiManager.startScan();
    }

    public void stop() {
        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateCurrentWifiConnection();
    }

    private void updateCurrentWifiConnection() {
        String savedWifiBSSID = sharedPreferences.getString(Constants.WIFI_BSSID_KEY, null);
        if (TextUtils.isEmpty(savedWifiBSSID)) return;


        List<ScanResult> scanResultList = wifiManager.getScanResults();
        for (ScanResult scanResult: scanResultList) {
            if (savedWifiBSSID.equals(scanResult.BSSID)) {
                notifyWifiAccessibilityChanged(true);
                return;
            }
        }

        notifyWifiAccessibilityChanged(false);
    }

    public List<ScanResult> getScanResults() {
        return wifiManager.getScanResults();
    }

    private void notifyWifiAccessibilityChanged(boolean available) {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        intent.putExtra(WIFI_ACCESSIBILITY_KEY, available);
        context.startService(intent);
    }
}