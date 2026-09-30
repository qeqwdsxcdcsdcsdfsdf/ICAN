package com.attendance.controller;

import com.attendance.common.ApiResponse;
import com.attendance.entity.LocationConfig;
import com.attendance.service.LocationCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*")
public class LocationConfigController {
    private static final Logger logger = LoggerFactory.getLogger(LocationConfigController.class);

    @Autowired
    private LocationCheckService locationCheckService;

    @GetMapping("/config")
    public ApiResponse<LocationConfig> getConfig() {
        logger.info("查询定位配置");
        LocationConfig config = locationCheckService.getConfig();
        if (config != null) {
            return ApiResponse.success("查询成功", config);
        } else {
            return ApiResponse.error("未找到定位配置");
        }
    }

    @PostMapping("/config")
    public ApiResponse<Object> updateConfig(@RequestBody LocationConfig config) {
        logger.info("更新定位配置 - 中心点: ({}, {}), 半径: {}米", 
                config.getLatitude(), config.getLongitude(), config.getRadius());
        boolean success = locationCheckService.updateConfig(config);
        if (success) {
            return ApiResponse.success("配置更新成功");
        } else {
            return ApiResponse.error("配置更新失败");
        }
    }

    @PostMapping("/check")
    public ApiResponse<Map<String, Object>> checkLocation(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        double latVal = 0;
        double lngVal = 0;
        
        if (latitude != null && longitude != null) {
            latVal = latitude;
            lngVal = longitude;
        } else if (lat != null && lng != null) {
            latVal = lat;
            lngVal = lng;
        }
        
        logger.info("定位范围检查 - 位置: ({}, {})", latVal, lngVal);
        Map<String, Object> result = locationCheckService.checkLocation(latVal, lngVal);
        if ((Boolean) result.get("locationValid")) {
            return ApiResponse.success("定位合规", result);
        } else {
            return ApiResponse.error((String) result.get("message"), result);
        }
    }
}