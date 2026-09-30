package com.attendance;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {

    private static final String TAG = "App";
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setupExceptionHandler();
        Log.d(TAG, "Application onCreate completed");
    }

    public static App getInstance() {
        return instance;
    }

    private void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception on thread: " + thread.getName(), throwable);
            
            StringBuilder sb = new StringBuilder();
            sb.append("App Crash!\n");
            sb.append("Thread: ").append(thread.getName()).append("\n");
            sb.append("Message: ").append(throwable.getMessage()).append("\n");
            
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 20); i++) {
                sb.append("\tat ").append(stackTrace[i].toString()).append("\n");
            }
            
            Log.e(TAG, "Crash Details:\n" + sb.toString());
            
            try {
                Toast.makeText(this, "应用崩溃: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to show toast", e);
            }
        });
    }
}
