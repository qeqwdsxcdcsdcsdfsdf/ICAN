package com.attendance.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Map;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.location.LocationHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private TextView tvLocation;
    private TextView tvStatus;
    private TextView tvDistance;
    private Button btnSignIn;
    private Button btnRecords;
    private Button btnSetting;
    private Button btnFaceRegister;
    private Button btnViewLocation;
    private LinearLayout statusCard;
    private LinearLayout menuList;

    private LocationHelper locationHelper;
    private double latitude = 0;
    private double longitude = 0;
    private boolean isSigning = false;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_FACE_RECOGNITION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        tvLocation = findViewById(R.id.tv_location);
        tvStatus = findViewById(R.id.tv_status);
        tvDistance = findViewById(R.id.tv_distance);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnRecords = findViewById(R.id.btn_records);
        btnSetting = findViewById(R.id.btn_setting);
        btnFaceRegister = findViewById(R.id.btn_face_register);
        btnViewLocation = findViewById(R.id.btn_view_location);
        statusCard = findViewById(R.id.status_card);
        menuList = findViewById(R.id.menu_list);

        tvLocation.setText("当前位置: --");
        tvStatus.setText("状态: 等待定位");
        tvDistance.setText("距离信息: 计算中...");

        startAnimations();

        locationHelper = new LocationHelper(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationChanged(double lat, double lng) {
                try {
                    latitude = lat;
                    longitude = lng;
                    tvLocation.setText("当前位置: " + String.format("%.4f, %.4f", lat, lng));
                    tvStatus.setText("状态: 点击签到进行人脸识别");
                    tvDistance.setText("距离信息: 计算中...");
                    checkLocationDistance(lat, lng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                try {
                    tvLocation.setText("定位失败: " + error);
                    tvStatus.setText("状态: 定位失败，请检查权限");
                    tvDistance.setText("距离信息: 无法获取");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        checkLocationPermission();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSigning) {
                    return;
                }
                animateButtonPress(v);
                startFaceRecognition();
            }
        });

        btnRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                startActivityWithAnimation(new Intent(SignInActivity.this, AttendanceRecordActivity.class));
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                startActivityWithAnimation(new Intent(SignInActivity.this, SettingActivity.class));
            }
        });

        btnFaceRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                startActivityWithAnimation(new Intent(SignInActivity.this, FaceRegisterActivity.class));
            }
        });

        btnViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                viewCurrentLocation();
            }
        });
    }

    private void startAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);

        statusCard.startAnimation(fadeIn);

        slideUp.setStartOffset(300);
        btnSignIn.startAnimation(slideUp);

        scaleIn.setStartOffset(500);
        menuList.startAnimation(scaleIn);
    }

    private void animateButtonPress(View view) {
        Animation pressAnim = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        pressAnim.setDuration(150);
        view.startAnimation(pressAnim);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            if (locationHelper != null) {
                locationHelper.startLocationUpdates();
            }
        }
    }

    private void startFaceRecognition() {
        Intent intent = new Intent(SignInActivity.this, FaceRecognitionActivity.class);
        startActivityForResult(intent, REQUEST_FACE_RECOGNITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FACE_RECOGNITION) {
            if (resultCode == FaceRecognitionActivity.RESULT_SUCCESS) {
                signIn();
            } else if (resultCode == FaceRecognitionActivity.RESULT_CANCEL) {
                tvStatus.setText("状态: 已取消人脸识别");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationHelper != null) {
                    locationHelper.startLocationUpdates();
                }
            } else {
                Toast.makeText(this, "请允许定位权限以使用签到功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "暂未获取到位置，将继续进行签到", Toast.LENGTH_SHORT).show();
        }

        isSigning = true;
        btnSignIn.setEnabled(false);
        tvStatus.setText("状态: 正在签到...");

        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        SharedPreferences faceSp = getSharedPreferences("face_temp", MODE_PRIVATE);
        String faceImage = faceSp.getString("faceImage", "");

        if (faceImage.isEmpty()) {
            Toast.makeText(this, "人脸图片为空，请重新进行人脸识别", Toast.LENGTH_SHORT).show();
            isSigning = false;
            btnSignIn.setEnabled(true);
            tvStatus.setText("状态: 人脸图片为空");
            return;
        }

        AttendanceApi api = ApiService.getAttendanceApi(this);
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("staffNo", staffNo);
        params.put("faceFeature", faceImage);
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        Call<ApiResponse<Object>> call = api.signIn(params);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                isSigning = false;
                btnSignIn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(SignInActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        tvStatus.setText("状态: 签到成功");
                    } else {
                        Toast.makeText(SignInActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        tvStatus.setText("状态: 签到失败");
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "签到失败", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("状态: 签到失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                isSigning = false;
                btnSignIn.setEnabled(true);
                t.printStackTrace();
                Toast.makeText(SignInActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                tvStatus.setText("状态: 网络连接失败");
            }
        });
    }

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
    }

    private void checkLocationDistance(double lat, double lng) {
        AttendanceApi api = ApiService.getAttendanceApi(this);
        Call<ApiResponse<Object>> call = api.checkLocation(lat, lng);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof Map) {
                            Map<String, Object> result = (Map<String, Object>) data;
                            
                            Object distanceObj = result.get("distance");
                            Object radiusObj = result.get("radius");
                            Object validObj = result.get("locationValid");
                            Object messageObj = result.get("message");
                            
                            if (distanceObj != null && validObj != null) {
                                double distance = ((Number) distanceObj).doubleValue();
                                int radius = radiusObj != null ? ((Number) radiusObj).intValue() : 500;
                                boolean valid = (Boolean) validObj;
                                
                                if (valid) {
                                    tvDistance.setText("距离中心点: " + String.format("%.1f米", distance) + " (范围内)");
                                    tvDistance.setTextColor(getResources().getColor(R.color.colorGreen));
                                } else {
                                    if (messageObj != null && messageObj.toString().contains("未配置")) {
                                        tvDistance.setText("距离信息: " + messageObj.toString());
                                    } else {
                                        tvDistance.setText("距离中心点: " + String.format("%.1f米", distance) + " (超出范围)");
                                    }
                                    tvDistance.setTextColor(getResources().getColor(R.color.colorRed));
                                }
                            } else {
                                tvDistance.setText("距离信息: 获取失败");
                                tvDistance.setTextColor(getResources().getColor(R.color.colorText));
                            }
                        } else {
                            tvDistance.setText("距离信息: 获取失败");
                            tvDistance.setTextColor(getResources().getColor(R.color.colorText));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvDistance.setText("距离信息: 获取失败");
                        tvDistance.setTextColor(getResources().getColor(R.color.colorText));
                    }
                } else {
                    tvDistance.setText("距离信息: 获取失败");
                    tvDistance.setTextColor(getResources().getColor(R.color.colorText));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                t.printStackTrace();
                tvDistance.setText("距离信息: 网络错误");
                tvDistance.setTextColor(getResources().getColor(R.color.colorText));
            }
        });
    }

    private void viewCurrentLocation() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "暂未获取到位置信息", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String geoUri = "geo:" + latitude + "," + longitude + "?z=18&q=" + latitude + "," + longitude + "(我的位置)";
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));
            geoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            List<android.content.pm.ResolveInfo> activities = getPackageManager().queryIntentActivities(geoIntent, 0);
            if (activities != null && activities.size() > 0) {
                startActivity(geoIntent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String amapUrl = "https://uri.amap.com/marker?position=" + longitude + "," + latitude + "&name=我的位置&coordinate=gaode&callnative=1";
            Intent amapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(amapUrl));
            amapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            List<android.content.pm.ResolveInfo> activities = getPackageManager().queryIntentActivities(amapIntent, 0);
            if (activities != null && activities.size() > 0) {
                startActivity(amapIntent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String amapUri = "amapuri://viewMap?sourceApplication=appname&poiname=我的位置&lat=" + latitude + "&lon=" + longitude + "&dev=1";
            Intent amapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(amapUri));
            amapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            List<android.content.pm.ResolveInfo> activities = getPackageManager().queryIntentActivities(amapIntent, 0);
            if (activities != null && activities.size() > 0) {
                startActivity(amapIntent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String baiduUri = "baidumap://map/marker?location=" + latitude + "," + longitude + "&title=我的位置&content=当前定位";
            Intent baiduIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(baiduUri));
            baiduIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            List<android.content.pm.ResolveInfo> activities = getPackageManager().queryIntentActivities(baiduIntent, 0);
            if (activities != null && activities.size() > 0) {
                startActivity(baiduIntent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "未找到地图应用，请确保已安装高德地图", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }
}