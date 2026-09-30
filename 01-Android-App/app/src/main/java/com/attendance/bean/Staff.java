package com.attendance.bean;

public class Staff {
    private Long id;
    private String staffNo;
    private String name;
    private String department;
    private String position;
    private String phone;
    private String email;
    private String faceFeature;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStaffName() { return name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFaceFeature() { return faceFeature; }
    public void setFaceFeature(String faceFeature) { this.faceFeature = faceFeature; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}