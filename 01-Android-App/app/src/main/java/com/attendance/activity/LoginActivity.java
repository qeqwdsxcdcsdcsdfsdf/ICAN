package com.attendance.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.bean.Staff;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etStaffNo;
    private EditText etPassword;
    private Button btnLogin;
    private LinearLayout btnSettings;
    private LinearLayout loginCard;
    private LinearLayout brandSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etStaffNo = findViewById(R.id.et_staff_no);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSettings = findViewById(R.id.btn_settings);
        loginCard = findViewById(R.id.login_card);
        brandSection = findViewById(R.id.brand_section);

        btnSettings.setOnClickListener(v -> showServerSettingsDialog());

        startAnimations();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                String staffNo = etStaffNo.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(staffNo)) {
                    shakeView(etStaffNo);
                    Toast.makeText(LoginActivity.this, "请输入工号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    shakeView(etPassword);
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(staffNo, password);
            }
        });
    }

    private void showServerSettingsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("服务器设置");
        
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("服务器地址: http://xxx.xxx.xxx.xxx:8080");
        input.setTextSize(14);
        input.setPadding(20, 16, 20, 16);
        
        SharedPreferences configSp = getSharedPreferences("app_config", MODE_PRIVATE);
        String savedUrl = configSp.getString("server_url", "");
        if (savedUrl != null && !savedUrl.isEmpty()) {
            input.setText(savedUrl);
        }
        
        builder.setView(input);
        
        builder.setPositiveButton("保存", (dialog, which) -> {
            String serverUrl = input.getText().toString().trim();
            if (!serverUrl.isEmpty()) {
                SharedPreferences.Editor editor = configSp.edit();
                editor.putString("server_url", serverUrl);
                editor.apply();
                ApiService.resetInstance();
                Toast.makeText(LoginActivity.this, "服务器地址保存成功", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }

    private void startAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);

        brandSection.startAnimation(fadeIn);
        
        slideUp.setStartOffset(300);
        loginCard.startAnimation(slideUp);

        btnLogin.startAnimation(scaleIn);
        scaleIn.setStartOffset(600);
    }

    private void animateButtonPress(View view) {
        Animation pressAnim = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        pressAnim.setDuration(150);
        view.startAnimation(pressAnim);
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    private void login(String staffNo, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");

        AttendanceApi api = ApiService.getAttendanceApi(this);
        Call<ApiResponse<Staff>> call = api.login(staffNo, password);

        call.enqueue(new Callback<ApiResponse<Staff>>() {
            @Override
            public void onResponse(Call<ApiResponse<Staff>> call, Response<ApiResponse<Staff>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("登 录");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Staff> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("staffNo", apiResponse.getData().getStaffNo());
                        editor.putString("staffName", apiResponse.getData().getStaffName());
                        editor.putString("department", apiResponse.getData().getDepartment() != null ? apiResponse.getData().getDepartment() : "");
                        editor.putString("position", apiResponse.getData().getPosition() != null ? apiResponse.getData().getPosition() : "");
                        editor.putString("phone", apiResponse.getData().getPhone() != null ? apiResponse.getData().getPhone() : "");
                        editor.putString("email", apiResponse.getData().getEmail() != null ? apiResponse.getData().getEmail() : "");
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        sendDebugLog("login-success", "before-start-main", "Starting MainActivity");
                        startActivityWithAnimation(new Intent(LoginActivity.this, MainActivity.class));
                        sendDebugLog("login-success", "after-start-main", "MainActivity started successfully");
                        finish();
                    } else {
                        shakeView(loginCard);
                        Toast.makeText(LoginActivity.this, "登录失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    shakeView(loginCard);
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Staff>> call, Throwable t) {
                t.printStackTrace();
                btnLogin.setEnabled(true);
                btnLogin.setText("登 录");
                shakeView(loginCard);
                Toast.makeText(LoginActivity.this, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
    }

    private void sendDebugLog(String tag, String event, String message) {
        try {
            java.net.URL url = new java.net.URL("http://10.0.2.2:9999/log");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String json = String.format("{\"tag\":\"%s\",\"event\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}", 
                tag, event, message, System.currentTimeMillis());
            conn.getOutputStream().write(json.getBytes());
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            // Silent fail for debug logging
        }
    }
}