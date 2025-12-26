-- 统一用户表（包含学生、管理员、总负责人）
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键（用户唯一标识）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（学号或管理员账号）',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色（student-学生，admin-管理员，super_admin-总负责人）',
    student_id VARCHAR(20) COMMENT '学号（学生角色必填）',
    college VARCHAR(100) COMMENT '学院（学生角色必填）',
    major VARCHAR(100) COMMENT '专业（学生角色必填）',
    grade VARCHAR(20) COMMENT '年级（学生角色必填）',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    lab_id BIGINT COMMENT '关联实验室ID（管理员角色必填）',
    can_edit TINYINT NOT NULL DEFAULT 1 COMMENT '是否可编辑账号信息（0-不可编辑，1-可编辑）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态（0-禁用，1-正常）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    FOREIGN KEY (lab_id) REFERENCES t_lab(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一用户表';

-- 实验室表
CREATE TABLE IF NOT EXISTS t_lab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键（实验室唯一标识）',
    lab_name VARCHAR(100) NOT NULL UNIQUE COMMENT '实验室名称（如"AI竞赛实验室"）',
    lab_desc TEXT NOT NULL COMMENT '实验室介绍（研究方向、竞赛成果等）',
    require_skill VARCHAR(255) NOT NULL COMMENT '所需技能（如"Python、算法基础"）',
    recruit_num INT NOT NULL DEFAULT 0 COMMENT '招生人数',
    current_num INT NOT NULL DEFAULT 0 COMMENT '已投递人数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '招新状态（0-未开始，1-进行中，2-已结束）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（预设）',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室表';

-- 投递记录表
CREATE TABLE IF NOT EXISTS t_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键（投递记录唯一标识）',
    user_id BIGINT NOT NULL COMMENT '关联学生ID',
    lab_id BIGINT NOT NULL COMMENT '关联实验室ID',
    skill_tags VARCHAR(255) NOT NULL COMMENT '学生特长标签（如"Java、机器学习"）',
    study_progress TEXT NOT NULL COMMENT '近期学习进度（如"已学完Java基础，正在学Spring Boot"）',
    attachment_url VARCHAR(255) COMMENT '附件地址（简历、作品链接等）',
    delivery_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核，1-通过，2-拒绝）',
    audit_remark VARCHAR(255) COMMENT '审核备注（管理员填写）',
    audit_time DATETIME COMMENT '审核时间',
    is_admitted TINYINT NOT NULL DEFAULT 0 COMMENT '是否录取（0-未录取，1-已录取）',
    admit_time DATETIME COMMENT '录取时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    FOREIGN KEY (user_id) REFERENCES t_user(id),
    FOREIGN KEY (lab_id) REFERENCES t_lab(id),
    UNIQUE KEY uk_user_lab (user_id, lab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录表';

-- 插入示例数据
-- 插入实验室数据
INSERT INTO t_lab (lab_name, lab_desc, require_skill, recruit_num, status) VALUES
('AI竞赛实验室', '专注于人工智能算法研究，参加国内外各类AI竞赛，获得多项国家级奖项', 'Python、机器学习、深度学习、算法基础', 20, 1),
('软件开发实验室', '专注于软件开发技术，包括Web开发、移动应用开发等', 'Java、Spring Boot、Vue.js、数据库', 15, 1),
('网络安全实验室', '专注于网络安全技术研究，包括渗透测试、安全防护等', '计算机网络、操作系统、Linux、安全基础', 10, 1);

-- 插入总负责人账号（固定，不可修改）
INSERT INTO t_user (username, password, real_name, role, phone, email, can_edit) VALUES
('super_admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', '系统总负责人', 'super_admin', '13800138000', 'super_admin@lab.com', 0);

-- 插入管理员数据（密码为123456的BCrypt加密值）
INSERT INTO t_user (username, password, real_name, role, lab_id, phone, email) VALUES
('ai_admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', '张教授', 'admin', 1, '13800138001', 'ai_admin@lab.com'),
('soft_admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', '李教授', 'admin', 2, '13800138002', 'soft_admin@lab.com'),
('security_admin', '$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy', '王教授', 'admin', 3, '13800138003', 'security_admin@lab.com');