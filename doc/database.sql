CREATE DATABASE IF NOT EXISTS face_attendance_gps DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE face_attendance_gps;

DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

DROP TABLE IF EXISTS staff;
CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    staff_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    department VARCHAR(100) DEFAULT '' COMMENT '部门',
    position VARCHAR(50) DEFAULT '' COMMENT '职位',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    face_feature TEXT COMMENT '人脸特征向量（Base64编码）',
    face_image_path VARCHAR(500) DEFAULT '' COMMENT '人脸照片存储路径',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

DROP TABLE IF EXISTS location_config;
CREATE TABLE location_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    center_lat DECIMAL(10,6) NOT NULL COMMENT '打卡中心点纬度',
    center_lng DECIMAL(10,6) NOT NULL COMMENT '打卡中心点经度',
    check_radius INT NOT NULL DEFAULT 200 COMMENT '允许打卡半径（单位：米）',
    config_name VARCHAR(100) DEFAULT '默认配置' COMMENT '配置名称',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定位配置表';

DROP TABLE IF EXISTS attendance;
CREATE TABLE attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '签到记录ID',
    staff_id BIGINT NOT NULL COMMENT '员工ID',
    staff_no VARCHAR(50) NOT NULL COMMENT '工号',
    staff_name VARCHAR(50) NOT NULL COMMENT '姓名',
    sign_type TINYINT NOT NULL COMMENT '签到类型：1-上班 2-下班',
    sign_time DATETIME NOT NULL COMMENT '签到时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-迟到 2-早退 3-缺勤',
    face_match_result TINYINT NOT NULL COMMENT '人脸比对结果：0-通过 1-失败',
    sign_lat DECIMAL(10,6) COMMENT '签到时手机纬度',
    sign_lng DECIMAL(10,6) COMMENT '签到时手机经度',
    distance INT COMMENT '签到点与公司中心点实际距离（米）',
    location_status TINYINT DEFAULT 2 COMMENT '定位校验：0=定位合规 1=超出范围 2=获取定位失败',
    sign_result TINYINT NOT NULL COMMENT '最终签到结果：0-成功 1-失败',
    fail_reason VARCHAR(200) DEFAULT '' COMMENT '失败原因',
    device_info VARCHAR(500) DEFAULT '' COMMENT '设备信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

INSERT INTO admin (username, password, real_name) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员');

INSERT INTO location_config (center_lat, center_lng, check_radius, config_name) VALUES (39.9042, 116.4074, 200, '公司总部');

INSERT INTO staff (staff_no, name, password, department, position) VALUES ('S001', '张三', 'e10adc3949ba59abbe56e057f20f883e', '技术部', '工程师');
INSERT INTO staff (staff_no, name, password, department, position) VALUES ('S002', '李四', 'e10adc3949ba59abbe56e057f20f883e', '人事部', '专员');
INSERT INTO staff (staff_no, name, password, department, position) VALUES ('S003', '王五', 'e10adc3949ba59abbe56e057f20f883e', '财务部', '会计');