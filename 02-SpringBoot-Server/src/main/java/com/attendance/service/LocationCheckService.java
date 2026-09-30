package com.attendance.service;

import com.attendance.entity.LocationConfig;
import com.attendance.mapper.LocationConfigMapper;
import com.attendance.util.GeoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LocationCheckService {
    @Autowired
    private LocationConfigMapper locationConfigMapper;

    public Map<String, Object> checkLocation(double lat, double lng) {
        Map<String, Object> result = new HashMap<>();
        
        LocationConfig config = locationConfigMapper.selectById(1L);
        if (config == null) {
            result.put("success", false);
            result.put("message", "未配置打卡中心点");
            result.put("distance", 0);
            result.put("radius", 500);
            result.put("locationValid", false);
            result.put("locationStatus", 2);
            return result;
        }

        double centerLat = config.getLatitude();
        double centerLng = config.getLongitude();
        int radius = config.getRadius() != null ? config.getRadius() : 500;

        double distance = GeoUtil.getDistance(centerLat, centerLng, lat, lng);
        
        result.put("distance", (int) distance);
        result.put("radius", radius);
        result.put("centerLat", centerLat);
        result.put("centerLng", centerLng);
        
        if (distance <= radius) {
            result.put("locationStatus", 0);
            result.put("locationValid", true);
            result.put("message", "定位合规");
        } else {
            result.put("locationStatus", 1);
            result.put("locationValid", false);
            result.put("message", "超出打卡范围，距离中心点" + (int) distance + "米");
        }
        
        return result;
    }

    public LocationConfig getConfig() {
        return locationConfigMapper.selectById(1L);
    }

    public boolean updateConfig(LocationConfig config) {
        if (config.getId() == null) {
            config.setId(1L);
            return locationConfigMapper.insert(config) > 0;
        }
        return locationConfigMapper.updateById(config) > 0;
    }
}