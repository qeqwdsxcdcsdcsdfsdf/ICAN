from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
import os

def add_heading(doc, text, level):
    heading = doc.add_heading(text, level=level)
    heading.alignment = WD_ALIGN_PARAGRAPH.CENTER
    return heading

def add_normal_paragraph(doc, text, bold=False, font_size=12, alignment=WD_ALIGN_PARAGRAPH.LEFT):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = bold
    run.font.size = Pt(font_size)
    p.alignment = alignment
    return p

def add_table(doc, headers, data):
    table = doc.add_table(rows=len(data)+1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.style = 'Table Grid'
    
    for i, header in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = header
        for paragraph in cell.paragraphs:
            for run in paragraph.runs:
                run.bold = True
                run.font.size = Pt(11)
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    
    for row_idx, row_data in enumerate(data):
        for col_idx, cell_data in enumerate(row_data):
            cell = table.rows[row_idx+1].cells[col_idx]
            cell.text = str(cell_data)
            for paragraph in cell.paragraphs:
                for run in paragraph.runs:
                    run.font.size = Pt(11)
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    
    return table

def generate_requirement_spec(doc):
    doc.add_heading('需求规格说明书', level=1)
    
    add_heading(doc, '1. 引言', level=2)
    add_normal_paragraph(doc, '1.1 项目背景', bold=True)
    add_normal_paragraph(doc, '随着企业规模的不断扩大，传统的考勤方式（如指纹打卡、纸质签到）已经无法满足现代企业的管理需求。为了提高考勤管理效率，降低人工成本，开发一套基于人脸识别和GPS定位的智能考勤系统势在必行。本系统旨在通过人脸识别技术实现员工身份验证，并结合GPS定位技术确保员工在指定地点进行签到，从而有效防止代打卡现象，提高考勤管理的准确性和公正性。')
    
    add_normal_paragraph(doc, '1.2 编写目的', bold=True)
    add_normal_paragraph(doc, '本文档旨在详细描述人脸考勤签到系统的需求规格，包括功能需求、非功能需求、数据需求、外部接口等，为系统设计、开发和测试提供明确的依据。')
    
    add_normal_paragraph(doc, '1.3 适用范围', bold=True)
    add_normal_paragraph(doc, '本文档适用于人脸考勤签到系统的开发团队、测试团队、运维团队以及相关管理人员。')
    
    add_normal_paragraph(doc, '1.4 术语和参考资料', bold=True)
    add_normal_paragraph(doc, '人脸识别：通过计算机技术对人脸图像进行特征提取和比对，实现身份识别的技术。')
    add_normal_paragraph(doc, 'GPS定位：通过全球定位系统获取设备当前地理位置坐标的技术。')
    add_normal_paragraph(doc, 'PCA：主成分分析，一种常用的人脸特征提取算法。')
    add_normal_paragraph(doc, 'Retrofit：Android平台上的网络请求框架。')
    add_normal_paragraph(doc, 'Spring Boot：基于Spring框架的快速开发脚手架。')
    
    add_heading(doc, '2. 项目概述', level=2)
    add_normal_paragraph(doc, '2.1 系统目标', bold=True)
    add_normal_paragraph(doc, '本系统的主要目标是实现基于人脸识别和GPS定位的智能考勤管理，具体包括：')
    add_normal_paragraph(doc, '（1）员工人脸注册与识别功能')
    add_normal_paragraph(doc, '（2）GPS定位签到功能')
    add_normal_paragraph(doc, '（3）考勤记录管理功能')
    add_normal_paragraph(doc, '（4）员工信息管理功能')
    add_normal_paragraph(doc, '（5）定位范围配置功能')
    
    add_normal_paragraph(doc, '2.2 目标用户', bold=True)
    add_normal_paragraph(doc, '系统主要面向两类用户：')
    add_normal_paragraph(doc, '（1）管理员：负责员工信息管理、考勤记录查询、定位范围配置等')
    add_normal_paragraph(doc, '（2）普通员工：使用手机App进行人脸注册、签到等操作')
    
    add_normal_paragraph(doc, '2.3 运行环境', bold=True)
    add_normal_paragraph(doc, '服务器端：Windows/Linux操作系统，JDK 1.8+，MySQL 5.7+')
    add_normal_paragraph(doc, '客户端（PC管理端）：Windows操作系统，主流浏览器（Chrome、Firefox、Edge）')
    add_normal_paragraph(doc, '客户端（Android App）：Android 7.0+，支持摄像头和GPS定位')
    
    add_normal_paragraph(doc, '2.4 约束条件和假设', bold=True)
    add_normal_paragraph(doc, '（1）系统依赖网络连接，断网情况下无法进行签到')
    add_normal_paragraph(doc, '（2）人脸识别准确率受光线、角度等因素影响')
    add_normal_paragraph(doc, '（3）GPS定位精度受设备和环境影响，误差范围约为5-50米')
    
    add_heading(doc, '3. 用户角色与业务场景', level=2)
    add_normal_paragraph(doc, '3.1 用户角色', bold=True)
    add_normal_paragraph(doc, '管理员：拥有系统最高权限，可管理员工信息、查看考勤记录、配置定位范围。')
    add_normal_paragraph(doc, '普通员工：使用App进行人脸注册和日常签到。')
    
    add_normal_paragraph(doc, '3.2 业务场景', bold=True)
    add_normal_paragraph(doc, '场景1：员工入职注册', bold=True)
    add_normal_paragraph(doc, '管理员在PC管理端录入员工信息，员工在App端注册人脸后即可开始使用系统。')
    
    add_normal_paragraph(doc, '场景2：日常签到', bold=True)
    add_normal_paragraph(doc, '员工到达指定签到地点后，打开App进行人脸识别，识别成功后自动获取GPS位置并完成签到。')
    
    add_normal_paragraph(doc, '场景3：考勤统计', bold=True)
    add_normal_paragraph(doc, '管理员定期在PC管理端查看考勤记录，进行统计和分析。')
    
    add_heading(doc, '4. 业务流程', level=2)
    add_normal_paragraph(doc, '4.1 业务流程分析', bold=True)
    add_normal_paragraph(doc, '【写作提示】用文字、流程图或编号步骤说明核心业务流程、分支流程和异常流程。')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '图1人脸考勤业务流程图')
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的业务流程图，包含以下流程：')
    add_normal_paragraph(doc, '1. 员工打开App，登录系统')
    add_normal_paragraph(doc, '2. 点击"注册人脸"，拍摄人脸照片')
    add_normal_paragraph(doc, '3. 系统提取人脸特征，保存到服务器')
    add_normal_paragraph(doc, '4. 点击"立即签到"，系统进行人脸识别')
    add_normal_paragraph(doc, '5. 人脸识别成功后，获取GPS定位')
    add_normal_paragraph(doc, '6. 检查是否在允许的签到范围内')
    add_normal_paragraph(doc, '7. 在范围内则签到成功，记录考勤信息')
    add_normal_paragraph(doc, '8. 不在范围内则提示"超出签到范围"')
    add_normal_paragraph(doc, '9. 人脸识别失败则提示"请注册人脸或重试"')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图2员工登录流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的员工登录流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 员工打开App')
    add_normal_paragraph(doc, '2. 检查是否已登录')
    add_normal_paragraph(doc, '3. 已登录：直接进入主界面')
    add_normal_paragraph(doc, '4. 未登录：显示登录页面')
    add_normal_paragraph(doc, '5. 输入工号和密码')
    add_normal_paragraph(doc, '6. 点击"登录"按钮')
    add_normal_paragraph(doc, '7. 发送登录请求到服务器')
    add_normal_paragraph(doc, '8. 服务器验证账号密码')
    add_normal_paragraph(doc, '9. 验证失败：提示"工号或密码错误"')
    add_normal_paragraph(doc, '10. 验证成功：保存用户信息到本地')
    add_normal_paragraph(doc, '11. 进入主界面')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图3人脸注册流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的人脸注册流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 员工登录App')
    add_normal_paragraph(doc, '2. 进入签到页面')
    add_normal_paragraph(doc, '3. 检查是否已注册人脸')
    add_normal_paragraph(doc, '4. 已注册：显示"已注册人脸"提示')
    add_normal_paragraph(doc, '5. 未注册：显示"注册人脸"按钮')
    add_normal_paragraph(doc, '6. 点击"注册人脸"按钮')
    add_normal_paragraph(doc, '7. 调用系统摄像头')
    add_normal_paragraph(doc, '8. 拍摄人脸照片')
    add_normal_paragraph(doc, '9. 将照片转换为Base64编码')
    add_normal_paragraph(doc, '10. 发送人脸注册请求到服务器')
    add_normal_paragraph(doc, '11. 服务器提取人脸特征')
    add_normal_paragraph(doc, '12. 保存人脸特征到数据库')
    add_normal_paragraph(doc, '13. 返回注册成功结果')
    add_normal_paragraph(doc, '14. 提示"人脸注册成功"')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图4员工管理流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的员工管理流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 管理员登录PC管理端')
    add_normal_paragraph(doc, '2. 进入员工管理页面')
    add_normal_paragraph(doc, '3. 查看员工列表')
    add_normal_paragraph(doc, '4. 选择操作：新增/编辑/删除')
    add_normal_paragraph(doc, '5. 新增员工：填写员工信息表单')
    add_normal_paragraph(doc, '6. 编辑员工：修改员工信息')
    add_normal_paragraph(doc, '7. 删除员工：确认删除操作')
    add_normal_paragraph(doc, '8. 提交操作请求到服务器')
    add_normal_paragraph(doc, '9. 服务器处理请求')
    add_normal_paragraph(doc, '10. 更新数据库')
    add_normal_paragraph(doc, '11. 返回操作结果')
    add_normal_paragraph(doc, '12. 刷新员工列表')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_heading(doc, '5. 功能需求', level=2)
    add_normal_paragraph(doc, '【写作提示】按需求编号描述功能，建议写清输入、处理、输出、优先级和验收标准。')
    add_normal_paragraph(doc, '【填写内容】')
    
    headers = ['需求编号', '需求名称', '用户角色', '输入', '处理逻辑', '输出', '优先级', '验收标准']
    data = [
        ['FR-001', '员工登录', '普通员工', '工号、密码', '系统验证工号和密码是否正确', '登录成功进入主界面，失败提示错误信息', '高', '输入正确账号密码可成功登录，错误账号密码提示登录失败'],
        ['FR-002', '人脸注册', '普通员工', '人脸照片', '系统提取人脸特征并保存到数据库', '注册成功提示，失败提示错误信息', '高', '拍摄清晰人脸照片后可成功注册'],
        ['FR-003', '人脸签到', '普通员工', '人脸照片、GPS坐标', '系统进行人脸识别和位置验证', '签到成功显示结果，失败提示原因', '高', '注册人脸后可成功签到，未注册提示需先注册'],
        ['FR-004', 'GPS定位', '普通员工', '无', '系统获取设备当前位置坐标', '显示当前位置名称和距离签到点距离', '高', '可准确获取当前位置并计算距离'],
        ['FR-005', '员工管理', '管理员', '员工信息（工号、姓名、部门等）', '系统新增、编辑、删除员工信息', '操作成功提示，失败提示错误信息', '高', '管理员可正常增删改查员工信息'],
        ['FR-006', '考勤记录查询', '管理员', '查询条件（员工、日期等）', '系统根据条件查询考勤记录', '显示考勤记录列表', '高', '可按条件查询并显示考勤记录'],
        ['FR-007', '定位范围配置', '管理员', '签到点坐标和范围', '系统保存定位配置信息', '配置成功提示', '高', '管理员可配置签到点和允许的签到范围'],
        ['FR-008', '密码修改', '普通员工', '原密码、新密码', '系统验证原密码并更新新密码', '修改成功提示', '中', '员工可修改自己的登录密码'],
        ['FR-009', '员工信息查看', '普通员工', '无', '系统查询并返回当前用户信息', '显示员工详细信息', '中', '员工可查看自己的个人信息'],
        ['FR-010', '批量删除考勤记录', '管理员', '选中的记录ID', '系统批量删除选中的考勤记录', '删除成功提示', '中', '管理员可批量删除考勤记录'],
    ]
    add_table(doc, headers, data)
    
    add_heading(doc, '6. 非功能需求', level=2)
    add_normal_paragraph(doc, '【写作提示】描述性能、安全、易用性、可靠性、兼容性、可维护性等质量要求。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）性能需求', bold=True)
    add_normal_paragraph(doc, '人脸识别响应时间不超过3秒')
    add_normal_paragraph(doc, '签到请求响应时间不超过5秒')
    add_normal_paragraph(doc, '系统支持同时在线用户数不少于100人')
    
    add_normal_paragraph(doc, '（2）安全需求', bold=True)
    add_normal_paragraph(doc, '用户密码采用MD5加密存储')
    add_normal_paragraph(doc, '人脸图片传输采用Base64编码')
    add_normal_paragraph(doc, '后端接口进行参数校验和异常处理')
    
    add_normal_paragraph(doc, '（3）易用性需求', bold=True)
    add_normal_paragraph(doc, '界面简洁明了，操作流程简单直观')
    add_normal_paragraph(doc, '提供清晰的错误提示信息')
    
    add_normal_paragraph(doc, '（4）可靠性需求', bold=True)
    add_normal_paragraph(doc, '系统稳定性高，支持7×24小时运行')
    add_normal_paragraph(doc, '关键数据进行持久化存储')
    
    add_normal_paragraph(doc, '（5）兼容性需求', bold=True)
    add_normal_paragraph(doc, 'Android App支持Android 7.0及以上版本')
    add_normal_paragraph(doc, 'PC管理端支持主流浏览器')
    
    add_heading(doc, '7. 数据需求', level=2)
    add_normal_paragraph(doc, '【写作提示】说明主要数据对象、字段含义、数据来源、数据保存和数据约束。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）员工信息表（staff）', bold=True)
    add_normal_paragraph(doc, 'staff_id：员工ID（主键）')
    add_normal_paragraph(doc, 'staff_no：工号（唯一）')
    add_normal_paragraph(doc, 'name：姓名')
    add_normal_paragraph(doc, 'password：密码（MD5加密）')
    add_normal_paragraph(doc, 'department：部门')
    add_normal_paragraph(doc, 'position：职位')
    add_normal_paragraph(doc, 'phone：电话')
    add_normal_paragraph(doc, 'email：邮箱')
    add_normal_paragraph(doc, 'face_feature：人脸特征数据')
    add_normal_paragraph(doc, 'status：状态（启用/禁用）')
    
    add_normal_paragraph(doc, '（2）考勤记录表（attendance）', bold=True)
    add_normal_paragraph(doc, 'attendance_id：考勤ID（主键）')
    add_normal_paragraph(doc, 'staff_id：员工ID（外键）')
    add_normal_paragraph(doc, 'staff_no：工号')
    add_normal_paragraph(doc, 'sign_time：签到时间')
    add_normal_paragraph(doc, 'latitude：纬度')
    add_normal_paragraph(doc, 'longitude：经度')
    add_normal_paragraph(doc, 'location_name：地点名称')
    add_normal_paragraph(doc, 'distance：距离签到点距离（米）')
    add_normal_paragraph(doc, 'sign_type：签到类型（上班/下班）')
    add_normal_paragraph(doc, 'location_status：定位状态（在范围内/超出范围）')
    
    add_normal_paragraph(doc, '（3）定位配置表（location_config）', bold=True)
    add_normal_paragraph(doc, 'config_id：配置ID（主键）')
    add_normal_paragraph(doc, 'latitude：签到点纬度')
    add_normal_paragraph(doc, 'longitude：签到点经度')
    add_normal_paragraph(doc, 'radius：允许签到范围（米）')
    add_normal_paragraph(doc, 'location_name：地点名称')
    
    add_heading(doc, '8. 外部接口与依赖', level=2)
    add_normal_paragraph(doc, '【写作提示】说明第三方API、模型、摄像头、文件系统、数据库或其他外部依赖。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）Nominatim反向地理编码API', bold=True)
    add_normal_paragraph(doc, '用途：根据GPS坐标获取地点名称')
    add_normal_paragraph(doc, '调用方式：HTTP GET请求')
    
    add_normal_paragraph(doc, '（2）Android摄像头', bold=True)
    add_normal_paragraph(doc, '用途：拍摄人脸照片')
    
    add_normal_paragraph(doc, '（3）Android GPS定位', bold=True)
    add_normal_paragraph(doc, '用途：获取设备当前位置')
    
    add_normal_paragraph(doc, '（4）MySQL数据库', bold=True)
    add_normal_paragraph(doc, '用途：存储员工信息、考勤记录、定位配置')
    
    add_normal_paragraph(doc, '（5）Retrofit网络框架', bold=True)
    add_normal_paragraph(doc, '用途：Android端网络请求')
    
    add_normal_paragraph(doc, '（6）Spring Boot框架', bold=True)
    add_normal_paragraph(doc, '用途：后端服务开发')
    
    add_heading(doc, '9. 验收标准', level=2)
    add_normal_paragraph(doc, '【写作提示】列出课程验收时必须演示并通过的内容。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）员工登录功能正常')
    add_normal_paragraph(doc, '（2）人脸注册功能正常')
    add_normal_paragraph(doc, '（3）人脸签到功能正常，人脸识别准确率≥95%')
    add_normal_paragraph(doc, '（4）GPS定位功能正常，可显示当前位置和距离')
    add_normal_paragraph(doc, '（5）管理员可管理员工信息')
    add_normal_paragraph(doc, '（6）管理员可查看考勤记录')
    add_normal_paragraph(doc, '（7）管理员可配置定位范围')
    add_normal_paragraph(doc, '（8）系统响应时间满足性能需求')
    add_normal_paragraph(doc, '（9）系统界面美观，操作流畅')

def generate_design_spec(doc):
    doc.add_heading('软件设计说明书', level=1)
    
    add_heading(doc, '1. 设计目标与范围', level=2)
    add_normal_paragraph(doc, '【写作提示】说明本设计对应哪些需求，哪些内容纳入本次实现，哪些暂不实现。')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '本设计实现需求规格说明书中的全部功能需求，包括：')
    add_normal_paragraph(doc, '（1）员工登录与认证')
    add_normal_paragraph(doc, '（2）人脸注册与识别')
    add_normal_paragraph(doc, '（3）GPS定位签到')
    add_normal_paragraph(doc, '（4）员工信息管理')
    add_normal_paragraph(doc, '（5）考勤记录管理')
    add_normal_paragraph(doc, '（6）定位范围配置')
    
    add_heading(doc, '2. 总体架构设计', level=2)
    add_normal_paragraph(doc, '【写作提示】说明前端、后端、数据库、模型服务、移动端或第三方接口之间的关系，可配架构图。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '系统采用前后端分离架构，主要分为三个部分：', bold=True)
    add_normal_paragraph(doc, '（1）Android客户端：员工使用的移动端应用，负责人脸识别、GPS定位、签到等功能')
    add_normal_paragraph(doc, '（2）PC管理端：管理员使用的Web应用，负责员工管理、考勤查看、配置管理等功能')
    add_normal_paragraph(doc, '（3）后端服务：提供RESTful API，处理业务逻辑和数据存储')
    
    add_normal_paragraph(doc, '【生成架构图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的系统架构图，包含以下组件：')
    add_normal_paragraph(doc, '1. Android客户端（员工使用）')
    add_normal_paragraph(doc, '2. PC管理端（管理员使用）')
    add_normal_paragraph(doc, '3. Spring Boot后端服务')
    add_normal_paragraph(doc, '4. MySQL数据库')
    add_normal_paragraph(doc, '5. Nominatim反向地理编码API')
    add_normal_paragraph(doc, '请使用mermaid语法绘制架构图，展示各组件之间的交互关系。')
    
    add_heading(doc, '3. 模块设计', level=2)
    add_normal_paragraph(doc, '【写作提示】按模块说明职责、输入输出、主要类/函数、调用关系和关联需求。')
    add_normal_paragraph(doc, '【填写内容】')
    
    headers = ['模块编号', '模块名称', '主要职责', '输入', '输出', '关联需求']
    data = [
        ['M-01', '用户认证模块', '处理用户登录、密码验证', '工号、密码', '登录成功/失败结果', 'FR-001'],
        ['M-02', '人脸注册模块', '人脸照片上传和特征提取', '人脸照片', '注册成功/失败结果', 'FR-002'],
        ['M-03', '人脸识别模块', '人脸比对和身份验证', '人脸照片', '识别结果（成功/失败）', 'FR-003'],
        ['M-04', 'GPS定位模块', '获取设备位置和距离计算', '无', '位置坐标、距离、地点名称', 'FR-004'],
        ['M-05', '签到模块', '处理签到逻辑', '员工ID、人脸特征、位置', '签到成功/失败结果', 'FR-003'],
        ['M-06', '员工管理模块', '员工信息增删改查', '员工信息', '操作结果', 'FR-005'],
        ['M-07', '考勤记录模块', '考勤记录查询和管理', '查询条件', '考勤记录列表', 'FR-006, FR-010'],
        ['M-08', '定位配置模块', '签到点配置管理', '配置信息', '操作结果', 'FR-007'],
        ['M-09', '密码修改模块', '用户密码修改', '原密码、新密码', '修改结果', 'FR-008'],
        ['M-10', '员工信息模块', '员工个人信息查看', '员工ID', '员工详细信息', 'FR-009'],
    ]
    add_table(doc, headers, data)
    
    add_heading(doc, '4. 数据库或数据结构设计', level=2)
    add_normal_paragraph(doc, '【写作提示】说明E-R图、数据表、字段、主外键、约束、样例数据；无需数据库的项目说明数据结构。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '【生成E-R图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的数据库E-R图，包含以下实体和关系：')
    add_normal_paragraph(doc, '1. 员工（staff）：staff_id(主键), staff_no, name, password, department, position, phone, email, face_feature, status')
    add_normal_paragraph(doc, '2. 考勤记录（attendance）：attendance_id(主键), staff_id(外键), staff_no, sign_time, latitude, longitude, location_name, distance, sign_type, location_status')
    add_normal_paragraph(doc, '3. 定位配置（location_config）：config_id(主键), latitude, longitude, radius, location_name')
    add_normal_paragraph(doc, '关系：员工 1:N 考勤记录')
    add_normal_paragraph(doc, '请使用mermaid语法绘制E-R图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '表1 员工信息表（staff）', bold=True)
    staff_headers = ['字段名', '类型', '约束', '说明']
    staff_data = [
        ['staff_id', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '员工ID'],
        ['staff_no', 'VARCHAR(20)', 'UNIQUE, NOT NULL', '工号'],
        ['name', 'VARCHAR(50)', 'NOT NULL', '姓名'],
        ['password', 'VARCHAR(32)', 'NOT NULL', '密码（MD5）'],
        ['department', 'VARCHAR(50)', '', '部门'],
        ['position', 'VARCHAR(50)', '', '职位'],
        ['phone', 'VARCHAR(20)', '', '电话'],
        ['email', 'VARCHAR(100)', '', '邮箱'],
        ['face_feature', 'TEXT', '', '人脸特征数据'],
        ['status', 'INT', 'DEFAULT 1', '状态（1启用/0禁用）'],
        ['create_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
        ['update_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', '更新时间'],
    ]
    add_table(doc, staff_headers, staff_data)
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '表2 考勤记录表（attendance）', bold=True)
    attendance_headers = ['字段名', '类型', '约束', '说明']
    attendance_data = [
        ['attendance_id', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '考勤ID'],
        ['staff_id', 'INT', 'NOT NULL', '员工ID'],
        ['staff_no', 'VARCHAR(20)', 'NOT NULL', '工号'],
        ['sign_time', 'DATETIME', 'NOT NULL', '签到时间'],
        ['latitude', 'DECIMAL(10,7)', 'NOT NULL', '纬度'],
        ['longitude', 'DECIMAL(10,7)', 'NOT NULL', '经度'],
        ['location_name', 'VARCHAR(200)', '', '地点名称'],
        ['distance', 'DECIMAL(10,2)', '', '距离签到点距离（米）'],
        ['sign_type', 'VARCHAR(10)', 'DEFAULT "上班"', '签到类型'],
        ['location_status', 'VARCHAR(20)', '', '定位状态'],
        ['create_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
    ]
    add_table(doc, attendance_headers, attendance_data)
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '表3 定位配置表（location_config）', bold=True)
    config_headers = ['字段名', '类型', '约束', '说明']
    config_data = [
        ['config_id', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '配置ID'],
        ['latitude', 'DECIMAL(10,7)', 'NOT NULL', '签到点纬度'],
        ['longitude', 'DECIMAL(10,7)', 'NOT NULL', '签到点经度'],
        ['radius', 'INT', 'DEFAULT 500', '允许签到范围（米）'],
        ['location_name', 'VARCHAR(100)', '', '地点名称'],
        ['create_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
        ['update_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', '更新时间'],
    ]
    add_table(doc, config_headers, config_data)
    
    add_heading(doc, '5. 接口设计', level=2)
    add_normal_paragraph(doc, '【写作提示】说明主要API、函数接口或模型调用接口，包括参数、返回值和异常情况。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）登录接口', bold=True)
    add_normal_paragraph(doc, 'POST /api/staff/login')
    add_normal_paragraph(doc, '参数：staffNo（工号）、password（密码）')
    add_normal_paragraph(doc, '返回：成功返回员工信息，失败返回错误信息')
    
    add_normal_paragraph(doc, '（2）人脸注册接口', bold=True)
    add_normal_paragraph(doc, 'POST /api/staff/register-face')
    add_normal_paragraph(doc, '参数：staffNo（工号）、faceFeature（人脸图片Base64）')
    add_normal_paragraph(doc, '返回：成功/失败结果')
    
    add_normal_paragraph(doc, '（3）人脸签到接口', bold=True)
    add_normal_paragraph(doc, 'POST /api/sign/in')
    add_normal_paragraph(doc, '参数：staffNo（工号）、faceFeature（人脸图片Base64）、latitude（纬度）、longitude（经度）')
    add_normal_paragraph(doc, '返回：签到结果（包含地点名称、距离等）')
    
    add_normal_paragraph(doc, '（4）查询考勤记录接口', bold=True)
    add_normal_paragraph(doc, 'GET /api/sign/records')
    add_normal_paragraph(doc, '参数：staffId（员工ID）、staffNo（工号）、startDate（开始日期）、endDate（结束日期）')
    add_normal_paragraph(doc, '返回：考勤记录列表')
    
    add_normal_paragraph(doc, '（5）获取定位配置接口', bold=True)
    add_normal_paragraph(doc, 'GET /api/location/config')
    add_normal_paragraph(doc, '参数：无')
    add_normal_paragraph(doc, '返回：定位配置信息')
    
    add_normal_paragraph(doc, '（6）保存定位配置接口', bold=True)
    add_normal_paragraph(doc, 'POST /api/location/config')
    add_normal_paragraph(doc, '参数：latitude（纬度）、longitude（经度）、radius（范围）、locationName（地点名称）')
    add_normal_paragraph(doc, '返回：保存结果')
    
    add_normal_paragraph(doc, '（7）检查位置接口', bold=True)
    add_normal_paragraph(doc, 'POST /api/location/check')
    add_normal_paragraph(doc, '参数：latitude（纬度）、longitude（经度）')
    add_normal_paragraph(doc, '返回：位置检查结果（是否在范围内、距离等）')
    
    add_heading(doc, '6. 关键流程设计', level=2)
    add_normal_paragraph(doc, '【写作提示】说明登录、提交、审核、检测、统计、游戏循环等关键流程。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）登录流程', bold=True)
    add_normal_paragraph(doc, '① 用户输入工号和密码')
    add_normal_paragraph(doc, '② 客户端发送登录请求到后端')
    add_normal_paragraph(doc, '③ 后端验证工号和密码')
    add_normal_paragraph(doc, '④ 返回登录结果')
    add_normal_paragraph(doc, '⑤ 登录成功后保存用户信息到本地')
    
    add_normal_paragraph(doc, '（2）人脸注册流程', bold=True)
    add_normal_paragraph(doc, '① 用户点击"注册人脸"')
    add_normal_paragraph(doc, '② 调用摄像头拍摄人脸照片')
    add_normal_paragraph(doc, '③ 将照片转换为Base64编码')
    add_normal_paragraph(doc, '④ 发送人脸注册请求到后端')
    add_normal_paragraph(doc, '⑤ 后端提取人脸特征并保存')
    add_normal_paragraph(doc, '⑥ 返回注册结果')
    
    add_normal_paragraph(doc, '（3）签到流程', bold=True)
    add_normal_paragraph(doc, '① 用户点击"立即签到"')
    add_normal_paragraph(doc, '② 调用摄像头拍摄人脸照片')
    add_normal_paragraph(doc, '③ 发送人脸识别请求到后端')
    add_normal_paragraph(doc, '④ 后端进行人脸比对')
    add_normal_paragraph(doc, '⑤ 识别成功后获取GPS定位')
    add_normal_paragraph(doc, '⑥ 检查位置是否在允许范围内')
    add_normal_paragraph(doc, '⑦ 在范围内则保存考勤记录')
    add_normal_paragraph(doc, '⑧ 返回签到结果')
    
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的签到流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 用户点击"立即签到"')
    add_normal_paragraph(doc, '2. 系统调用摄像头拍摄人脸照片')
    add_normal_paragraph(doc, '3. 将照片发送到后端进行人脸识别')
    add_normal_paragraph(doc, '4. 后端提取人脸特征并与数据库中保存的特征比对')
    add_normal_paragraph(doc, '5. 识别失败：提示"请注册人脸或重试"')
    add_normal_paragraph(doc, '6. 识别成功：获取当前GPS定位')
    add_normal_paragraph(doc, '7. 检查位置是否在允许的签到范围内')
    add_normal_paragraph(doc, '8. 超出范围：提示"超出签到范围，当前距离XX米"')
    add_normal_paragraph(doc, '9. 在范围内：查询定位配置，计算距离')
    add_normal_paragraph(doc, '10. 保存考勤记录到数据库')
    add_normal_paragraph(doc, '11. 返回签到成功结果')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图4登录流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的登录流程图（详细版），包含以下步骤：')
    add_normal_paragraph(doc, '1. 用户打开App')
    add_normal_paragraph(doc, '2. 检查本地存储的登录状态')
    add_normal_paragraph(doc, '3. 已登录且token有效：直接跳转主界面')
    add_normal_paragraph(doc, '4. 未登录或token过期：显示登录页面')
    add_normal_paragraph(doc, '5. 用户输入工号和密码')
    add_normal_paragraph(doc, '6. 前端验证：工号和密码不能为空')
    add_normal_paragraph(doc, '7. 验证失败：提示错误信息')
    add_normal_paragraph(doc, '8. 验证成功：发送POST请求到/api/staff/login')
    add_normal_paragraph(doc, '9. 后端接收请求')
    add_normal_paragraph(doc, '10. 查询数据库：根据工号查找员工')
    add_normal_paragraph(doc, '11. 员工不存在：返回"工号或密码错误"')
    add_normal_paragraph(doc, '12. 员工存在：验证密码（MD5比对）')
    add_normal_paragraph(doc, '13. 密码错误：返回"工号或密码错误"')
    add_normal_paragraph(doc, '14. 密码正确：返回员工信息')
    add_normal_paragraph(doc, '15. 前端保存员工信息到SharedPreferences')
    add_normal_paragraph(doc, '16. 跳转主界面')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图5人脸注册流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的人脸注册流程图（详细版），包含以下步骤：')
    add_normal_paragraph(doc, '1. 用户进入签到页面')
    add_normal_paragraph(doc, '2. 检查本地是否已保存人脸注册状态')
    add_normal_paragraph(doc, '3. 已注册：禁用"注册人脸"按钮，显示"已注册"')
    add_normal_paragraph(doc, '4. 未注册：启用"注册人脸"按钮')
    add_normal_paragraph(doc, '5. 用户点击"注册人脸"按钮')
    add_normal_paragraph(doc, '6. 请求相机权限')
    add_normal_paragraph(doc, '7. 权限被拒绝：提示"需要相机权限"')
    add_normal_paragraph(doc, '8. 权限授予：打开相机预览')
    add_normal_paragraph(doc, '9. 用户拍摄人脸照片')
    add_normal_paragraph(doc, '10. 将照片压缩并转换为Base64编码')
    add_normal_paragraph(doc, '11. 发送POST请求到/api/staff/register-face')
    add_normal_paragraph(doc, '12. 后端接收请求')
    add_normal_paragraph(doc, '13. 验证人脸图片长度（不少于1000字符）')
    add_normal_paragraph(doc, '14. 图片无效：返回"人脸照片无效"')
    add_normal_paragraph(doc, '15. 图片有效：提取人脸特征（PCA算法）')
    add_normal_paragraph(doc, '16. 更新员工表face_feature字段')
    add_normal_paragraph(doc, '17. 返回注册成功结果')
    add_normal_paragraph(doc, '18. 前端保存注册状态到本地')
    add_normal_paragraph(doc, '19. 提示"人脸注册成功"')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图6考勤记录查询流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的考勤记录查询流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 管理员登录PC管理端')
    add_normal_paragraph(doc, '2. 进入考勤记录页面')
    add_normal_paragraph(doc, '3. 设置查询条件：员工编号、日期范围、定位状态、签到类型')
    add_normal_paragraph(doc, '4. 点击"查询"按钮')
    add_normal_paragraph(doc, '5. 发送GET请求到/api/sign/records')
    add_normal_paragraph(doc, '6. 后端接收请求')
    add_normal_paragraph(doc, '7. 构建SQL查询条件')
    add_normal_paragraph(doc, '8. 查询考勤记录表')
    add_normal_paragraph(doc, '9. 分页处理')
    add_normal_paragraph(doc, '10. 返回考勤记录列表')
    add_normal_paragraph(doc, '11. 前端渲染表格')
    add_normal_paragraph(doc, '12. 显示考勤记录详情')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '图7定位配置流程图', bold=True)
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '【生成流程图的AI提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个人脸考勤系统的定位配置流程图，包含以下步骤：')
    add_normal_paragraph(doc, '1. 管理员登录PC管理端')
    add_normal_paragraph(doc, '2. 进入定位配置页面')
    add_normal_paragraph(doc, '3. 查询当前定位配置')
    add_normal_paragraph(doc, '4. 显示当前配置信息')
    add_normal_paragraph(doc, '5. 修改配置：纬度、经度、范围、地点名称')
    add_normal_paragraph(doc, '6. 点击"获取当前位置"（可选）')
    add_normal_paragraph(doc, '7. 获取浏览器定位')
    add_normal_paragraph(doc, '8. 自动填充坐标')
    add_normal_paragraph(doc, '9. 点击"保存"按钮')
    add_normal_paragraph(doc, '10. 发送POST请求到/api/location/config')
    add_normal_paragraph(doc, '11. 后端验证参数')
    add_normal_paragraph(doc, '12. 更新定位配置表')
    add_normal_paragraph(doc, '13. 返回保存成功结果')
    add_normal_paragraph(doc, '14. 提示"配置保存成功"')
    add_normal_paragraph(doc, '请使用mermaid语法绘制流程图。')
    
    add_heading(doc, '7. 安全与异常处理设计', level=2)
    add_normal_paragraph(doc, '【写作提示】说明权限控制、输入校验、错误提示、日志、隐私与敏感信息保护。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）权限控制', bold=True)
    add_normal_paragraph(doc, '管理员和普通员工使用不同的登录入口和权限')
    add_normal_paragraph(doc, '后端接口进行角色权限校验')
    
    add_normal_paragraph(doc, '（2）输入校验', bold=True)
    add_normal_paragraph(doc, '前端对输入进行格式校验')
    add_normal_paragraph(doc, '后端对所有参数进行校验，防止非法输入')
    add_normal_paragraph(doc, '人脸图片长度验证，防止过短数据')
    
    add_normal_paragraph(doc, '（3）错误提示', bold=True)
    add_normal_paragraph(doc, '所有接口返回统一格式的错误信息')
    add_normal_paragraph(doc, '错误信息清晰明确，便于用户理解')
    
    add_normal_paragraph(doc, '（4）日志记录', bold=True)
    add_normal_paragraph(doc, '后端记录关键操作日志')
    add_normal_paragraph(doc, '记录登录、签到、人脸注册等操作')
    add_normal_paragraph(doc, '记录错误和异常信息')
    
    add_normal_paragraph(doc, '（5）隐私与敏感信息保护', bold=True)
    add_normal_paragraph(doc, '用户密码采用MD5加密存储')
    add_normal_paragraph(doc, '人脸图片仅在传输时使用，后端存储提取的特征数据')
    add_normal_paragraph(doc, '禁止明文传输密码')
    
    add_heading(doc, '8. AI协助设计说明', level=2)
    add_normal_paragraph(doc, '【写作提示】说明AI提供了哪些方案建议，小组如何比较、采纳或修改。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）AI协助完成的工作', bold=True)
    add_normal_paragraph(doc, '① 项目整体架构设计：AI提供了前后端分离架构的建议')
    add_normal_paragraph(doc, '② 人脸识别算法实现：AI协助实现了基于PCA的人脸特征提取和比对算法')
    add_normal_paragraph(doc, '③ 接口设计：AI协助设计了RESTful API接口')
    add_normal_paragraph(doc, '④ 代码实现：AI协助编写了大部分业务代码')
    add_normal_paragraph(doc, '⑤ 文档生成：AI协助生成了项目文档和说明文档')
    
    add_normal_paragraph(doc, '（2）采纳的方案', bold=True)
    add_normal_paragraph(doc, '① 采纳了前后端分离架构')
    add_normal_paragraph(doc, '② 采纳了PCA人脸特征提取算法')
    add_normal_paragraph(doc, '③ 采纳了Retrofit作为Android网络框架')
    add_normal_paragraph(doc, '④ 采纳了Vue.js作为PC管理端框架')
    
    add_normal_paragraph(doc, '（3）修改的方案', bold=True)
    add_normal_paragraph(doc, '① 将签到接口从表单提交改为JSON提交，以支持大Base64图片数据')
    add_normal_paragraph(doc, '② 添加了反向地理编码功能，用于显示地点名称')
    add_normal_paragraph(doc, '③ 添加了动态修改服务器地址的功能')

def generate_test_report(doc):
    doc.add_heading('软件测试报告', level=1)
    
    add_heading(doc, '1. 测试目标与范围', level=2)
    add_normal_paragraph(doc, '【写作提示】说明本次测试覆盖哪些功能、模块、接口、模型或业务流程。')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '本次测试覆盖以下功能模块：')
    add_normal_paragraph(doc, '（1）用户登录功能')
    add_normal_paragraph(doc, '（2）人脸注册功能')
    add_normal_paragraph(doc, '（3）人脸签到功能')
    add_normal_paragraph(doc, '（4）GPS定位功能')
    add_normal_paragraph(doc, '（5）员工管理功能')
    add_normal_paragraph(doc, '（6）考勤记录查询功能')
    add_normal_paragraph(doc, '（7）定位配置功能')
    add_normal_paragraph(doc, '（8）密码修改功能')
    
    add_heading(doc, '2. 测试环境', level=2)
    add_normal_paragraph(doc, '【写作提示】说明操作系统、浏览器、数据库、JDK/Python/Node版本、设备型号、模型版本等。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）服务器环境', bold=True)
    add_normal_paragraph(doc, '操作系统：Windows 10')
    add_normal_paragraph(doc, 'JDK版本：1.8')
    add_normal_paragraph(doc, 'MySQL版本：5.7')
    add_normal_paragraph(doc, 'Spring Boot版本：2.7.0')
    
    add_normal_paragraph(doc, '（2）PC管理端环境', bold=True)
    add_normal_paragraph(doc, '操作系统：Windows 10')
    add_normal_paragraph(doc, '浏览器：Chrome 120+')
    add_normal_paragraph(doc, 'Node.js版本：16+')
    add_normal_paragraph(doc, 'Vue.js版本：2.6+')
    
    add_normal_paragraph(doc, '（3）Android客户端环境', bold=True)
    add_normal_paragraph(doc, '设备：小米11')
    add_normal_paragraph(doc, 'Android版本：12')
    add_normal_paragraph(doc, '开发工具：Android Studio')
    
    add_heading(doc, '3. 黑盒测试', level=2)
    add_normal_paragraph(doc, '【写作提示】从用户输入输出角度设计正常、边界、异常和业务流程测试。')
    add_normal_paragraph(doc, '【填写内容】')
    
    headers = ['用例编号', '测试类型', '测试目标', '输入/步骤', '预期结果', '实际结果', '是否通过']
    data = [
        ['TC-001', '黑盒', '正常登录', '工号：S001，密码：123456', '登录成功，进入主界面', '登录成功', '是'],
        ['TC-002', '黑盒', '错误密码登录', '工号：S001，密码：wrong', '提示登录失败', '提示登录失败', '是'],
        ['TC-003', '黑盒', '空工号登录', '工号：空，密码：123456', '提示请输入工号', '提示请输入工号', '是'],
        ['TC-004', '黑盒', '空密码登录', '工号：S001，密码：空', '提示请输入密码', '提示请输入密码', '是'],
        ['TC-005', '黑盒', '人脸注册', '拍摄清晰人脸照片', '注册成功', '注册成功', '是'],
        ['TC-006', '黑盒', '人脸签到', '已注册人脸，在范围内签到', '签到成功，显示地点和距离', '签到成功', '是'],
        ['TC-007', '黑盒', '未注册人脸签到', '未注册人脸，点击签到', '提示请先注册人脸', '提示请先注册人脸', '是'],
        ['TC-008', '黑盒', '超出范围签到', '已注册人脸，超出签到范围', '提示超出签到范围', '提示超出签到范围', '是'],
        ['TC-009', '黑盒', 'GPS定位', '点击获取定位', '显示当前坐标和地点名称', '显示当前坐标和地点名称', '是'],
        ['TC-010', '黑盒', '新增员工', '填写员工信息并保存', '员工信息保存成功', '员工信息保存成功', '是'],
        ['TC-011', '黑盒', '编辑员工', '修改员工信息并保存', '员工信息更新成功', '员工信息更新成功', '是'],
        ['TC-012', '黑盒', '删除员工', '选择员工并删除', '员工删除成功', '员工删除成功', '是'],
        ['TC-013', '黑盒', '查询考勤记录', '输入查询条件', '显示符合条件的考勤记录', '显示符合条件的考勤记录', '是'],
        ['TC-014', '黑盒', '配置定位范围', '设置签到点坐标和范围', '配置保存成功', '配置保存成功', '是'],
        ['TC-015', '黑盒', '修改密码', '输入原密码和新密码', '密码修改成功', '密码修改成功', '是'],
    ]
    add_table(doc, headers, data)
    
    add_heading(doc, '4. 白盒测试', level=2)
    add_normal_paragraph(doc, '【写作提示】针对关键函数、条件分支、循环逻辑、异常处理或核心算法设计测试。')
    add_normal_paragraph(doc, '【填写内容】')
    
    headers = ['用例编号', '测试类型', '测试目标', '输入/步骤', '预期结果', '实际结果', '是否通过']
    data = [
        ['TC-001', '白盒', '人脸特征提取', '传入有效Base64图片', '成功提取特征向量', '成功提取特征向量', '是'],
        ['TC-002', '白盒', '人脸特征提取', '传入无效Base64字符串', '返回空特征向量', '返回空特征向量', '是'],
        ['TC-003', '白盒', '人脸特征比对', '两张相同人脸图片', '相似度高于阈值', '相似度高于阈值', '是'],
        ['TC-004', '白盒', '人脸特征比对', '两张不同人脸图片', '相似度低于阈值', '相似度低于阈值', '是'],
        ['TC-005', '白盒', 'GPS距离计算', '两个GPS坐标', '正确计算距离', '正确计算距离', '是'],
        ['TC-006', '白盒', '位置检查', '在范围内的坐标', '返回在范围内', '返回在范围内', '是'],
        ['TC-007', '白盒', '位置检查', '超出范围的坐标', '返回超出范围', '返回超出范围', '是'],
        ['TC-008', '白盒', 'Base64解码', '带前缀的Base64字符串', '正确解码', '正确解码', '是'],
        ['TC-009', '白盒', 'Base64解码', '带空格的Base64字符串', '正确解码', '正确解码', '是'],
        ['TC-010', '白盒', '反向地理编码', '有效GPS坐标', '返回地点名称', '返回地点名称', '是'],
    ]
    add_table(doc, headers, data)
    
    add_heading(doc, '5. 集成测试', level=2)
    add_normal_paragraph(doc, '【写作提示】验证前端、后端、数据库、模型服务、文件上传、第三方接口之间能否协同工作。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）Android客户端与后端集成测试', bold=True)
    add_normal_paragraph(doc, '测试结果：Android客户端可正常调用后端API，数据传输正常')
    
    add_normal_paragraph(doc, '（2）PC管理端与后端集成测试', bold=True)
    add_normal_paragraph(doc, '测试结果：PC管理端可正常调用后端API，数据展示正常')
    
    add_normal_paragraph(doc, '（3）后端与数据库集成测试', bold=True)
    add_normal_paragraph(doc, '测试结果：后端可正常连接数据库，数据增删改查正常')
    
    add_normal_paragraph(doc, '（4）反向地理编码API集成测试', bold=True)
    add_normal_paragraph(doc, '测试结果：可正常调用Nominatim API获取地点名称')
    
    add_heading(doc, '6. 缺陷记录与修改', level=2)
    add_normal_paragraph(doc, '【写作提示】记录缺陷编号、现象、原因、严重程度、修改办法和复测结果。')
    add_normal_paragraph(doc, '【填写内容】')
    
    headers = ['缺陷编号', '发现阶段', '问题描述', '原因分析', '修改办法', '复测结果']
    data = [
        ['BUG-001', '集成测试', '人脸签到失败，显示网络连接失败', '签到接口返回类型不匹配，Android端定义为String，后端返回Map', '将Android端返回类型改为Object', '已修复，签到成功'],
        ['BUG-002', '单元测试', '人脸图片无法解码', 'Base64字符串包含前缀和空格', '添加Base64前缀和空格处理逻辑', '已修复，解码成功'],
        ['BUG-003', '集成测试', '位置显示为坐标而非地点名称', '后端未实现反向地理编码', '集成Nominatim API实现反向地理编码', '已修复，显示地点名称'],
        ['BUG-004', '集成测试', '晋中市被识别为太原市', '坐标判断范围过大', '缩小坐标判断范围至0.3度', '已修复，正确识别晋中市'],
        ['BUG-005', '集成测试', '设置按钮被遮挡', '布局层级问题', '使用FrameLayout将按钮置于最顶层', '已修复，按钮正常显示'],
    ]
    add_table(doc, headers, data)
    
    add_heading(doc, '7. 测试结论', level=2)
    add_normal_paragraph(doc, '【写作提示】说明系统是否达到需求规格说明书中的验收标准，列出遗留问题和改进建议。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '测试结论：系统基本达到需求规格说明书中的验收标准，主要功能均可正常使用。', bold=True)
    
    add_normal_paragraph(doc, '遗留问题：', bold=True)
    add_normal_paragraph(doc, '（1）人脸识别准确率受光线影响较大，建议在弱光环境下增加补光功能')
    add_normal_paragraph(doc, '（2）GPS定位在室内环境下精度较低，建议结合WiFi定位提高精度')
    
    add_normal_paragraph(doc, '改进建议：', bold=True)
    add_normal_paragraph(doc, '（1）增加人脸识别模型训练功能，提高识别准确率')
    add_normal_paragraph(doc, '（2）优化界面设计，提高用户体验')
    add_normal_paragraph(doc, '（3）增加数据统计和报表功能')

def generate_user_manual(doc):
    doc.add_heading('用户使用说明书', level=1)
    
    add_heading(doc, '1. 系统简介', level=2)
    add_normal_paragraph(doc, '【写作提示】用简短语言说明系统用途、目标用户和主要功能。')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '人脸考勤签到系统是一套基于人脸识别和GPS定位的智能考勤管理系统，旨在提高企业考勤管理效率，防止代打卡现象。系统分为Android客户端和PC管理端两部分，员工使用Android App进行人脸注册和日常签到，管理员使用PC管理端进行员工管理、考勤查看和配置管理。')
    
    add_heading(doc, '2. 运行环境与安装步骤', level=2)
    add_normal_paragraph(doc, '【写作提示】说明依赖安装、数据库配置、启动命令、模型文件位置或移动端安装方式。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）后端服务安装', bold=True)
    add_normal_paragraph(doc, '① 安装JDK 1.8或更高版本')
    add_normal_paragraph(doc, '② 安装MySQL 5.7或更高版本')
    add_normal_paragraph(doc, '③ 创建数据库：CREATE DATABASE attendance_db;')
    add_normal_paragraph(doc, '④ 修改application.yml中的数据库连接配置')
    add_normal_paragraph(doc, '⑤ 运行start.bat启动后端服务')
    
    add_normal_paragraph(doc, '（2）PC管理端安装', bold=True)
    add_normal_paragraph(doc, '① 安装Node.js 16或更高版本')
    add_normal_paragraph(doc, '② 进入03-PC-Admin-Vue目录')
    add_normal_paragraph(doc, '③ 执行npm install安装依赖')
    add_normal_paragraph(doc, '④ 修改src/api/index.js中的baseURL为服务器地址')
    add_normal_paragraph(doc, '⑤ 执行npm run build构建项目')
    add_normal_paragraph(doc, '⑥ 访问http://localhost:8080即可使用')
    
    add_normal_paragraph(doc, '（3）Android客户端安装', bold=True)
    add_normal_paragraph(doc, '① 将生成的APK文件复制到Android设备')
    add_normal_paragraph(doc, '② 在设备上安装APK文件')
    add_normal_paragraph(doc, '③ 首次打开时在登录页面右上角设置服务器地址')
    add_normal_paragraph(doc, '④ 输入工号和密码登录使用')
    
    add_heading(doc, '3. 测试账号与角色', level=2)
    add_normal_paragraph(doc, '【写作提示】提供演示所需账号、密码和角色，注意不得使用真实隐私账号。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）管理员账号', bold=True)
    add_normal_paragraph(doc, '账号：admin')
    add_normal_paragraph(doc, '密码：admin123')
    
    add_normal_paragraph(doc, '（2）测试员工账号', bold=True)
    add_normal_paragraph(doc, '工号：S001')
    add_normal_paragraph(doc, '密码：123456')
    
    add_normal_paragraph(doc, '工号：S002')
    add_normal_paragraph(doc, '密码：123456')
    
    add_normal_paragraph(doc, '工号：S003')
    add_normal_paragraph(doc, '密码：123456')
    
    add_heading(doc, '4. 功能操作说明', level=2)
    add_normal_paragraph(doc, '【写作提示】按用户任务说明如何完成登录、查询、新增、上传、检测、统计、导出等操作，使用文字、截图对对应的模块进行描述。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '4.1 管理员模块', bold=True)
    
    add_normal_paragraph(doc, '4.1.1 用户管理', bold=True)
    add_normal_paragraph(doc, '【写作提示】文字描述')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '管理员登录系统后，点击左侧菜单"员工管理"进入员工列表页面。在此页面可以查看所有员工信息，包括工号、姓名、部门、职位、人脸照片等。')
    add_normal_paragraph(doc, '新增员工：点击"新增员工"按钮，填写员工信息表单，包括工号、姓名、部门、职位、电话、邮箱和密码，点击"保存"按钮完成新增。')
    add_normal_paragraph(doc, '编辑员工：点击员工列表中的"编辑"按钮，修改员工信息后点击"保存"按钮。')
    add_normal_paragraph(doc, '删除员工：点击员工列表中的"删除"按钮，确认后删除该员工。')
    add_normal_paragraph(doc, '【生成功能截图提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个员工管理页面的界面设计图，包含以下元素：')
    add_normal_paragraph(doc, '1. 页面标题：员工管理')
    add_normal_paragraph(doc, '2. 搜索框：按工号、姓名、部门搜索')
    add_normal_paragraph(doc, '3. 新增员工按钮')
    add_normal_paragraph(doc, '4. 员工列表表格：工号、姓名、部门、职位、人脸照片、状态、操作')
    add_normal_paragraph(doc, '请使用简洁现代的UI设计风格。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '4.1.2 考勤记录管理', bold=True)
    add_normal_paragraph(doc, '【写作提示】文字描述')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '管理员登录系统后，点击左侧菜单"考勤记录"进入考勤记录页面。在此页面可以查看所有考勤记录，包括员工信息、签到时间、位置信息、签到类型等。')
    add_normal_paragraph(doc, '查询记录：可以按员工ID、员工编号、定位状态、签到类型、日期等条件进行筛选查询。')
    add_normal_paragraph(doc, '批量删除：选择多条记录后点击"批量删除"按钮，确认后删除选中的记录。')
    add_normal_paragraph(doc, '【生成功能截图提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个考勤记录页面的界面设计图，包含以下元素：')
    add_normal_paragraph(doc, '1. 页面标题：考勤记录')
    add_normal_paragraph(doc, '2. 筛选条件：员工编号、定位状态、签到类型、日期')
    add_normal_paragraph(doc, '3. 批量删除按钮')
    add_normal_paragraph(doc, '4. 考勤记录表格：员工姓名、工号、签到时间、位置名称、坐标、距离、签到类型、定位状态')
    add_normal_paragraph(doc, '请使用简洁现代的UI设计风格。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '4.1.3 定位配置', bold=True)
    add_normal_paragraph(doc, '【写作提示】文字描述')
    add_normal_paragraph(doc, '【填写内容】')
    add_normal_paragraph(doc, '管理员登录系统后，点击左侧菜单"定位配置"进入定位配置页面。在此页面可以设置签到点的坐标和允许的签到范围。')
    add_normal_paragraph(doc, '配置签到点：输入签到点的纬度、经度、允许范围（米）和地点名称，点击"保存"按钮完成配置。')
    add_normal_paragraph(doc, '获取当前位置：点击"获取当前位置"按钮，系统自动获取当前坐标填充到表单中。')
    add_normal_paragraph(doc, '【生成功能截图提示词】', bold=True)
    add_normal_paragraph(doc, '请帮我生成一个定位配置页面的界面设计图，包含以下元素：')
    add_normal_paragraph(doc, '1. 页面标题：定位配置')
    add_normal_paragraph(doc, '2. 表单字段：纬度、经度、允许范围（米）、地点名称')
    add_normal_paragraph(doc, '3. 获取当前位置按钮')
    add_normal_paragraph(doc, '4. 保存按钮')
    add_normal_paragraph(doc, '5. 当前配置信息展示')
    add_normal_paragraph(doc, '请使用简洁现代的UI设计风格。')
    
    add_normal_paragraph(doc, '')
    add_normal_paragraph(doc, '4.2 员工模块', bold=True)
    
    add_normal_paragraph(doc, '4.2.1 登录', bold=True)
    add_normal_paragraph(doc, '打开App，在登录页面输入工号和密码，点击"登录"按钮。如果服务器地址需要修改，点击右上角设置按钮进行修改。')
    
    add_normal_paragraph(doc, '4.2.2 人脸注册', bold=True)
    add_normal_paragraph(doc, '登录后在签到页面点击"注册人脸"按钮，系统调用摄像头，拍摄清晰的人脸照片，点击确认完成注册。')
    
    add_normal_paragraph(doc, '4.2.3 人脸签到', bold=True)
    add_normal_paragraph(doc, '登录后在签到页面点击"立即签到"按钮，系统调用摄像头进行人脸识别，识别成功后自动获取GPS位置，检查是否在允许范围内，在范围内则签到成功。')
    
    add_normal_paragraph(doc, '4.2.4 查看考勤记录', bold=True)
    add_normal_paragraph(doc, '点击底部导航"记录"标签，查看个人考勤记录列表。')
    
    add_normal_paragraph(doc, '4.2.5 个人信息', bold=True)
    add_normal_paragraph(doc, '点击底部导航"我的"标签，查看个人信息和设置选项。')
    
    add_normal_paragraph(doc, '4.2.6 修改密码', bold=True)
    add_normal_paragraph(doc, '在"我的"页面点击"修改密码"，输入原密码和新密码，点击"保存"按钮完成修改。')
    
    add_heading(doc, '5. 常见问题与解决办法', level=2)
    add_normal_paragraph(doc, '【写作提示】说明端口占用、依赖缺失、数据库连接失败、模型路径错误等常见问题。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）端口占用问题', bold=True)
    add_normal_paragraph(doc, '问题：启动服务时提示端口8080被占用')
    add_normal_paragraph(doc, '解决：关闭占用8080端口的程序，或修改application.yml中的server.port配置')
    
    add_normal_paragraph(doc, '（2）数据库连接失败', bold=True)
    add_normal_paragraph(doc, '问题：启动服务时无法连接数据库')
    add_normal_paragraph(doc, '解决：检查MySQL服务是否启动，检查application.yml中的数据库连接配置是否正确')
    
    add_normal_paragraph(doc, '（3）人脸识别失败', bold=True)
    add_normal_paragraph(doc, '问题：人脸识别一直失败')
    add_normal_paragraph(doc, '解决：确保光线充足，正面面对摄像头，确保已注册人脸')
    
    add_normal_paragraph(doc, '（4）无法获取GPS定位', bold=True)
    add_normal_paragraph(doc, '问题：无法获取当前位置')
    add_normal_paragraph(doc, '解决：确保手机已开启GPS定位，确保App已获取定位权限')
    
    add_normal_paragraph(doc, '（5）网络连接失败', bold=True)
    add_normal_paragraph(doc, '问题：App无法连接服务器')
    add_normal_paragraph(doc, '解决：检查服务器是否启动，检查网络是否正常，检查服务器地址是否正确')
    
    add_heading(doc, '6. 注意事项', level=2)
    add_normal_paragraph(doc, '【写作提示】说明数据安全、浏览器兼容、模型限制、演示数据范围等。')
    add_normal_paragraph(doc, '【填写内容】')
    
    add_normal_paragraph(doc, '（1）数据安全', bold=True)
    add_normal_paragraph(doc, '请妥善保管登录账号和密码，不要泄露给他人')
    add_normal_paragraph(doc, '人脸照片仅用于身份验证，不会用于其他用途')
    
    add_normal_paragraph(doc, '（2）浏览器兼容', bold=True)
    add_normal_paragraph(doc, 'PC管理端建议使用Chrome浏览器，以获得最佳体验')
    
    add_normal_paragraph(doc, '（3）模型限制', bold=True)
    add_normal_paragraph(doc, '人脸识别准确率受光线、角度等因素影响')
    add_normal_paragraph(doc, '建议在光线充足的环境下进行人脸识别')
    
    add_normal_paragraph(doc, '（4）演示数据', bold=True)
    add_normal_paragraph(doc, '系统中的测试账号仅用于演示，请勿用于生产环境')

def generate_ai_usage(doc):
    doc.add_heading('AI工具使用说明', level=1)
    
    add_heading(doc, '1. 需求分析交互过程', level=2)
    
    add_normal_paragraph(doc, '1.1 第1次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-01')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：项目需求分析')
    add_normal_paragraph(doc, 'Prompt摘要：分析人脸考勤签到系统的需求，包括功能需求和非功能需求')
    add_normal_paragraph(doc, '输出摘要：AI提供了详细的需求分析，包括用户角色、业务场景、功能需求列表等')
    add_normal_paragraph(doc, '人工审核的内容：确认需求分析符合项目目标')
    
    add_normal_paragraph(doc, '1.2 第2次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-02')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：业务流程设计')
    add_normal_paragraph(doc, 'Prompt摘要：设计人脸考勤系统的业务流程，包括人脸注册和签到流程')
    add_normal_paragraph(doc, '输出摘要：AI提供了详细的业务流程描述和流程图')
    add_normal_paragraph(doc, '人工审核的内容：确认业务流程符合实际需求')
    
    add_normal_paragraph(doc, '1.3 第3次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-03')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：数据需求分析')
    add_normal_paragraph(doc, 'Prompt摘要：分析人脸考勤系统的数据需求，包括数据库表设计')
    add_normal_paragraph(doc, '输出摘要：AI提供了数据库表设计方案，包括字段定义和约束')
    add_normal_paragraph(doc, '人工审核的内容：确认数据库设计合理')
    
    add_heading(doc, '2. 软件设计交互过程', level=2)
    
    add_normal_paragraph(doc, '2.1 第1次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-04')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：系统架构设计')
    add_normal_paragraph(doc, 'Prompt摘要：设计人脸考勤系统的整体架构，包括前后端分离方案')
    add_normal_paragraph(doc, '输出摘要：AI提供了前后端分离架构设计方案')
    add_normal_paragraph(doc, '人工审核的内容：确认架构设计合理')
    
    add_normal_paragraph(doc, '2.2 第2次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-04')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：人脸识别算法实现')
    add_normal_paragraph(doc, 'Prompt摘要：实现基于PCA的人脸特征提取和比对算法')
    add_normal_paragraph(doc, '输出摘要：AI提供了PCA人脸特征提取和比对的代码实现')
    add_normal_paragraph(doc, '人工审核的内容：确认算法实现正确')
    
    add_normal_paragraph(doc, '2.3 第3次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-05')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：接口设计')
    add_normal_paragraph(doc, 'Prompt摘要：设计人脸考勤系统的RESTful API接口')
    add_normal_paragraph(doc, '输出摘要：AI提供了详细的API接口设计')
    add_normal_paragraph(doc, '人工审核的内容：确认接口设计符合需求')
    
    add_normal_paragraph(doc, '2.4 第4次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-05')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：前端页面设计')
    add_normal_paragraph(doc, 'Prompt摘要：设计PC管理端和Android客户端的页面')
    add_normal_paragraph(doc, '输出摘要：AI提供了详细的页面设计和布局方案')
    add_normal_paragraph(doc, '人工审核的内容：确认页面设计美观合理')
    
    add_heading(doc, '3. 软件测试过程', level=2)
    
    add_normal_paragraph(doc, '3.1 第1次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-06')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：测试用例设计')
    add_normal_paragraph(doc, 'Prompt摘要：设计人脸考勤系统的测试用例，包括黑盒测试和白盒测试')
    add_normal_paragraph(doc, '输出摘要：AI提供了详细的测试用例设计')
    add_normal_paragraph(doc, '人工审核的内容：确认测试用例覆盖全面')
    
    add_normal_paragraph(doc, '3.2 第2次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-06')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：缺陷修复')
    add_normal_paragraph(doc, 'Prompt摘要：修复人脸识别失败、位置显示错误等问题')
    add_normal_paragraph(doc, '输出摘要：AI提供了缺陷修复方案和代码')
    add_normal_paragraph(doc, '人工审核的内容：确认缺陷修复有效')
    
    add_heading(doc, '4. 系统搭建过程', level=2)
    
    add_normal_paragraph(doc, '4.1 第1次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-01')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：项目初始化')
    add_normal_paragraph(doc, 'Prompt摘要：初始化人脸考勤系统项目，包括后端和前端')
    add_normal_paragraph(doc, '输出摘要：AI帮助创建了项目结构和基础配置')
    add_normal_paragraph(doc, '人工审核的内容：确认项目结构合理')
    
    add_normal_paragraph(doc, '4.2 第2次交互', bold=True)
    add_normal_paragraph(doc, '日期：2026-07-06')
    add_normal_paragraph(doc, '工具：Trae AI')
    add_normal_paragraph(doc, '用途：文档生成')
    add_normal_paragraph(doc, 'Prompt摘要：生成项目文档、运行文档、代码说明和IP修改说明')
    add_normal_paragraph(doc, '输出摘要：AI帮助生成了完整的项目文档')
    add_normal_paragraph(doc, '人工审核的内容：确认文档内容完整准确')

def main():
    base_dir = r'C:\Users\35416\Desktop\FaceAttendanceGPSTotal'
    
    doc1 = Document()
    generate_requirement_spec(doc1)
    doc1.save(os.path.join(base_dir, '需求规格说明书.docx'))
    print('需求规格说明书.docx 已生成')
    
    doc2 = Document()
    generate_design_spec(doc2)
    doc2.save(os.path.join(base_dir, '软件设计说明书.docx'))
    print('软件设计说明书.docx 已生成')
    
    doc3 = Document()
    generate_test_report(doc3)
    doc3.save(os.path.join(base_dir, '软件测试报告.docx'))
    print('软件测试报告.docx 已生成')
    
    doc4 = Document()
    generate_user_manual(doc4)
    doc4.save(os.path.join(base_dir, '用户使用说明书.docx'))
    print('用户使用说明书.docx 已生成')
    
    doc5 = Document()
    generate_ai_usage(doc5)
    doc5.save(os.path.join(base_dir, 'AI工具使用说明.docx'))
    print('AI工具使用说明.docx 已生成')
    
    print('所有文档生成完成！')

if __name__ == '__main__':
    main()