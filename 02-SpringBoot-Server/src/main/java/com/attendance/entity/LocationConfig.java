package com.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("location_config")
public class LocationConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer radius;
    private String address;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}