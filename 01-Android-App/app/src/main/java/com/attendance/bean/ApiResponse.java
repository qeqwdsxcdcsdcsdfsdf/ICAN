package com.attendance.bean;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("signResult")
    private Integer signResult;

    @SerializedName("faceMatch")
    private Boolean faceMatch;

    @SerializedName("locationValid")
    private Boolean locationValid;

    @SerializedName("distance")
    private Integer distance;

    @SerializedName("locationStatus")
    private Integer locationStatus;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Integer getSignResult() { return signResult; }
    public void setSignResult(Integer signResult) { this.signResult = signResult; }
    public Boolean getFaceMatch() { return faceMatch; }
    public void setFaceMatch(Boolean faceMatch) { this.faceMatch = faceMatch; }
    public Boolean getLocationValid() { return locationValid; }
    public void setLocationValid(Boolean locationValid) { this.locationValid = locationValid; }
    public Integer getDistance() { return distance; }
    public void setDistance(Integer distance) { this.distance = distance; }
    public Integer getLocationStatus() { return locationStatus; }
    public void setLocationStatus(Integer locationStatus) { this.locationStatus = locationStatus; }
    public int getCode() { return success ? 200 : 500; }
}