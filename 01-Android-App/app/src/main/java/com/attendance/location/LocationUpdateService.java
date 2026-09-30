package com.attendance.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}