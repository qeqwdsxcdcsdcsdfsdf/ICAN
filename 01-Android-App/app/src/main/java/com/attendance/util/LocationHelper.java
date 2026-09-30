package com.attendance.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public interface LocationCallback {
        void onLocationResult(double latitude, double longitude);
        void onLocationFailed(String error);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED 
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            callback.onLocationFailed("定位权限未授予");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "Location found: lat=" + location.getLatitude() + ", lng=" + location.getLongitude());
                            callback.onLocationResult(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.w(TAG, "Location is null");
                            callback.onLocationFailed("无法获取定位信息");
                        }
                    }
                });
    }
}
