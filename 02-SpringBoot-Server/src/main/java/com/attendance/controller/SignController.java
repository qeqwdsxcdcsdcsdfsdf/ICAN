package com.attendance.controller;

import com.attendance.common.ApiResponse;
import com.attendance.entity.Attendance;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.service.SignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sign")
@CrossOrigin(origins = "*")
public class SignController {
    private static final Logger logger = LoggerFactory.getLogger(SignController.class);

    @Autowired
    private SignService signService;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @PostMapping("/in")
    public ApiResponse<Map<String, Object>> signIn(@RequestBody Map<String, Object> params) {
        String staffNo = (String) params.get("staffNo");
        Double latitude = params.get("latitude") != null ? ((Number) params.get("latitude")).doubleValue() : null;
        Double longitude = params.get("longitude") != null ? ((Number) params.get("longitude")).doubleValue() : null;
        String faceFeature = (String) params.get("faceFeature");
        
        logger.info("签到请求 - 员工: {}, 位置: ({}, {}), 人脸特征: {}", staffNo, latitude, longitude, 
                faceFeature != null ? "已提供(" + faceFeature.length() + "字符)" : "未提供");
        
        if (faceFeature != null && !faceFeature.isEmpty()) {
            faceFeature = stripBase64Prefix(faceFeature);
        }
        
        if (faceFeature == null || faceFeature.isEmpty()) {
            return ApiResponse.error("人脸图片为空，请重新进行人脸识别");
        }
        
        if (faceFeature.length() < 1000) {
            logger.error("人脸图片太短，长度: {}，内容: {}", faceFeature.length(), faceFeature);
            return ApiResponse.error("人脸图片数据无效，请重新进行人脸识别");
        }
        
        return signService.signIn(staffNo, faceFeature, latitude, longitude, null);
    }
    
    private String stripBase64Prefix(String base64) {
        if (base64 == null) return null;
        if (base64.startsWith("data:image/jpeg;base64,")) {
            base64 = base64.substring("data:image/jpeg;base64,".length());
        } else if (base64.startsWith("data:image/png;base64,")) {
            base64 = base64.substring("data:image/png;base64,".length());
        } else if (base64.startsWith("data:image/jpg;base64,")) {
            base64 = base64.substring("data:image/jpg;base64,".length());
        }
        return base64.replaceAll("\\s", "");
    }

    @PostMapping("/verify-face")
    public ApiResponse<Object> verifyFace(@RequestBody Map<String, String> params) {
        String staffNo = params.get("staffNo");
        String faceImage = params.get("faceImage");
        logger.info("人脸验证请求 - 员工: {}", staffNo);
        return signService.verifyFace(staffNo, faceImage);
    }

    @PostMapping("/delete")
    public ApiResponse<Object> deleteRecords(@RequestBody Map<String, Object> params) {
        List<?> ids = (List<?>) params.get("ids");
        logger.info("删除考勤记录请求 - 数量: {}, IDs: {}", ids != null ? ids.size() : 0, ids);
        
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error("请选择要删除的记录");
        }
        
        List<Long> longIds = new java.util.ArrayList<>();
        for (Object id : ids) {
            if (id instanceof Long) {
                longIds.add((Long) id);
            } else if (id instanceof Integer) {
                longIds.add(((Integer) id).longValue());
            } else if (id instanceof String) {
                longIds.add(Long.parseLong((String) id));
            }
        }
        
        logger.info("转换后的ID列表: {}", longIds);
        
        int result = attendanceMapper.deleteBatchIds(longIds);
        if (result > 0) {
            logger.info("删除考勤记录成功 - 数量: {}", result);
            return ApiResponse.success("删除成功，共删除 " + result + " 条记录");
        } else {
            return ApiResponse.error("删除失败");
        }
    }

    @GetMapping("/reverse-geocode")
    public ApiResponse<Object> reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        logger.info("反向地理编码请求: {}, {}", lat, lon);
        
        String address = getAddressFromCoords(lat, lon);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("display_name", address);
        result.put("address", address);
        
        logger.info("反向地理编码结果: {}", address);
        return ApiResponse.success(result);
    }
    
    private String getAddressFromCoords(double lat, double lon) {
        String province = getProvince(lat, lon);
        String city = getCity(lat, lon);
        
        if (province.isEmpty() && city.isEmpty()) {
            return String.format("%.6f, %.6f", lat, lon);
        } else if (city.isEmpty()) {
            return province;
        } else if (province.isEmpty()) {
            return city;
        } else if (province.equals(city)) {
            return city;
        } else {
            return province + " " + city;
        }
    }
    
    private String getProvince(double lat, double lon) {
        if (lat >= 38.0 && lat <= 42.0 && lon >= 115.0 && lon <= 118.0) return "河北省";
        if (lat >= 34.0 && lat <= 38.0 && lon >= 110.0 && lon <= 114.0) return "山西省";
        if (lat >= 40.0 && lat <= 43.0 && lon >= 112.0 && lon <= 116.0) return "内蒙古";
        if (lat >= 39.0 && lat <= 43.0 && lon >= 120.0 && lon <= 126.0) return "辽宁省";
        if (lat >= 41.0 && lat <= 44.0 && lon >= 122.0 && lon <= 128.0) return "吉林省";
        if (lat >= 43.0 && lat <= 48.0 && lon >= 122.0 && lon <= 135.0) return "黑龙江省";
        if (lat >= 30.0 && lat <= 35.0 && lon >= 115.0 && lon <= 122.0) return "江苏省";
        if (lat >= 28.0 && lat <= 33.0 && lon >= 116.0 && lon <= 120.0) return "安徽省";
        if (lat >= 24.0 && lat <= 30.0 && lon >= 115.0 && lon <= 120.0) return "福建省";
        if (lat >= 28.0 && lat <= 31.0 && lon >= 115.0 && lon <= 118.0) return "江西省";
        if (lat >= 34.0 && lat <= 38.0 && lon >= 114.0 && lon <= 122.0) return "山东省";
        if (lat >= 32.0 && lat <= 35.0 && lon >= 110.0 && lon <= 116.0) return "河南省";
        if (lat >= 29.0 && lat <= 33.0 && lon >= 110.0 && lon <= 116.0) return "湖北省";
        if (lat >= 24.0 && lat <= 30.0 && lon >= 110.0 && lon <= 115.0) return "湖南省";
        if (lat >= 20.0 && lat <= 25.0 && lon >= 110.0 && lon <= 115.0) return "广东省";
        if (lat >= 22.0 && lat <= 24.0 && lon >= 108.0 && lon <= 111.0) return "广西";
        if (lat >= 18.0 && lat <= 20.0 && lon >= 108.0 && lon <= 111.0) return "海南省";
        if (lat >= 29.0 && lat <= 33.0 && lon >= 102.0 && lon <= 108.0) return "四川省";
        if (lat >= 25.0 && lat <= 29.0 && lon >= 103.0 && lon <= 109.0) return "贵州省";
        if (lat >= 22.0 && lat <= 26.0 && lon >= 100.0 && lon <= 105.0) return "云南省";
        if (lat >= 26.0 && lat <= 30.0 && lon >= 91.0 && lon <= 98.0) return "西藏";
        if (lat >= 33.0 && lat <= 37.0 && lon >= 104.0 && lon <= 110.0) return "陕西省";
        if (lat >= 34.0 && lat <= 37.0 && lon >= 104.0 && lon <= 107.0) return "甘肃省";
        if (lat >= 35.0 && lat <= 40.0 && lon >= 98.0 && lon <= 104.0) return "青海省";
        if (lat >= 35.0 && lat <= 39.0 && lon >= 100.0 && lon <= 104.0) return "宁夏";
        if (lat >= 34.0 && lat <= 43.0 && lon >= 73.0 && lon <= 96.0) return "新疆";
        if (lat >= 22.0 && lat <= 23.0 && lon >= 113.0 && lon <= 114.0) return "澳门";
        if (lat >= 22.0 && lat <= 23.0 && lon >= 113.0 && lon <= 114.0) return "香港";
        if (lat >= 24.0 && lat <= 25.0 && lon >= 118.0 && lon <= 120.0) return "台湾";
        return "";
    }
    
    private String getCity(double lat, double lon) {
        if (Math.abs(lat - 39.9042) < 0.5 && Math.abs(lon - 116.4074) < 0.5) return "北京市";
        if (Math.abs(lat - 39.0842) < 0.5 && Math.abs(lon - 117.2009) < 0.5) return "天津市";
        if (Math.abs(lat - 30.5728) < 0.5 && Math.abs(lon - 104.0668) < 0.5) return "成都市";
        if (Math.abs(lat - 31.2304) < 0.5 && Math.abs(lon - 121.4737) < 0.5) return "上海市";
        if (Math.abs(lat - 23.1291) < 0.5 && Math.abs(lon - 113.2644) < 0.5) return "广州市";
        if (Math.abs(lat - 22.5431) < 0.5 && Math.abs(lon - 114.0579) < 0.5) return "深圳市";
        if (Math.abs(lat - 30.2741) < 0.5 && Math.abs(lon - 120.1551) < 0.5) return "杭州市";
        if (Math.abs(lat - 32.0603) < 0.5 && Math.abs(lon - 118.7969) < 0.5) return "南京市";
        if (Math.abs(lat - 34.7466) < 0.5 && Math.abs(lon - 113.6253) < 0.5) return "郑州市";
        if (Math.abs(lat - 36.0671) < 0.5 && Math.abs(lon - 120.3826) < 0.5) return "青岛市";
        if (Math.abs(lat - 31.8206) < 0.5 && Math.abs(lon - 117.2272) < 0.5) return "合肥市";
        if (Math.abs(lat - 28.2280) < 0.5 && Math.abs(lon - 112.9388) < 0.5) return "长沙市";
        if (Math.abs(lat - 30.5928) < 0.5 && Math.abs(lon - 114.3055) < 0.5) return "武汉市";
        if (Math.abs(lat - 25.0389) < 0.5 && Math.abs(lon - 102.7183) < 0.5) return "昆明市";
        if (Math.abs(lat - 29.5630) < 0.5 && Math.abs(lon - 106.5516) < 0.5) return "重庆市";
        if (Math.abs(lat - 37.5793) < 0.3 && Math.abs(lon - 112.7653) < 0.3) return "晋中市";
        if (Math.abs(lat - 37.8706) < 0.3 && Math.abs(lon - 112.5489) < 0.3) return "太原市";
        if (Math.abs(lat - 38.0423) < 0.5 && Math.abs(lon - 114.5149) < 0.5) return "石家庄市";
        if (Math.abs(lat - 41.8057) < 0.5 && Math.abs(lon - 123.4315) < 0.5) return "沈阳市";
        if (Math.abs(lat - 43.8256) < 0.5 && Math.abs(lon - 125.3245) < 0.5) return "长春市";
        if (Math.abs(lat - 45.8038) < 0.5 && Math.abs(lon - 126.5349) < 0.5) return "哈尔滨市";
        if (Math.abs(lat - 32.0153) < 0.5 && Math.abs(lon - 118.7969) < 0.5) return "南京市";
        if (Math.abs(lat - 26.0611) < 0.5 && Math.abs(lon - 119.2965) < 0.5) return "福州市";
        if (Math.abs(lat - 28.6823) < 0.5 && Math.abs(lon - 115.8579) < 0.5) return "南昌市";
        if (Math.abs(lat - 36.6512) < 0.5 && Math.abs(lon - 116.9847) < 0.5) return "济南市";
        if (Math.abs(lat - 29.5630) < 0.5 && Math.abs(lon - 106.5516) < 0.5) return "重庆市";
        if (Math.abs(lat - 27.7007) < 0.5 && Math.abs(lon - 106.6992) < 0.5) return "贵阳市";
        if (Math.abs(lat - 34.3416) < 0.5 && Math.abs(lon - 108.9398) < 0.5) return "西安市";
        if (Math.abs(lat - 36.0611) < 0.5 && Math.abs(lon - 103.8343) < 0.5) return "兰州市";
        if (Math.abs(lat - 36.6170) < 0.5 && Math.abs(lon - 101.7782) < 0.5) return "西宁市";
        if (Math.abs(lat - 38.4864) < 0.5 && Math.abs(lon - 106.2333) < 0.5) return "银川市";
        if (Math.abs(lat - 43.8256) < 0.5 && Math.abs(lon - 87.6168) < 0.5) return "乌鲁木齐市";
        if (Math.abs(lat - 91.1145) < 0.5 && Math.abs(lon - 29.6540) < 0.5) return "拉萨市";
        if (Math.abs(lat - 37.5793) < 0.3 && Math.abs(lon - 112.7653) < 0.3) return "晋中市";
        if (Math.abs(lat - 37.8706) < 0.3 && Math.abs(lon - 112.5489) < 0.3) return "太原市";
        if (Math.abs(lat - 37.4496) < 0.5 && Math.abs(lon - 112.5745) < 0.5) return "吕梁市";
        return "";
    }

    @GetMapping("/records")
    public ApiResponse<List<Attendance>> getRecords(
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String staffNo,
            @RequestParam(required = false) Integer locationStatus,
            @RequestParam(required = false) Integer signType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String date) {
        logger.info("查询考勤记录 - 员工ID: {}, 员工编号: {}, 定位状态: {}, 签到类型: {}, 关键词: {}, 日期: {}", 
                staffId, staffNo, locationStatus, signType, keyword, date);
        
        List<Attendance> records;

        if (keyword != null || date != null) {
            records = attendanceMapper.selectWithConditions(locationStatus, signType, keyword, date);
        } else if (staffNo != null && !staffNo.isEmpty()) {
            records = attendanceMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getStaffNo, staffNo)
                            .orderByDesc(Attendance::getSignTime));
        } else if (locationStatus != null) {
            records = attendanceMapper.selectAllWithLocationStatus(locationStatus);
        } else if (staffId != null) {
            records = attendanceMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getStaffId, staffId)
                            .orderByDesc(Attendance::getSignTime));
        } else {
            records = attendanceMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                            .orderByDesc(Attendance::getSignTime));
        }

        logger.info("查询到考勤记录: {} 条", records.size());
        return ApiResponse.success("查询成功", records);
    }
}