package com.attendance.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static final String BASE_URL = "http://192.168.2.113:8080";

    private static Retrofit retrofit;

    public static String getBaseUrl(Context context) {
        SharedPreferences sp = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        String savedUrl = sp.getString("server_url", "");
        if (savedUrl != null && !savedUrl.isEmpty()) {
            return savedUrl;
        }
        return BASE_URL;
    }

    public static Retrofit getInstance(Context context) {
        String baseUrl = getBaseUrl(context);
        android.util.Log.d("ApiService", "Base URL: " + baseUrl);
        
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(new okhttp3.Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                            okhttp3.Request request = chain.request();
                            android.util.Log.d("ApiService", "Request: " + request.method() + " " + request.url());
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void resetInstance() {
        retrofit = null;
    }

    public static AttendanceApi getAttendanceApi(Context context) {
        return getInstance(context).create(AttendanceApi.class);
    }
}