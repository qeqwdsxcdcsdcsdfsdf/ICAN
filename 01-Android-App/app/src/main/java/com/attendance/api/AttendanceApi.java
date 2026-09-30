package com.attendance.api;

import com.attendance.bean.ApiResponse;
import com.attendance.bean.AttendanceRecord;
import com.attendance.bean.Staff;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AttendanceApi {

    @POST("/api/staff/login")
    @FormUrlEncoded
    Call<ApiResponse<Staff>> login(
            @Field("staffNo") String staffNo,
            @Field("password") String password
    );

    @POST("/api/sign/in")
    Call<ApiResponse<Object>> signIn(@Body Map<String, Object> params);

    @POST("/api/staff/face/register")
    Call<ApiResponse<String>> registerFace(@Body Map<String, String> params);

    @POST("/api/sign/verify-face")
    Call<ApiResponse<String>> verifyFace(@Body Map<String, String> params);

    @GET("/api/sign/records")
    Call<ApiResponse<List<AttendanceRecord>>> getAttendanceRecords(
            @Query("staffNo") String staffNo
    );

    @GET("/api/staff/info")
    Call<ApiResponse<Staff>> getStaffInfo(
            @Query("staffNo") String staffNo
    );

    @POST("/api/location/check")
    @FormUrlEncoded
    Call<ApiResponse<Object>> checkLocation(
            @Field("lat") double lat,
            @Field("lng") double lng
    );

    @POST("/api/staff/change-password")
    @FormUrlEncoded
    Call<ApiResponse<String>> changePassword(
            @Field("staffNo") String staffNo,
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword
    );

    @GET("/api/location/config")
    Call<ApiResponse<Object>> getLocationConfig();
}