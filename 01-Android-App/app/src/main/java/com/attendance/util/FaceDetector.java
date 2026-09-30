package com.attendance.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;

public class FaceDetector {

    private static final int FACE_SIZE = 200;
    private static final int CELL_SIZE = 10;
    private Context mContext;
    private OnFaceDetectedListener mListener;

    public interface OnFaceDetectedListener {
        void onFaceDetected(Bitmap faceBitmap, String faceFeature);
        void onNoFaceDetected();
        void onError(String error);
    }

    public FaceDetector(Context context) {
        mContext = context;
    }

    public void setOnFaceDetectedListener(OnFaceDetectedListener listener) {
        mListener = listener;
    }

    public void detectFaceFromBitmap(final Bitmap bitmap) {
        if (bitmap == null) {
            if (mListener != null) {
                mListener.onError("图片为空");
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    android.media.FaceDetector.Face[] faces = new android.media.FaceDetector.Face[5];
                    android.media.FaceDetector detector = new android.media.FaceDetector(width, height, 5);

                    Bitmap workingBitmap = bitmap;
                    Bitmap.Config config = workingBitmap.getConfig();
                    if (config != Bitmap.Config.RGB_565) {
                        workingBitmap = workingBitmap.copy(Bitmap.Config.RGB_565, false);
                    }

                    int faceCount = detector.findFaces(workingBitmap, faces);

                    Rect faceRect;
                    boolean faceFound = faceCount > 0;

                    if (faceFound) {
                        android.media.FaceDetector.Face face = faces[0];
                        PointF midPoint = new PointF();
                        face.getMidPoint(midPoint);
                        float eyesDistance = face.eyesDistance();

                        int faceWidth = (int) (eyesDistance * 3.5);
                        int faceHeight = (int) (eyesDistance * 4.5);

                        int x = Math.max(0, (int) midPoint.x - faceWidth / 2);
                        int y = Math.max(0, (int) midPoint.y - faceHeight / 2);
                        int w = Math.min(width - x, faceWidth);
                        int h = Math.min(height - y, faceHeight);

                        faceRect = new Rect(x, y, x + w, y + h);
                    } else {
                        int centerX = width / 2;
                        int centerY = height / 2;
                        int size = Math.min(width, height) / 2;
                        faceRect = new Rect(centerX - size / 2, centerY - size / 2,
                                centerX + size / 2, centerY + size / 2);
                    }

                    Bitmap croppedBitmap = Bitmap.createBitmap(workingBitmap,
                            faceRect.left, faceRect.top,
                            faceRect.width(), faceRect.height());

                    final Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, FACE_SIZE, FACE_SIZE, true);

                    final String feature = generateFeature(resizedBitmap);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onFaceDetected(resizedBitmap, feature);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        final Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, FACE_SIZE, FACE_SIZE, true);
                        final String feature = generateFeature(resizedBitmap);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onFaceDetected(resizedBitmap, feature);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onError("人脸检测失败: " + ex.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private String generateFeature(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int cellsPerRow = width / CELL_SIZE;
        int cellsPerCol = height / CELL_SIZE;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cellsPerCol; i++) {
            for (int j = 0; j < cellsPerRow; j++) {
                double graySum = 0;
                double rSum = 0, gSum = 0, bSum = 0;
                double edgeCount = 0;
                int count = 0;

                for (int y = i * CELL_SIZE; y < (i + 1) * CELL_SIZE && y < height; y++) {
                    for (int x = j * CELL_SIZE; x < (j + 1) * CELL_SIZE && x < width; x++) {
                        int pixel = pixels[y * width + x];
                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        int gray = (r + g + b) / 3;

                        graySum += gray;
                        rSum += r;
                        gSum += g;
                        bSum += b;
                        count++;

                        if (x > 0 && y > 0 && x < width - 1 && y < height - 1) {
                            int left = pixels[y * width + (x - 1)];
                            int up = pixels[(y - 1) * width + x];
                            int leftGray = (Color.red(left) + Color.green(left) + Color.blue(left)) / 3;
                            int upGray = (Color.red(up) + Color.green(up) + Color.blue(up)) / 3;

                            if (Math.abs(gray - leftGray) > 30 || Math.abs(gray - upGray) > 30) {
                                edgeCount++;
                            }
                        }
                    }
                }

                if (count > 0) {
                    double avgGray = graySum / count / 255.0;
                    double avgR = rSum / count / 255.0;
                    double avgG = gSum / count / 255.0;
                    double avgB = bSum / count / 255.0;
                    double edgeRatio = edgeCount / count;

                    sb.append(String.format("%.4f,%.4f,%.4f,%.4f,%.4f", avgGray, avgR, avgG, avgB, edgeRatio));
                } else {
                    sb.append("0.5,0.5,0.5,0.5,0.0");
                }

                if (i < cellsPerCol - 1 || j < cellsPerRow - 1) {
                    sb.append(";");
                }
            }
        }
        return sb.toString();
    }

    public static float[] stringToArray(String featureString) {
        if (featureString == null || featureString.isEmpty()) {
            return new float[0];
        }

        try {
            String[] blocks = featureString.split(";");
            java.util.ArrayList<Float> result = new java.util.ArrayList<>();

            for (String block : blocks) {
                String[] values = block.split(",");
                for (String value : values) {
                    result.add(Float.parseFloat(value.trim()));
                }
            }

            float[] array = new float[result.size()];
            for (int i = 0; i < result.size(); i++) {
                array[i] = result.get(i);
            }
            return array;
        } catch (Exception e) {
            return new float[0];
        }
    }

    public static float calculateSimilarity(float[] feature1, float[] feature2) {
        if (feature1 == null || feature2 == null || feature1.length != feature2.length) {
            return 0f;
        }

        float sum = 0f;
        for (int i = 0; i < feature1.length; i++) {
            sum += Math.abs(feature1[i] - feature2[i]);
        }

        float distance = sum / feature1.length;
        return 1f - distance;
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}