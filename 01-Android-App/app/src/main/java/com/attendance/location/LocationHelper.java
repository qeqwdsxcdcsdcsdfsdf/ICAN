package com.attendance.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class LocationHelper {
    private static final int LOCATION_TIMEOUT = 30000;
    private Context mContext;
    private LocationManager mLocationManager;
    private LocationCallbackListener mCallback;
    private LocationCallback mSimpleCallback;
    private Handler mHandler;
    private boolean mIsTimeout = false;

    public LocationHelper(Context context) {
        this.mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public LocationHelper(Context context, LocationCallback callback) {
        this(context);
        this.mSimpleCallback = callback;
    }

    public void requestLocation(LocationCallbackListener callback) {
        mCallback = callback;
        mIsTimeout = false;

        if (!hasLocationPermission()) {
            mCallback.onLocationFailed("定位权限未授权");
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsTimeout) {
                    mIsTimeout = true;
                    mCallback.onLocationFailed("定位超时");
                }
            }
        }, LOCATION_TIMEOUT);

        try {
            Location lastKnownGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastKnownNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location bestLocation = getBestLocation(lastKnownGPS, lastKnownNetwork);
            if (bestLocation != null) {
                mIsTimeout = true;
                mCallback.onLocationSuccess(bestLocation.getLatitude(), bestLocation.getLongitude());
                return;
            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        } catch (SecurityException e) {
            mCallback.onLocationFailed("定位权限获取失败");
        }
    }

    public void stopLocation() {
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private Location getBestLocation(Location gpsLocation, Location networkLocation) {
        if (gpsLocation == null) return networkLocation;
        if (networkLocation == null) return gpsLocation;

        long gpsTime = gpsLocation.getTime();
        long networkTime = networkLocation.getTime();
        long timeDiff = Math.abs(gpsTime - networkTime);

        if (timeDiff < 60000) {
            return gpsLocation.getAccuracy() < networkLocation.getAccuracy() ? gpsLocation : networkLocation;
        } else {
            return gpsTime > networkTime ? gpsLocation : networkLocation;
        }
    }

    public void startLocationUpdates() {
        if (mSimpleCallback != null) {
            requestLocation(new LocationCallbackListener() {
                @Override
                public void onLocationSuccess(double lat, double lng) {
                    mSimpleCallback.onLocationChanged(lat, lng);
                }

                @Override
                public void onLocationFailed(String error) {
                    mSimpleCallback.onError(error);
                }
            });
        }
    }

    public void stopLocationUpdates() {
        stopLocation();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (!mIsTimeout && location != null) {
                mIsTimeout = true;
                mCallback.onLocationSuccess(location.getLatitude(), location.getLongitude());
                stopLocation();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    public interface LocationCallback {
        void onLocationChanged(double lat, double lng);
        void onError(String error);
    }
}