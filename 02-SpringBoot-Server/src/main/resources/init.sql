CREATE TABLE IF NOT EXISTS admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO admin (username, password, real_name, phone) 
SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '13800138000' 
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE username = 'admin');

CREATE TABLE IF NOT EXISTS staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    face_feature TEXT,
    face_image_path VARCHAR(500),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO staff (staff_no, name, password, department, position, phone, face_feature, status) 
SELECT 's001', '张三', 'e10adc3949ba59abbe56e057f20f883e', '技术部', '工程师', '13800138001', '', 1 
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_no = 's001');

INSERT INTO staff (staff_no, name, password, department, position, phone, face_feature, status) 
SELECT 's002', '李四', 'e10adc3949ba59abbe56e057f20f883e', '人事部', '经理', '13800138002', '', 1 
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_no = 's002');

INSERT INTO staff (staff_no, name, password, department, position, phone, face_feature, status) 
SELECT 's003', '王五', 'e10adc3949ba59abbe56e057f20f883e', '财务部', '会计', '13800138003', '', 1 
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_no = 's003');

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    staff_no VARCHAR(50),
    staff_name VARCHAR(50),
    sign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    sign_type TINYINT NOT NULL,
    status TINYINT DEFAULT 0,
    face_match_result TINYINT DEFAULT 0,
    sign_lat DECIMAL(10, 7),
    sign_lng DECIMAL(10, 7),
    distance INT DEFAULT 0,
    location_status TINYINT DEFAULT 2,
    sign_result TINYINT DEFAULT 0,
    fail_reason VARCHAR(500),
    device_info VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS location_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    radius INT DEFAULT 5000,
    address VARCHAR(255),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO location_config (name, latitude, longitude, radius, address) 
SELECT '默认考勤地点', 37.4496, 112.5746, 5000, '默认考勤区域' 
WHERE NOT EXISTS (SELECT 1 FROM location_config);