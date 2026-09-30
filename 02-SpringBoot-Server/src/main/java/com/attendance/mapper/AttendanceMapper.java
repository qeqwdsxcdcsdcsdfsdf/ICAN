package com.attendance.mapper;

import com.attendance.entity.Attendance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
    Attendance selectTodaySign(@Param("staffId") Long staffId, @Param("signType") Integer signType, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
    List<Attendance> selectByDateRange(@Param("staffId") Long staffId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    List<Attendance> selectAllWithLocationStatus(@Param("locationStatus") Integer locationStatus);
    List<Attendance> selectWithConditions(@Param("locationStatus") Integer locationStatus,
                                          @Param("signType") Integer signType,
                                          @Param("keyword") String keyword,
                                          @Param("date") String date);
}