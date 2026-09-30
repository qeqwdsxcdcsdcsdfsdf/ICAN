package com.attendance.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.bean.Staff;

import android.util.Base64;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends Activity {

    private Button btnLogout;
    private TextView tvStaffNo;
    private TextView tvName;
    private TextView tvDepartment;
    private TextView tvPosition;
    private ImageView ivFaceImage;
    private TextView tvNoFace;
    private EditText etServerUrl;
    private Button btnSaveServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnLogout = findViewById(R.id.btn_logout);
        tvStaffNo = findViewById(R.id.tv_staff_no);
        tvName = findViewById(R.id.tv_name);
        tvDepartment = findViewById(R.id.tv_department);
        tvPosition = findViewById(R.id.tv_position);
        ivFaceImage = findViewById(R.id.iv_face_image);
        tvNoFace = findViewById(R.id.tv_no_face);
        etServerUrl = findViewById(R.id.et_server_url);
        btnSaveServer = findViewById(R.id.btn_save_server);

        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");
        String name = sp.getString("name", "");

        tvStaffNo.setText("工号: " + staffNo);
        tvName.setText(name);

        loadStaffInfo(staffNo);

        SharedPreferences configSp = getSharedPreferences("app_config", MODE_PRIVATE);
        String savedUrl = configSp.getString("server_url", "");
        if (savedUrl != null && !savedUrl.isEmpty()) {
            etServerUrl.setText(savedUrl);
        }

        btnSaveServer.setOnClickListener(v -> saveServerUrl());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(SettingActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void saveServerUrl() {
        String serverUrl = etServerUrl.getText().toString().trim();
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SharedPreferences configSp = getSharedPreferences("app_config", MODE_PRIVATE);
        SharedPreferences.Editor editor = configSp.edit();
        editor.putString("server_url", serverUrl);
        editor.apply();
        
        ApiService.resetInstance();
        
        Toast.makeText(this, "服务器地址保存成功", Toast.LENGTH_SHORT).show();
    }

    private void loadStaffInfo(String staffNo) {
        AttendanceApi api = ApiService.getAttendanceApi(this);
        Call<ApiResponse<Staff>> call = api.getStaffInfo(staffNo);

        call.enqueue(new Callback<ApiResponse<Staff>>() {
            @Override
            public void onResponse(Call<ApiResponse<Staff>> call, Response<ApiResponse<Staff>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Staff> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        Staff staff = apiResponse.getData();
                        tvDepartment.setText(staff.getDepartment() != null ? staff.getDepartment() : "--");
                        tvPosition.setText(staff.getPosition() != null ? staff.getPosition() : "--");

                        if (staff.getFaceFeature() != null && !staff.getFaceFeature().isEmpty()) {
                            try {
                                byte[] imageBytes = Base64.decode(staff.getFaceFeature(), Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                ivFaceImage.setImageBitmap(bitmap);
                                ivFaceImage.setVisibility(View.VISIBLE);
                                tvNoFace.setVisibility(View.GONE);
                            } catch (Exception e) {
                                ivFaceImage.setVisibility(View.GONE);
                                tvNoFace.setVisibility(View.VISIBLE);
                            }
                        } else {
                            ivFaceImage.setVisibility(View.GONE);
                            tvNoFace.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Staff>> call, Throwable t) {
            }
        });
    }
}