# GeofenceTest
Create geofence area and select Wifi network to detect when you enter or exit area.
Application using [GeofencingClient](https://developers.google.com/android/reference/com/google/android/gms/location/GeofencingClient) to monitor geofence changes and [WifiManager.getScamResults()](https://developer.android.com/reference/android/net/wifi/WifiManager.html#getScanResults()) to retrieve available Wifi points. 

Pre-requisites
--------------

- Android SDK 25
- Android Build Tools v25.0.3
- Android Support Repository


Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

If you want to use your google_maps_key, change it in [google_maps_api.xml](https://github.com/Tyomnuy/GeofenceTest/blob/master/app/src/debug/res/values/google_maps_api.xml)
