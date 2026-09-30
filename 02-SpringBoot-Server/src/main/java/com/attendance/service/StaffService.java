package com.attendance.service;

import com.attendance.common.ApiResponse;
import com.attendance.entity.Staff;
import com.attendance.mapper.StaffMapper;
import com.attendance.util.FaceCompareUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.List;

@Service
public class StaffService {
    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private BaiduFaceService baiduFaceService;

    public ApiResponse<Staff> login(String staffNo, String password) {
        logger.info("员工登录 - 工号: {}", staffNo);

        Staff staff = staffMapper.selectByStaffNo(staffNo);
        
        if (staff == null) {
            logger.warn("登录失败，员工不存在: {}", staffNo);
            return ApiResponse.error("员工不存在");
        }

        if (staff.getStatus() != 1) {
            logger.warn("登录失败，员工已被禁用: {}", staffNo);
            return ApiResponse.error("员工已被禁用");
        }

        String md5Password = md5(password);
        if (staff.getPassword().equals(md5Password)) {
            logger.info("登录成功 - 工号: {}", staffNo);
            return ApiResponse.success("登录成功", staff);
        } else {
            logger.warn("登录失败，密码错误: {}", staffNo);
            return ApiResponse.error("密码错误");
        }
    }

    public ApiResponse<Object> registerFace(String staffNo, String faceFeature) {
        logger.info("人脸注册 - 工号: {}", staffNo);

        try {
            Staff staff = staffMapper.selectByStaffNo(staffNo);
            if (staff == null) {
                logger.warn("人脸注册失败，员工不存在: {}", staffNo);
                return ApiResponse.error("员工不存在");
            }

            if (faceFeature == null || faceFeature.isEmpty()) {
                logger.warn("人脸注册失败，照片为空: {}", staffNo);
                return ApiResponse.error("人脸照片不能为空");
            }

            if (faceFeature.length() > 500000) {
                logger.warn("人脸注册失败，照片过大: {}", staffNo);
                return ApiResponse.error("照片过大，请压缩后重试");
            }

            faceFeature = stripBase64Prefix(faceFeature);

            boolean baiduRegistered = baiduFaceService.addFace(staffNo, faceFeature);
            if (baiduRegistered) {
                logger.info("百度AI人脸注册成功 - 工号: {}", staffNo);
            } else {
                logger.warn("百度AI人脸注册失败或未配置，将使用本地比对 - 工号: {}", staffNo);
            }

            staff.setFaceFeature(faceFeature);
            int result = staffMapper.updateById(staff);
            
            if (result > 0) {
                logger.info("人脸注册成功 - 工号: {}", staffNo);
                return ApiResponse.success("人脸注册成功");
            } else {
                logger.error("人脸注册失败，更新数据库失败: {}", staffNo);
                return ApiResponse.error("人脸注册失败");
            }
        } catch (Exception e) {
            logger.error("人脸注册异常 - 工号: {}", staffNo, e);
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }

    public ApiResponse<List<Staff>> getAllStaff() {
        logger.info("查询所有员工");
        List<Staff> staffList = staffMapper.selectList(null);
        logger.info("查询到员工: {} 人", staffList.size());
        return ApiResponse.success("查询成功", staffList);
    }

    public ApiResponse<Staff> getStaffByStaffNo(String staffNo) {
        logger.info("查询员工信息 - 工号: {}", staffNo);
        Staff staff = staffMapper.selectByStaffNo(staffNo);
        if (staff != null) {
            return ApiResponse.success("查询成功", staff);
        } else {
            logger.warn("员工不存在 - 工号: {}", staffNo);
            return ApiResponse.error("员工不存在");
        }
    }

    public ApiResponse<Object> addStaff(Staff staff) {
        logger.info("添加员工 - 工号: {}", staff.getStaffNo());

        Staff existing = staffMapper.selectByStaffNo(staff.getStaffNo());
        if (existing != null) {
            logger.warn("添加员工失败，工号已存在: {}", staff.getStaffNo());
            return ApiResponse.error("工号已存在");
        }

        staff.setStatus(1);
        staff.setFaceFeature(FaceCompareUtil.generateRandomFeature());
        
        if (staff.getPassword() == null || staff.getPassword().isEmpty()) {
            String defaultPassword = staff.getStaffNo();
            if (defaultPassword.length() > 6) {
                defaultPassword = defaultPassword.substring(defaultPassword.length() - 6);
            }
            staff.setPassword(md5(defaultPassword));
            logger.info("员工密码为空，使用默认密码(工号后6位)并MD5加密 - 工号: {}", staff.getStaffNo());
        } else {
            staff.setPassword(md5(staff.getPassword()));
            logger.info("员工密码已MD5加密 - 工号: {}", staff.getStaffNo());
        }
        
        int result = staffMapper.insert(staff);
        
        if (result > 0) {
            logger.info("添加员工成功 - 工号: {}", staff.getStaffNo());
            return ApiResponse.success("添加成功");
        } else {
            logger.error("添加员工失败 - 工号: {}", staff.getStaffNo());
            return ApiResponse.error("添加失败");
        }
    }

    public ApiResponse<Object> updateStaff(Staff staff) {
        logger.info("更新员工信息 - ID: {}", staff.getId());
        
        Staff existing = staffMapper.selectById(staff.getId());
        if (existing == null) {
            logger.warn("更新员工失败，员工不存在: ID={}", staff.getId());
            return ApiResponse.error("员工不存在");
        }

        if (staff.getPassword() == null || staff.getPassword().isEmpty()) {
            staff.setPassword(existing.getPassword());
        } else {
            staff.setPassword(md5(staff.getPassword()));
            logger.info("员工密码已MD5加密 - ID: {}", staff.getId());
        }
        
        int result = staffMapper.updateById(staff);
        
        if (result > 0) {
            logger.info("更新员工成功 - ID: {}", staff.getId());
            return ApiResponse.success("更新成功");
        } else {
            logger.error("更新员工失败 - ID: {}", staff.getId());
            return ApiResponse.error("更新失败");
        }
    }

    public ApiResponse<Object> deleteStaff(Long id) {
        logger.info("删除员工 - ID: {}", id);
        
        int result = staffMapper.deleteById(id);
        
        if (result > 0) {
            logger.info("删除员工成功 - ID: {}", id);
            return ApiResponse.success("删除成功");
        } else {
            logger.error("删除员工失败 - ID: {}", id);
            return ApiResponse.error("删除失败");
        }
    }

    public ApiResponse<Object> changePassword(String staffNo, String oldPassword, String newPassword) {
        logger.info("修改密码 - 工号: {}", staffNo);

        Staff staff = staffMapper.selectByStaffNo(staffNo);
        if (staff == null) {
            logger.warn("修改密码失败，员工不存在: {}", staffNo);
            return ApiResponse.error("员工不存在");
        }

        if (staff.getStatus() != 1) {
            logger.warn("修改密码失败，员工已被禁用: {}", staffNo);
            return ApiResponse.error("员工已被禁用");
        }

        String md5OldPassword = md5(oldPassword);
        if (!staff.getPassword().equals(md5OldPassword)) {
            logger.warn("修改密码失败，旧密码错误: {}", staffNo);
            return ApiResponse.error("旧密码错误");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            logger.warn("修改密码失败，新密码为空: {}", staffNo);
            return ApiResponse.error("新密码不能为空");
        }

        if (newPassword.length() < 6) {
            logger.warn("修改密码失败，新密码长度不足: {}", staffNo);
            return ApiResponse.error("新密码至少6位");
        }

        staff.setPassword(md5(newPassword));
        int result = staffMapper.updateById(staff);

        if (result > 0) {
            logger.info("修改密码成功 - 工号: {}", staffNo);
            return ApiResponse.success("密码修改成功");
        } else {
            logger.error("修改密码失败，更新数据库失败: {}", staffNo);
            return ApiResponse.error("密码修改失败");
        }
    }

    private String stripBase64Prefix(String base64) {
        if (base64 != null) {
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            base64 = base64.replaceAll("\\s", "");
        }
        return base64;
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("MD5加密异常", e);
            return input;
        }
    }
}