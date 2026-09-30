package com.attendance.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.bean.Staff;
import com.attendance.util.LocationHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInFragment extends Fragment {

    private static final String TAG = "SignInFragment";
    private TextView tvStatus;
    private TextView tvCurrentAddress;
    private TextView tvCurrentCoords;
    private TextView tvDistance;
    private Button btnSignIn;
    private Button btnFaceRegister;
    private Button btnGetLocation;
    private boolean isSigning = false;
    private static final int REQUEST_FACE_RECOGNITION = 1002;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private double centerLatitude = 0;
    private double centerLongitude = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_simple, container, false);

        tvStatus = view.findViewById(R.id.tv_status);
        tvCurrentAddress = view.findViewById(R.id.tv_current_address);
        tvCurrentCoords = view.findViewById(R.id.tv_current_coords);
        tvDistance = view.findViewById(R.id.tv_distance);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        btnFaceRegister = view.findViewById(R.id.btn_face_register);
        btnGetLocation = view.findViewById(R.id.btn_get_location);

        if (tvStatus != null) {
            tvStatus.setText("状态: 点击签到进行人脸识别");
        }

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                if (isSigning) return;
                checkFaceRegistered();
            });
        }

        if (btnFaceRegister != null) {
            btnFaceRegister.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getContext(), FaceRegisterActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error starting FaceRegisterActivity", e);
                    Toast.makeText(getContext(), "启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> {
                fetchLocationAndCalculateDistance();
            });
        }

        fetchLocationConfig();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_FACE_RECOGNITION) {
            if (resultCode == FaceRecognitionActivity.RESULT_SUCCESS) {
                Log.d(TAG, "人脸识别成功，开始获取位置并签到");
                getLocationAndSignIn();
            } else if (resultCode == FaceRecognitionActivity.RESULT_CANCEL) {
                Log.d(TAG, "人脸识别取消");
                if (tvStatus != null) {
                    tvStatus.setText("状态: 已取消人脸识别");
                }
            } else {
                Log.d(TAG, "人脸识别结果未知: " + resultCode);
            }
        }
    }

    private void startFaceRecognition() {
        try {
            Intent intent = new Intent(getContext(), FaceRecognitionActivity.class);
            startActivityForResult(intent, REQUEST_FACE_RECOGNITION);
        } catch (Exception e) {
            Log.e(TAG, "Error starting FaceRecognitionActivity", e);
            Toast.makeText(getContext(), "启动识别失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFaceRegistered() {
        SharedPreferences sp = getContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        AttendanceApi api = ApiService.getAttendanceApi(getContext());
        Call<ApiResponse<Staff>> call = api.getStaffInfo(staffNo);
        call.enqueue(new Callback<ApiResponse<Staff>>() {
            @Override
            public void onResponse(Call<ApiResponse<Staff>> call, Response<ApiResponse<Staff>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Staff staff = response.body().getData();
                    String faceFeature = staff.getFaceFeature();
                    
                    if (faceFeature == null || faceFeature.isEmpty() || "0".equals(faceFeature)) {
                        Toast.makeText(getContext(), "您尚未注册人脸，请先注册", Toast.LENGTH_LONG).show();
                        try {
                            startActivity(new Intent(getContext(), FaceRegisterActivity.class));
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting FaceRegisterActivity", e);
                        }
                    } else {
                        startFaceRecognition();
                    }
                } else {
                    startFaceRecognition();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Staff>> call, Throwable t) {
                Log.w(TAG, "Failed to check face registration", t);
                startFaceRecognition();
            }
        });
    }

    private void fetchLocationConfig() {
        AttendanceApi api = ApiService.getAttendanceApi(getContext());
        Call<ApiResponse<Object>> call = api.getLocationConfig();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof Map) {
                        Map<String, Object> config = (Map<String, Object>) data;
                        Number lat = (Number) config.get("latitude");
                        Number lng = (Number) config.get("longitude");
                        if (lat != null && lng != null) {
                            centerLatitude = lat.doubleValue();
                            centerLongitude = lng.doubleValue();
                            Log.d(TAG, "Location config loaded: " + centerLatitude + ", " + centerLongitude);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.w(TAG, "Failed to fetch location config", t);
            }
        });
    }

    private void fetchLocationAndCalculateDistance() {
        if (btnGetLocation != null) {
            btnGetLocation.setEnabled(false);
            btnGetLocation.setText("获取中...");
        }
        if (tvCurrentAddress != null) {
            tvCurrentAddress.setText("正在获取定位...");
        }

        LocationHelper locationHelper = new LocationHelper(getContext());
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;

                if (tvCurrentCoords != null) {
                    tvCurrentCoords.setText(String.format("%.6f, %.6f", latitude, longitude));
                }

                String address = getAddressFromCoords(latitude, longitude);
                if (tvCurrentAddress != null) {
                    tvCurrentAddress.setText(address);
                }

                calculateAndShowDistance();

                if (btnGetLocation != null) {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("获取定位");
                }
            }

            @Override
            public void onLocationFailed(String error) {
                Log.w(TAG, "Location failed: " + error);
                Toast.makeText(getContext(), "定位失败: " + error, Toast.LENGTH_SHORT).show();
                if (tvCurrentAddress != null) {
                    tvCurrentAddress.setText("定位失败");
                }
                if (btnGetLocation != null) {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("获取定位");
                }
            }
        });
    }

    private String getAddressFromCoords(double lat, double lng) {
        if (lat == 0 && lng == 0) {
            return "未知位置";
        }

        if (getContext() != null && Geocoder.isPresent()) {
            try {
                Geocoder geocoder = new Geocoder(getContext());
                java.util.List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    if (address.getAdminArea() != null) {
                        sb.append(address.getAdminArea());
                    }
                    if (address.getLocality() != null) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(address.getLocality());
                    }
                    if (address.getThoroughfare() != null) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(address.getThoroughfare());
                    }
                    if (sb.length() > 0) {
                        return sb.toString();
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Geocoder error", e);
            }
        }

        return String.format("%.4f, %.4f", lat, lng);
    }

    private void calculateAndShowDistance() {
        if (centerLatitude == 0 && centerLongitude == 0) {
            if (tvDistance != null) {
                tvDistance.setText("-- 米");
            }
            return;
        }

        double distance = calculateDistance(currentLatitude, currentLongitude, centerLatitude, centerLongitude);
        if (tvDistance != null) {
            if (distance < 1000) {
                tvDistance.setText(String.format("%.0f 米", distance));
            } else {
                tvDistance.setText(String.format("%.2f 公里", distance / 1000));
            }
        }
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371000;
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 100) / 100;
        return s;
    }

    private void getLocationAndSignIn() {
        if (tvStatus != null) {
            tvStatus.setText("状态: 正在获取定位...");
        }

        LocationHelper locationHelper = new LocationHelper(getContext());
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;

                if (tvCurrentCoords != null) {
                    tvCurrentCoords.setText(String.format("%.6f, %.6f", latitude, longitude));
                }
                if (tvCurrentAddress != null) {
                    tvCurrentAddress.setText(getAddressFromCoords(latitude, longitude));
                }
                calculateAndShowDistance();

                signIn();
            }

            @Override
            public void onLocationFailed(String error) {
                Log.w(TAG, "Location failed: " + error);
                Toast.makeText(getContext(), "定位失败，将使用默认位置", Toast.LENGTH_SHORT).show();
                currentLatitude = 0;
                currentLongitude = 0;
                signIn();
            }
        });
    }

    private void signIn() {
        isSigning = true;
        if (btnSignIn != null) btnSignIn.setEnabled(false);
        if (tvStatus != null) tvStatus.setText("状态: 正在签到...");

        SharedPreferences sp = getContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        SharedPreferences faceSp = getContext().getSharedPreferences("face_temp", getContext().MODE_PRIVATE);
        String faceImage = faceSp.getString("faceImage", "");

        Log.d(TAG, "signIn() called - staffNo: " + staffNo + ", faceImage length: " + (faceImage != null ? faceImage.length() : 0) 
                + ", latitude: " + currentLatitude + ", longitude: " + currentLongitude);

        Map<String, Object> params = new HashMap<>();
        params.put("staffNo", staffNo);
        params.put("faceFeature", faceImage);
        params.put("latitude", currentLatitude);
        params.put("longitude", currentLongitude);

        AttendanceApi api = ApiService.getAttendanceApi(getContext());
        Call<ApiResponse<Object>> call = api.signIn(params);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                isSigning = false;
                if (btnSignIn != null) btnSignIn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (tvStatus != null) tvStatus.setText("状态: 签到成功");
                    } else {
                        Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (tvStatus != null) tvStatus.setText("状态: 签到失败");
                    }
                } else {
                    Toast.makeText(getContext(), "签到失败", Toast.LENGTH_SHORT).show();
                    if (tvStatus != null) tvStatus.setText("状态: 签到失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                isSigning = false;
                if (btnSignIn != null) btnSignIn.setEnabled(true);
                t.printStackTrace();
                Toast.makeText(getContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
                if (tvStatus != null) tvStatus.setText("状态: 网络连接失败");
            }
        });
    }
}