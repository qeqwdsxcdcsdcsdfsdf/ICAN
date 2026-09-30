package com.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("attendance")
public class Attendance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long staffId;
    private String staffNo;
    private String staffName;
    private Integer signType;
    private Date signTime;
    private Integer status;
    private Integer faceMatchResult;
    private Double signLat;
    private Double signLng;
    private Integer distance;
    private Integer locationStatus;
    private Integer signResult;
    private String failReason;
    private String deviceInfo;
    private Date createTime;
}