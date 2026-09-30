package com.attendance.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import android.util.Base64;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRegisterActivity extends Activity {

    private Button btnRegister;
    private TextView tvStatus;

    private String currentPhotoPath;

    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int REQUEST_IMAGE_CAPTURE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register);

        btnRegister = findViewById(R.id.btn_register);
        tvStatus = findViewById(R.id.tv_status);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPhotoPath == null) {
                    checkCameraPermission();
                } else {
                    registerFace();
                }
            }
        });
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "请允许相机权限以注册人脸", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.attendance.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FACE_REG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tvStatus.setText("已拍照，请再次点击注册");
            btnRegister.setText("确认注册");
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            currentPhotoPath = null;
        }
    }

    private void registerFace() {
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap == null) {
                Toast.makeText(this, "无法读取照片", Toast.LENGTH_SHORT).show();
                return;
            }

            bitmap = compressBitmap(bitmap, 500, 500);

            tvStatus.setText("正在检测人脸...");
            btnRegister.setEnabled(false);

            FaceDetector detector = new FaceDetector(this);
            detector.setOnFaceDetectedListener(new FaceDetector.OnFaceDetectedListener() {
                @Override
                public void onFaceDetected(Bitmap faceBitmap, String faceFeature) {
                    uploadFaceFeature(staffNo, faceFeature);
                }

                @Override
                public void onNoFaceDetected() {
                    runOnUiThread(() -> {
                        tvStatus.setText("未检测到人脸，请重新拍照");
                        btnRegister.setEnabled(true);
                        btnRegister.setText("重新拍照");
                        currentPhotoPath = null;
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvStatus.setText("人脸检测失败: " + error);
                        btnRegister.setEnabled(true);
                    });
                }
            });
            detector.detectFaceFromBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(this, "照片处理失败", Toast.LENGTH_SHORT).show();
            btnRegister.setEnabled(true);
        }
    }

    private void uploadFaceFeature(String staffNo, String faceFeature) {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        if (bitmap == null) {
            Toast.makeText(this, "无法读取照片", Toast.LENGTH_SHORT).show();
            btnRegister.setEnabled(true);
            return;
        }

        bitmap = compressBitmap(bitmap, 500, 500);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String faceImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        Map<String, String> params = new java.util.HashMap<>();
        params.put("staffNo", staffNo);
        params.put("faceImage", faceImage);

        AttendanceApi api = ApiService.getAttendanceApi(this);
        Call<ApiResponse<String>> call = api.registerFace(params);

        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<String> apiResponse = response.body();
                        if (apiResponse.getCode() == 200 || apiResponse.isSuccess()) {
                            Toast.makeText(FaceRegisterActivity.this, "人脸注册成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(FaceRegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            tvStatus.setText("注册失败");
                        }
                    } else {
                        String errorMsg = "注册失败";
                        if (response.code() != 0) {
                            errorMsg += " (HTTP " + response.code() + ")";
                        }
                        Toast.makeText(FaceRegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        tvStatus.setText("注册失败");
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(FaceRegisterActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    tvStatus.setText("网络连接失败");
                });
            }
        });
    }

    private Bitmap compressBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);

        if (scale >= 1) {
            return bitmap;
        }

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}