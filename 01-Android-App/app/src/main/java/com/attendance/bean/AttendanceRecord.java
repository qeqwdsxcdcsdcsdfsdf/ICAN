package com.attendance.bean;

import java.util.Date;

public class AttendanceRecord {
    private Long id;
    private String staffNo;
    private String staffName;
    private Integer signType;
    private Date signTime;
    private Integer status;
    private Integer faceMatchResult;
    private Double signLat;
    private Double signLng;
    private Integer distance;
    private Integer locationStatus;
    private Integer signResult;
    private String failReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public Integer getSignType() { return signType; }
    public void setSignType(Integer signType) { this.signType = signType; }
    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getFaceMatchResult() { return faceMatchResult; }
    public void setFaceMatchResult(Integer faceMatchResult) { this.faceMatchResult = faceMatchResult; }
    public Double getSignLat() { return signLat; }
    public void setSignLat(Double signLat) { this.signLat = signLat; }
    public Double getSignLng() { return signLng; }
    public void setSignLng(Double signLng) { this.signLng = signLng; }
    public Integer getDistance() { return distance; }
    public void setDistance(Integer distance) { this.distance = distance; }
    public Integer getLocationStatus() { return locationStatus; }
    public void setLocationStatus(Integer locationStatus) { this.locationStatus = locationStatus; }
    public Integer getSignResult() { return signResult; }
    public void setSignResult(Integer signResult) { this.signResult = signResult; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
}