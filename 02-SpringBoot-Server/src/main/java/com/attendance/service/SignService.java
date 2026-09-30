package com.attendance.service;

import com.attendance.common.ApiResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.Staff;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.mapper.StaffMapper;
import com.attendance.util.FaceCompareUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignService {
    private static final Logger logger = LoggerFactory.getLogger(SignService.class);

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private LocationCheckService locationCheckService;

    @Autowired
    private BaiduFaceService baiduFaceService;

    public ApiResponse<Map<String, Object>> signIn(String staffNo, String faceFeature, Double lat, Double lng, String deviceInfo) {
        logger.info("开始签到，员工编号: {}, 人脸特征: {}, 位置: ({}, {})", staffNo, 
                faceFeature != null ? "已提供" : "未提供", lat, lng);

        Staff staff = staffMapper.selectByStaffNo(staffNo);
        if (staff == null) {
            logger.warn("签到失败，员工不存在: {}", staffNo);
            return ApiResponse.error("员工不存在");
        }

        if (staff.getStatus() != 1) {
            logger.warn("签到失败，员工已被禁用: {}", staffNo);
            return ApiResponse.error("员工已被禁用");
        }

        Date now = new Date();
        int signType = getSignType(now);
        
        if (isDuplicateSign(staff.getId(), signType, now)) {
            String msg = signType == 1 ? "今日已完成上班签到" : "今日已完成下班签到";
            logger.warn("签到失败，重复签到: {}, 类型: {}", staffNo, signType);
            return ApiResponse.error(msg);
        }

        boolean faceMatch;
        int faceMatchResult;

        if (faceFeature == null || faceFeature.isEmpty()) {
            faceMatch = false;
            faceMatchResult = 1;
            logger.warn("人脸特征为空，验证失败");
        } else if ("VERIFIED".equals(faceFeature)) {
            faceMatch = false;
            faceMatchResult = 1;
            logger.warn("不允许跳过人脸验证，请提供真实人脸特征");
        } else {
            String registeredFaceFeature = staff.getFaceFeature();
            registeredFaceFeature = stripBase64Prefix(registeredFaceFeature);
            
            logger.info("人脸比对准备 - 已注册人脸特征长度: {}, 传入人脸特征长度: {}", 
                    registeredFaceFeature != null ? registeredFaceFeature.length() : 0, 
                    faceFeature != null ? faceFeature.length() : 0);
            
            double similarityScore = FaceCompareUtil.compareAndGetScore(registeredFaceFeature, faceFeature);
            faceMatch = similarityScore >= 60;
            faceMatchResult = faceMatch ? 0 : 1;
            logger.info("人脸比对结果: {}, 相似度分数: {}", faceMatch, similarityScore);
        }

        Map<String, Object> locationResult = null;
        int locationStatus = 2;
        int distance = 0;
        boolean locationValid = false;

        if (lat != null && lng != null) {
            locationResult = locationCheckService.checkLocation(lat, lng);
            locationStatus = (Integer) locationResult.get("locationStatus");
            distance = (Integer) locationResult.get("distance");
            locationValid = (Boolean) locationResult.get("locationValid");
            logger.info("位置校验结果: locationStatus={}, distance={}米, locationValid={}", 
                    locationStatus, distance, locationValid);
        } else {
            logger.warn("位置信息为空");
        }

        int signResult;
        String failReason = "";

        if (faceMatch && locationValid) {
            signResult = 0;
            failReason = "";
        } else {
            signResult = 1;
            StringBuilder reason = new StringBuilder();
            if (!faceMatch) {
                reason.append("人脸比对失败；");
            }
            if (!locationValid) {
                if (locationStatus == 2) {
                    reason.append("无法获取定位；");
                } else {
                    reason.append("定位超出范围；");
                }
            }
            failReason = reason.toString();
        }

        Attendance attendance = new Attendance();
        attendance.setStaffId(staff.getId());
        attendance.setStaffNo(staffNo);
        attendance.setStaffName(staff.getName());
        attendance.setSignType(signType);
        attendance.setSignTime(now);
        attendance.setStatus(calculateAttendanceStatus(signType, now));
        attendance.setFaceMatchResult(faceMatchResult);
        attendance.setSignLat(lat);
        attendance.setSignLng(lng);
        attendance.setDistance(distance);
        attendance.setLocationStatus(locationStatus);
        attendance.setSignResult(signResult);
        attendance.setFailReason(failReason);
        attendance.setDeviceInfo(deviceInfo);

        int insertResult = attendanceMapper.insert(attendance);
        if (insertResult <= 0) {
            logger.error("签到记录保存失败: {}", staffNo);
            return ApiResponse.error("签到记录保存失败");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("signResult", signResult);
        data.put("faceMatch", faceMatch);
        data.put("locationValid", locationValid);
        data.put("distance", distance);
        data.put("locationStatus", locationStatus);
        
        String message = signResult == 0 
                ? (signType == 1 ? "上班签到成功" : "下班签到成功")
                : failReason;

        logger.info("签到完成，员工: {}, 结果: {}, 人脸: {}, 定位: {}", 
                staffNo, signResult, faceMatch, locationValid);

        return signResult == 0 
                ? ApiResponse.success(message, data)
                : ApiResponse.error(message, data);
    }

    private int getSignType(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 12 ? 1 : 2;
    }

    private boolean isDuplicateSign(Long staffId, int signType, Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();

        Attendance existing = attendanceMapper.selectTodaySign(staffId, signType, startTime, endTime);
        if (existing != null) {
            if (existing.getSignResult() == 0) {
                return true;
            }
        }
        return false;
    }

    private int calculateAttendanceStatus(int signType, Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (signType == 1) {
            if (hour < 8 || (hour == 8 && minute <= 30)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (hour >= 18) {
                return 0;
            } else {
                return 2;
            }
        }
    }

    public ApiResponse<Object> verifyFace(String staffNo, String faceImage) {
        logger.info("========== 开始人脸验证 ==========");
        logger.info("员工编号: {}", staffNo);

        Staff staff = staffMapper.selectByStaffNo(staffNo);
        if (staff == null) {
            logger.warn("人脸验证失败，员工不存在: {}", staffNo);
            return ApiResponse.error("员工不存在");
        }

        String registeredFaceFeature = staff.getFaceFeature();
        if (registeredFaceFeature == null || registeredFaceFeature.isEmpty()) {
            logger.warn("人脸验证失败，员工未注册人脸: {}", staffNo);
            return ApiResponse.error("该员工未注册人脸，请先注册");
        }

        boolean isRegisteredWithImage = !registeredFaceFeature.startsWith("[");
        if (!isRegisteredWithImage) {
            logger.warn("人脸验证失败，员工人脸特征格式错误，特征以[开头: {}", staffNo);
            return ApiResponse.error("该员工未注册人脸，请先注册");
        }

        if (faceImage == null || faceImage.isEmpty()) {
            logger.warn("人脸验证失败，输入图片为空: {}", staffNo);
            return ApiResponse.error("人脸图片不能为空");
        }

        faceImage = stripBase64Prefix(faceImage);
        registeredFaceFeature = stripBase64Prefix(registeredFaceFeature);
        
        logger.info("已注册人脸特征长度: {}", registeredFaceFeature.length());
        logger.info("输入人脸图片长度: {}", faceImage.length());
        
        try {
            String matchedUserId = baiduFaceService.searchFace(faceImage);
            
            if (matchedUserId != null && matchedUserId.equals(staffNo)) {
                logger.info("百度AI人脸验证成功，匹配用户ID: {}", matchedUserId);
                logger.info("========== 人脸验证结束 ==========");
                return ApiResponse.success("人脸识别成功");
            } else {
                logger.info("百度AI人脸验证结果: matchedUserId={}, staffNo={}", matchedUserId, staffNo);
                
                if (matchedUserId == null) {
                    logger.info("百度AI未找到匹配或未配置，使用简化验证模式");
                }
                
                java.util.Map<String, Integer> detailScores = new java.util.HashMap<>();
                double similarityScore = FaceCompareUtil.compareAndGetScore(registeredFaceFeature, faceImage, detailScores);
                boolean faceMatch = similarityScore >= 60;
                
                logger.info("人脸比对详细分数(基于PCA算法):");
                logger.info("  - PCA相似度: {}%", detailScores.get("pcaSimilarity"));
                logger.info("  - HOG特征相似度: {}%", detailScores.get("hogSimilarity"));
                logger.info("  - LBP特征相似度: {}%", detailScores.get("lbpSimilarity"));
                logger.info("  - 像素相似度: {}%", detailScores.get("pixelSimilarity"));
                logger.info("人脸比对综合相似度分数: {:.2f}%", similarityScore);
                logger.info("人脸比对结果: {}", faceMatch);
                logger.info("========== 人脸验证结束 ==========");

                if (faceMatch) {
                    return ApiResponse.success("人脸识别成功");
                } else {
                    logger.warn("人脸比对未通过，综合相似度: {:.2f}%，低于阈值60%", similarityScore);
                    return ApiResponse.error("人脸比对失败，请确保是本人进行签到");
                }
            }
        } catch (Exception e) {
            logger.error("人脸比对过程异常: {}", staffNo, e);
            return ApiResponse.error("人脸比对异常，请重试");
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
}