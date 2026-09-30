package com.attendance.config;

import com.attendance.entity.LocationConfig;
import com.attendance.mapper.LocationConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private LocationConfigMapper locationConfigMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocationConfig config = locationConfigMapper.selectById(1L);
        if (config != null) {
            config.setLatitude(37.4496);
            config.setLongitude(112.5746);
            config.setRadius(5000);
            config.setAddress("默认考勤区域");
            locationConfigMapper.updateById(config);
        } else {
            config = new LocationConfig();
            config.setId(1L);
            config.setName("默认考勤地点");
            config.setLatitude(37.4496);
            config.setLongitude(112.5746);
            config.setRadius(5000);
            config.setAddress("默认考勤区域");
            locationConfigMapper.insert(config);
        }
    }
}