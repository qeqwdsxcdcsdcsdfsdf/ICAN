# 人脸+GPS双重校验考勤系统

## 项目概述
基于Android APP + SpringBoot后端 + Vue管理后台的人脸+GPS双重校验考勤系统。

## 技术架构
```
┌─────────────────────────────────────────────────────────────────┐
│                     Android APP                               │
│  - 人脸采集录入                                                │
│  - GPS定位获取                                                │
│  - 签到功能（人脸+GPS双重校验）                                 │
├─────────────────────────────────────────────────────────────────┤
│                     SpringBoot后端                             │
│  - 人脸比对模块                                                │
│  - GPS距离计算模块（Haversine公式）                             │
│  - 签到业务逻辑                                                │
│  - RESTful API接口                                            │
├─────────────────────────────────────────────────────────────────┤
│                     Vue管理后台                                │
│  - 员工管理                                                   │
│  - 考勤记录查看                                               │
│  - 定位配置设置                                               │
└─────────────────────────────────────────────────────────────────┘
```

## 项目结构
```
FaceAttendanceGPSTotal/
├── 01-Android-App/          # Android打卡APP
│   └── app/src/main/java/com/attendance/
│       ├── activity/         # 活动页面
│       ├── face/             # 人脸检测工具
│       ├── location/         # GPS定位模块
│       ├── api/              # 网络请求接口
│       ├── util/             # 工具类
│       └── bean/             # 实体类
├── 02-SpringBoot-Server/    # 后端服务
│   └── src/main/java/com/attendance/
│       ├── controller/       # 控制器
│       ├── service/          # 服务层
│       ├── mapper/           # 数据访问
│       ├── entity/           # 实体类
│       └── util/             # 工具类（含GeoUtil距离计算）
├── 03-PC-Admin-Vue/         # 电脑管理后台
│   └── src/
│       ├── views/            # 页面组件
│       ├── api/              # API接口
│       └── components/       # 通用组件
└── doc/                      # 文档
    └── database.sql          # 数据库建表SQL
```

## 快速开始

### 1. 数据库配置
导入 `doc/database.sql` 到MySQL数据库。

### 2. 后端服务
```bash
cd 02-SpringBoot-Server
mvn spring-boot:run
```
访问地址: http://localhost:8080

### 3. 管理后台
```bash
cd 03-PC-Admin-Vue
npm install
npm run dev
```
访问地址: http://localhost:8081

### 4. Android APP
使用Android Studio打开 `01-Android-App` 目录，配置后端服务器IP地址后运行。

## 核心功能

### 人脸比对
- 使用特征向量比对算法
- 支持128维特征向量存储

### GPS定位校验
- Haversine球面距离计算公式
- 可配置打卡中心点和允许半径
- 实时计算签到点与中心点距离

### 双重校验规则
```
if(人脸比对通过 == true && 定位校验通过 == true){
    执行签到入库，判定打卡有效；
}else{
    签到失败，返回具体失败原因；
}
```

## API接口

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/admin/login | POST | 管理员登录 |
| /api/staff/login | POST | 员工登录 |
| /api/staff/list | GET | 获取员工列表 |
| /api/sign/in | POST | 签到（含人脸+GPS） |
| /api/sign/records | GET | 获取签到记录 |
| /api/location/config | GET/POST | 获取/更新定位配置 |
| /api/location/check | POST | 校验定位是否在范围内 |

## 默认账号

### 管理员
- 用户名: admin
- 密码: 123456

### 员工
- 工号: S001 / S002 / S003
- 密码: 123456