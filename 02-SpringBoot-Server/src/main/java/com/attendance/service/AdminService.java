package com.attendance.service;

import com.attendance.common.ApiResponse;
import com.attendance.entity.Admin;
import com.attendance.mapper.AdminMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminMapper adminMapper;

    public ApiResponse<Map<String, Object>> login(String username, String password) {
        logger.info("管理员登录 - 用户名: {}", username);

        Admin admin = adminMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username));
        
        if (admin == null) {
            logger.warn("管理员登录失败，用户名不存在: {}", username);
            return ApiResponse.error("用户名不存在");
        }

        String md5Password = md5(password);
        if (admin.getPassword().equals(md5Password)) {
            logger.info("管理员登录成功 - 用户名: {}", username);
            Map<String, Object> data = new HashMap<>();
            data.put("admin", admin);
            return ApiResponse.success("登录成功", data);
        } else {
            logger.warn("管理员登录失败，密码错误: {}", username);
            return ApiResponse.error("密码错误");
        }
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