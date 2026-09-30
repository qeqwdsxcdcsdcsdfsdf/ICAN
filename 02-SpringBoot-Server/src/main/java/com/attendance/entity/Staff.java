package com.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String staffNo;
    private String name;
    private String password;
    private String department;
    private String position;
    private String phone;
    private String email;
    private String faceFeature;
    private String faceImagePath;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}