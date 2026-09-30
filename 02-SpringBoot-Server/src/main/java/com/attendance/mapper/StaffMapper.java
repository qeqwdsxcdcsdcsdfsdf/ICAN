package com.attendance.mapper;

import com.attendance.entity.Staff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
    Staff selectByStaffNo(@Param("staffNo") String staffNo);
}