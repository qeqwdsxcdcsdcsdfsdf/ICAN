package com.attendance.bean;

public class SignRequest {
    private String staffNo;
    private String faceFeature;
    private Double lat;
    private Double lng;
    private String deviceInfo;

    public SignRequest() {}

    public SignRequest(String staffNo, String faceFeature, Double lat, Double lng, String deviceInfo) {
        this.staffNo = staffNo;
        this.faceFeature = faceFeature;
        this.lat = lat;
        this.lng = lng;
        this.deviceInfo = deviceInfo;
    }

    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getFaceFeature() { return faceFeature; }
    public void setFaceFeature(String faceFeature) { this.faceFeature = faceFeature; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}