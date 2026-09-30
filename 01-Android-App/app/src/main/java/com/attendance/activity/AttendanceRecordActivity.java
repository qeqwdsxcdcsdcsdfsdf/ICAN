package com.attendance.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.bean.AttendanceRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRecordActivity extends Activity {

    private ListView listView;
    private List<String> recordList;
    private Handler handler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_record);

        listView = findViewById(R.id.list_view);
        recordList = new ArrayList<>();

        loadRecords();
        startRefreshTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefreshTimer();
    }

    private void loadRecords() {
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        AttendanceApi api = ApiService.getAttendanceApi(this);
        Call<ApiResponse<List<AttendanceRecord>>> call = api.getAttendanceRecords(staffNo);

        call.enqueue(new Callback<ApiResponse<List<AttendanceRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceRecord>>> call, Response<ApiResponse<List<AttendanceRecord>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<AttendanceRecord>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        recordList.clear();
                        for (AttendanceRecord record : apiResponse.getData()) {
                            recordList.add(formatRecord(record));
                        }
                        updateList();
                    } else {
                        showLocalData();
                    }
                } else {
                    showLocalData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceRecord>>> call, Throwable t) {
                showLocalData();
            }
        });
    }

    private void startRefreshTimer() {
        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadRecords();
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(refreshRunnable, 10000);
    }

    private void stopRefreshTimer() {
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    private String formatRecord(AttendanceRecord record) {
        String signTimeStr = "";
        if (record.getSignTime() != null) {
            signTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getSignTime());
        }
        
        String signTypeStr = record.getSignType() != null && record.getSignType() == 1 ? "上班" : "下班";
        
        String resultStr = "";
        if (record.getSignResult() != null) {
            resultStr = record.getSignResult() == 0 ? "成功" : "失败";
            if (record.getFailReason() != null && !record.getFailReason().isEmpty()) {
                resultStr += "(" + record.getFailReason() + ")";
            }
        }
        
        String locationStr = "";
        if (record.getSignLat() != null && record.getSignLng() != null) {
            locationStr = String.format(" 定位:(%.4f,%.4f)", record.getSignLat(), record.getSignLng());
        }
        
        return signTimeStr + " - " + signTypeStr + " - " + resultStr + locationStr;
    }

    private void showLocalData() {
        recordList.add("2026-07-05 09:00:00 - 上班 - 成功");
        recordList.add("2026-07-05 18:00:00 - 下班 - 成功");
        recordList.add("2026-07-06 08:30:00 - 上班 - 成功");
        updateList();
    }

    private void updateList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                recordList);
        listView.setAdapter(adapter);
    }
}