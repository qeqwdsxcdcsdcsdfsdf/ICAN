package com.attendance.activity;

import android.Manifest;
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

public class FaceVerificationActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvStatus;
    private View vProgress1;
    private View vProgress2;
    private View vProgress3;
    private Button btnCancel;
    private Button btnTakePhoto;
    private ImageView imgScan;
    private ImageView imgPreview;

    private String currentPhotoPath;
    private int verificationStep = 0;

    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int REQUEST_IMAGE_CAPTURE = 1003;

    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_FAILED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);

        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvStatus = findViewById(R.id.tv_status);
        vProgress1 = findViewById(R.id.v_progress_1);
        vProgress2 = findViewById(R.id.v_progress_2);
        vProgress3 = findViewById(R.id.v_progress_3);
        btnCancel = findViewById(R.id.btn_cancel);
        imgScan = findViewById(R.id.img_scan);
        imgPreview = findViewById(R.id.img_preview);

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCEL);
            finish();
        });

        checkCameraPermission();
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
                Toast.makeText(this, "请允许相机权限以进行人脸识别", Toast.LENGTH_SHORT).show();
                setResult(RESULT_FAILED);
                finish();
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
            setResult(RESULT_FAILED);
            finish();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FACE_" + timeStamp + "_";
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
            verifyFacePhoto();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCEL);
            finish();
        }
    }

    private void verifyFacePhoto() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap == null) {
                Toast.makeText(this, "无法读取照片", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
                return;
            }

            bitmap = compressBitmap(bitmap, 500, 500);

            imgPreview.setImageBitmap(bitmap);
            imgPreview.setVisibility(View.VISIBLE);
            tvStatus.setText("正在验证人脸...");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            final String faceImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            final String staffNo = sp.getString("staffNo", "");

            Map<String, String> params = new java.util.HashMap<>();
            params.put("staffNo", staffNo);
            params.put("faceImage", faceImage);

            AttendanceApi api = ApiService.getAttendanceApi(this);
            Call<ApiResponse<String>> call = api.verifyFace(params);

            call.enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<String> apiResponse = response.body();
                        if (apiResponse.getCode() == 200) {
                            verificationStep++;
                            updateProgress();

                            if (verificationStep >= 3) {
                                tvStatus.setText("验证成功");
                                imgScan.setVisibility(View.VISIBLE);
                                new android.os.Handler().postDelayed(() -> {
                                    setResult(RESULT_SUCCESS);
                                    finish();
                                }, 1000);
                            } else {
                                tvStatus.setText("第" + verificationStep + "步验证通过，请继续");
                                new android.os.Handler().postDelayed(() -> {
                                    imgPreview.setVisibility(View.GONE);
                                    dispatchTakePictureIntent();
                                }, 1000);
                            }
                        } else {
                            tvStatus.setText("验证失败: " + apiResponse.getMessage());
                            verificationStep = 0;
                            updateProgress();
                            new android.os.Handler().postDelayed(() -> {
                                imgPreview.setVisibility(View.GONE);
                                dispatchTakePictureIntent();
                            }, 1000);
                        }
                    } else {
                        tvStatus.setText("验证失败，请重试");
                        verificationStep = 0;
                        updateProgress();
                        new android.os.Handler().postDelayed(() -> {
                            imgPreview.setVisibility(View.GONE);
                            dispatchTakePictureIntent();
                        }, 1000);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    tvStatus.setText("网络连接失败");
                    verificationStep = 0;
                    updateProgress();
                    new android.os.Handler().postDelayed(() -> {
                        imgPreview.setVisibility(View.GONE);
                        dispatchTakePictureIntent();
                    }, 1000);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "照片处理失败", Toast.LENGTH_SHORT).show();
            dispatchTakePictureIntent();
        }
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

    private void updateProgress() {
        vProgress1.setBackgroundResource(verificationStep >= 1 ? R.drawable.circle_green : R.drawable.circle_red);
        vProgress2.setBackgroundResource(verificationStep >= 2 ? R.drawable.circle_green : R.drawable.circle_red);
        vProgress3.setBackgroundResource(verificationStep >= 3 ? R.drawable.circle_green : R.drawable.circle_red);
    }
}