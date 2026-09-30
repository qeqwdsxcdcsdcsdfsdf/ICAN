package com.attendance.controller;

import com.attendance.common.ApiResponse;
import com.attendance.entity.Staff;
import com.attendance.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {
    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);

    @Autowired
    private StaffService staffService;

    @PostMapping("/login")
    public ApiResponse<Staff> login(@RequestParam("staffNo") String staffNo, @RequestParam("password") String password) {
        logger.info("员工登录请求 - 工号: {}", staffNo);
        return staffService.login(staffNo, password);
    }

    @PostMapping("/face/register")
    public ApiResponse<Object> registerFace(@RequestBody Map<String, String> params) {
        String staffNo = params.get("staffNo");
        String faceImage = params.get("faceImage");
        logger.info("人脸注册请求 - 工号: {}", staffNo);
        return staffService.registerFace(staffNo, faceImage);
    }

    @GetMapping("/list")
    public ApiResponse<List<Staff>> getAllStaff() {
        logger.info("查询所有员工");
        return staffService.getAllStaff();
    }

    @GetMapping("/info")
    public ApiResponse<Staff> getStaffInfo(@RequestParam("staffNo") String staffNo) {
        logger.info("查询员工信息 - 工号: {}", staffNo);
        return staffService.getStaffByStaffNo(staffNo);
    }

    @PostMapping("/add")
    public ApiResponse<Object> addStaff(@RequestBody Staff staff) {
        logger.info("添加员工请求 - 工号: {}", staff.getStaffNo());
        return staffService.addStaff(staff);
    }

    @PostMapping("/update")
    public ApiResponse<Object> updateStaff(@RequestBody Staff staff) {
        logger.info("更新员工请求 - ID: {}", staff.getId());
        return staffService.updateStaff(staff);
    }

    @PostMapping("/delete")
    public ApiResponse<Object> deleteStaff(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        logger.info("删除员工请求 - ID: {}", id);
        return staffService.deleteStaff(id);
    }

    @PostMapping("/change-password")
    public ApiResponse<Object> changePassword(@RequestParam("staffNo") String staffNo, 
                                               @RequestParam("oldPassword") String oldPassword,
                                               @RequestParam("newPassword") String newPassword) {
        logger.info("修改密码请求 - 工号: {}", staffNo);
        return staffService.changePassword(staffNo, oldPassword, newPassword);
    }
}