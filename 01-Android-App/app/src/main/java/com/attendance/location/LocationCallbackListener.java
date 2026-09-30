package com.attendance.location;

public interface LocationCallbackListener {
    void onLocationSuccess(double latitude, double longitude);
    void onLocationFailed(String errorMessage);
}