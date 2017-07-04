package com.vail.myapplication;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.model.LatLng;
import com.vail.myapplication.wifi.WifiSensor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Mock
    private MainContract.View mainView;
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private WifiSensor wifiSensor;
    @Mock
    private GeofencingClient geofencingClient;

    private MainPresenter mainPresenter;


    @Before
    public void setupMocksAndView() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCheckPermission() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(mainView.checkPermissions()).thenReturn(false);
        mainPresenter.onStart();
        verify(mainView).requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Test
    public void testCheckPermission_onAddGeofencesClick() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(mainView.checkPermissions()).thenReturn(false);
        mainPresenter.onAddGeofencesClick();
        verify(mainView).requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Test
    public void testCheckPermission_onRemoveGeofencesClick() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(mainView.checkPermissions()).thenReturn(false);
        mainPresenter.onRemoveGeofencesClick();
        verify(mainView).requestPermissions(REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Test
    public void testRequestPermissionFailed() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        mainPresenter.onRequestPermissionsResult(REQUEST_PERMISSIONS_REQUEST_CODE, new String[]{},
                new int[]{PackageManager.PERMISSION_DENIED});
        verify(mainView).showPermissionDeniedSnackbar();
    }

    @Test
    public void testWifiButtonClick_noResults() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(wifiSensor.getScanResults()).thenReturn(new ArrayList<ScanResult>());
        mainPresenter.onWifiButtonClick();
        verify(mainView).showToast(R.string.no_wifi_points);
    }

    @Test
    public void testWifiButtonClick_showDialog() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        List<ScanResult> scanResults = Arrays.asList(mock(ScanResult.class));
        when(wifiSensor.getScanResults()).thenReturn(scanResults);
        mainPresenter.onWifiButtonClick();
        verify(mainView).showListDialog(scanResults);
    }

    @Test
    public void testOnMapReady_hasSavedLocation() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(sharedPreferences.contains(any(String.class))).thenReturn(true);
        when(sharedPreferences.getString(any(String.class), any(String.class))).thenReturn("0");
        mainPresenter.onMapReady();
        verify(mainView).navigateMap(new LatLng(0,0));
    }

    @Test
    public void testOnMapReady_noSavedLocation() {
        mainPresenter = new MainPresenter(mainView, sharedPreferences, wifiSensor, geofencingClient);
        when(sharedPreferences.contains(any(String.class))).thenReturn(false);
        mainPresenter.onMapReady();
        verify(mainView).updateMarker();
    }
}