package com.attendance.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.util.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecognitionActivity extends AppCompatActivity {

    public static final int RESULT_SUCCESS = 100;
    public static final int RESULT_CANCEL = 101;
    public static final int RESULT_FAILED = 102;

    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int REQUEST_IMAGE_CAPTURE = 2001;

    private TextView tvTitle;
    private TextView tvTips;
    private Button btnCapture;
    private Button btnCancel;
    private Button btnBack;
    private View dot1, dot2, dot3;

    private Uri photoUri;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        tvTitle = findViewById(R.id.tv_title);
        tvTips = findViewById(R.id.tv_tips);
        btnCapture = findViewById(R.id.btn_capture);
        btnCancel = findViewById(R.id.btn_cancel);
        btnBack = findViewById(R.id.btn_back);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCEL);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCEL);
            finish();
        });

        btnCapture.setOnClickListener(v -> {
            if (!isProcessing) {
                checkCameraPermission();
            }
        });
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建拍照文件失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.attendance.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && photoUri != null) {
                isProcessing = true;
                btnCapture.setEnabled(false);
                updateDots(2);

                tvTitle.setText("正在验证身份...");
                tvTips.setText("请稍候");

                verifyFace();
            } else if (resultCode == RESULT_CANCELED) {
                tvTitle.setText("已取消拍照");
                updateDots(1);
            } else {
                tvTitle.setText("拍照失败");
                tvTips.setText("请重试");
                btnCapture.setEnabled(true);
                updateDots(1);
            }
        }
    }

    private void updateDots(int activeCount) {
        dot1.setBackgroundResource(activeCount >= 1 ? R.drawable.dot_active : R.drawable.dot_inactive);
        dot2.setBackgroundResource(activeCount >= 2 ? R.drawable.dot_active : R.drawable.dot_inactive);
        dot3.setBackgroundResource(activeCount >= 3 ? R.drawable.dot_active : R.drawable.dot_inactive);
    }

    private void verifyFace() {
        try {
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            if (bitmap == null) {
                throw new IOException("无法读取照片");
            }

            tvTitle.setText("正在检测人脸...");
            updateDots(2);

            FaceDetector detector = new FaceDetector(this);
            detector.setOnFaceDetectedListener(new FaceDetector.OnFaceDetectedListener() {
                @Override
                public void onFaceDetected(Bitmap faceBitmap, String faceFeature) {
                    runOnUiThread(() -> {
                        tvTitle.setText("正在比对人脸...");
                        updateDots(2);
                    });

                    uploadAndVerifyFace(bitmap, faceFeature);
                }

                @Override
                public void onNoFaceDetected() {
                    runOnUiThread(() -> {
                        tvTitle.setText("未检测到人脸");
                        tvTips.setText("请重新拍照");
                        isProcessing = false;
                        btnCapture.setEnabled(true);
                        updateDots(1);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvTitle.setText("验证失败");
                        tvTips.setText("请重试");
                        isProcessing = false;
                        btnCapture.setEnabled(true);
                        updateDots(1);
                    });
                }
            });
            detector.detectFaceFromBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                tvTitle.setText("验证失败");
                tvTips.setText("请重试");
                isProcessing = false;
                btnCapture.setEnabled(true);
                updateDots(1);
            });
        }
    }

    private void uploadAndVerifyFace(Bitmap bitmap, String faceFeature) {
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String faceImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        AttendanceApi api = ApiService.getAttendanceApi(this);
        Map<String, String> params = new HashMap<>();
        params.put("staffNo", staffNo);
        params.put("faceImage", faceImage);

        Call<ApiResponse<String>> call = api.verifyFace(params);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                isProcessing = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        SharedPreferences sp = getSharedPreferences("face_temp", MODE_PRIVATE);
                        sp.edit().putString("faceImage", faceImage).apply();

                        runOnUiThread(() -> {
                            tvTitle.setText("验证成功！");
                            tvTips.setText("正在完成签到");
                            updateDots(3);
                            Toast.makeText(FaceRecognitionActivity.this, "人脸识别成功！", Toast.LENGTH_SHORT).show();

                            new android.os.Handler().postDelayed(() -> {
                                setResult(RESULT_SUCCESS);
                                finish();
                            }, 1000);
                        });
                    } else {
                        runOnUiThread(() -> {
                            tvTitle.setText("验证失败");
                            tvTips.setText(apiResponse.getMessage());
                            btnCapture.setEnabled(true);
                            updateDots(1);
                            Toast.makeText(FaceRecognitionActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        tvTitle.setText("验证失败");
                        tvTips.setText("网络异常，请重试");
                        btnCapture.setEnabled(true);
                        updateDots(1);
                        Toast.makeText(FaceRecognitionActivity.this, "人脸验证失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                isProcessing = false;
                t.printStackTrace();
                runOnUiThread(() -> {
                    tvTitle.setText("验证失败");
                    tvTips.setText("网络连接失败");
                    btnCapture.setEnabled(true);
                    updateDots(1);
                    Toast.makeText(FaceRecognitionActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "请允许相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}