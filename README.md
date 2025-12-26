# 大学计算机竞赛实验室招新系统

一个基于 Spring Boot + Vue 3 的实验室招新管理系统，支持学生投递、实验室管理、审核流程等完整功能。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [系统架构](#系统架构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库配置](#数据库配置)
- [部署指南](#部署指南)
- [API 文档](#api-文档)
- [常见问题](#常见问题)
- [更新日志](#更新日志)

## 🎯 项目简介

本系统为大学计算机竞赛实验室提供完整的招新管理解决方案，支持多实验室管理、学生在线投递、管理员审核、录取通知等功能。系统采用前后端分离架构，提供良好的用户体验和管理效率。

## 🛠 技术栈

### 后端技术
- **Spring Boot 2.7.14** - Java Web 框架
- **Spring Security** - 安全框架
- **MyBatis Plus 3.5.3.1** - ORM 框架
- **MySQL 5.7.27** - 关系型数据库
- **JWT 0.11.5** - Token 认证
- **Lombok** - 简化 Java 代码
- **FastJSON 2.0.32** - JSON 处理

### 前端技术
- **Vue 3.3.0** - 渐进式 JavaScript 框架
- **Vite 4.4.0** - 前端构建工具
- **Element Plus 2.4.0** - UI 组件库
- **Vue Router 4.2.0** - 路由管理
- **Pinia 3.0.4** - 状态管理
- **Axios 1.6.0** - HTTP 客户端

## ✨ 功能特性

### 学生端功能
- 🔐 用户注册与登录
- 📝 完善个人信息
- 🔍 浏览实验室信息
- 📤 在线投递申请
- 📎 上传简历附件
- 📊 查看投递状态
- 📬 查看录取结果

### 管理员端功能
- 👥 管理学生投递
- ✅ 审核学生申请
- 📝 添加审核备注
- 📈 查看统计数据
- 🔐 管理实验室信息

### 总负责人端功能
- 👑 管理所有实验室
- 👥 管理管理员账号
- 📊 查看全局统计
- 🔧 系统配置管理

## 🏗 系统架构

```
lab-recruitment/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/              # API 接口
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── layouts/          # 布局组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # 状态管理
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面组件
│   ├── package.json
│   └── vite.config.js
├── src/                      # 后端项目
│   ├── main/
│   │   ├── java/
│   │   │   └── com/lab/recruitment/
│   │   │       ├── config/   # 配置类
│   │   │       ├── controller/ # 控制器
│   │   │       ├── dto/      # 数据传输对象
│   │   │       ├── entity/   # 实体类
│   │   │       ├── exception/ # 异常处理
│   │   │       ├── mapper/   # MyBatis Mapper
│   │   │       ├── service/  # 业务逻辑
│   │   │       ├── utils/    # 工具类
│   │   │       └── vo/       # 视图对象
│   │   └── resources/
│   │       ├── mapper/      # MyBatis XML
│   │       ├── application.yml
│   │       └── init.sql
│   └── test/
├── pom.xml
└── README.md
```

## 💻 环境要求

### 开发环境
- **JDK 11+**
- **Node.js 16+**
- **MySQL 5.7.27+**
- **Maven 3.6+**

### 生产环境
- **JDK 11+**
- **MySQL 5.7.27+**
- **Nginx**（可选，用于反向代理）

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd lab-recruitment
```

### 2. 数据库配置

#### 2.1 创建数据库

```sql
CREATE DATABASE lab_recruitment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 导入初始化脚本

```bash
mysql -u root -p lab_recruitment < src/main/resources/init.sql
```

#### 2.3 修改数据库配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lab_recruitment?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: your_password
```

### 3. 后端启动

```bash
# 进入后端目录
cd lab-recruitment

# 使用 Maven 编译
mvn clean package

# 运行应用
java -jar target/lab-recruitment-1.0.0.jar

# 或者直接使用 Maven 运行
mvn spring-boot:run
```

后端服务将在 `http://localhost:8081` 启动

### 4. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

前端服务将在 `http://localhost:3000` 启动

## 📊 数据库配置

### 数据库表结构

#### 用户表 (t_user)
存储所有用户信息，包括学生、管理员和总负责人。

#### 实验室表 (t_lab)
存储实验室基本信息和招新状态。

#### 投递记录表 (t_delivery)
存储学生的投递记录和审核状态。

### 默认账号

系统初始化时会创建以下默认账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| super_admin | 123456 | 总负责人 | 系统最高权限账号 |
| ai_admin | 123456 | 管理员 | AI竞赛实验室管理员 |
| soft_admin | 123456 | 管理员 | 软件开发实验室管理员 |
| security_admin | 123456 | 管理员 | 网络安全实验室管理员 |

### 修改文件上传路径

编辑 `src/main/resources/application.yml`：

```yaml
file:
  upload-path: /path/to/your/uploads/
```

确保该路径存在且有写入权限。

## 🌐 部署指南

### Docker 部署（推荐）

#### 1. 安装 Docker

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install docker.io -y

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker
```

#### 2. 拉取 MySQL 镜像

```bash
sudo docker pull mysql:5.7.27
```

#### 3. 运行 MySQL 容器

```bash
sudo docker run --name mysql57 \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -v /var/lib/mysql:/var/lib/mysql \
  -v /etc/mysql/conf.d:/etc/mysql/conf.d \
  --restart=always \
  -d mysql:5.7.27
```

#### 4. 验证 MySQL

```bash
# 查看容器状态
sudo docker ps

# 验证版本
sudo docker exec -it mysql57 mysql --version

# 创建数据库
sudo docker exec -it mysql57 mysql -u root -p123456 -e "CREATE DATABASE lab_recruitment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
sudo docker exec -i mysql57 mysql -u root -p123456 lab_recruitment < src/main/resources/init.sql
```

#### 5. 构建并运行后端

```bash
# 构建后端 JAR
mvn clean package

# 运行后端
java -jar target/lab-recruitment-1.0.0.jar
```

#### 6. 构建并部署前端

```bash
cd frontend
npm run build

# 将 dist 目录部署到 Nginx
sudo cp -r dist/* /var/www/html/
```

### 传统部署

#### 后端部署

```bash
# 构建 JAR 包
mvn clean package

# 上传到服务器
scp target/lab-recruitment-1.0.0.jar user@server:/path/to/deploy/

# 在服务器上运行
nohup java -jar lab-recruitment-1.0.0.jar > app.log 2>&1 &
```

#### 前端部署

```bash
# 构建生产版本
cd frontend
npm run build

# 上传到服务器
scp -r dist/* user@server:/var/www/html/
```

## 📚 API 文档

### 认证接口

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "student001",
  "password": "123456",
  "realName": "张三",
  "studentId": "20210001",
  "college": "计算机学院",
  "major": "软件工程",
  "grade": "2021级",
  "phone": "13800138000",
  "email": "student@example.com"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "student001",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "student001",
    "role": "student"
  }
}
```

### 实验室接口

#### 获取实验室列表
```
GET /api/lab/list
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "labName": "AI竞赛实验室",
      "labDesc": "专注于人工智能算法研究...",
      "requireSkill": "Python、机器学习、深度学习",
      "recruitNum": 20,
      "currentNum": 5,
      "status": 1
    }
  ]
}
```

### 投递接口

#### 提交投递
```
POST /api/delivery/submit
Authorization: Bearer {token}
Content-Type: application/json

{
  "labId": 1,
  "skillTags": "Java、Python、机器学习",
  "studyProgress": "已学完Java基础，正在学习Spring Boot",
  "attachmentUrl": "/uploads/resume.pdf"
}
```

#### 获取投递列表（管理员）
```
GET /api/delivery/list?labId=1
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "labId": 1,
      "realName": "张三",
      "studentId": "20210001",
      "skillTags": "Java、Python、机器学习",
      "studyProgress": "已学完Java基础...",
      "auditStatus": 0,
      "deliveryTime": "2024-01-01 10:00:00"
    }
  ]
}
```

#### 审核投递
```
POST /api/delivery/audit
Authorization: Bearer {token}
Content-Type: application/json

{
  "deliveryId": 1,
  "auditStatus": 1,
  "auditRemark": "基础扎实，通过审核"
}
```

## ❓ 常见问题

### 1. 数据库连接失败

**问题**：启动后端时报错 "Access denied for user 'root'@'localhost'"

**解决方案**：
- 检查 MySQL 服务是否启动
- 确认用户名和密码是否正确
- 确认数据库 `lab_recruitment` 是否已创建
- 检查防火墙设置

### 2. 前端无法连接后端

**问题**：前端报错 "Network Error"

**解决方案**：
- 确认后端服务是否正常运行
- 检查后端端口（默认 8081）是否被占用
- 检查前端 API 配置（`frontend/src/utils/request.js`）
- 检查 CORS 配置

### 3. 文件上传失败

**问题**：上传文件时报错

**解决方案**：
- 检查文件上传路径是否存在
- 确认路径是否有写入权限
- 检查文件大小限制（默认 10MB）
- 查看日志文件 `logs/application.log`

### 4. JWT Token 过期

**问题**：登录后一段时间后提示未登录

**解决方案**：
- Token 默认有效期为 24 小时
- 可在 `application.yml` 中修改 `jwt.expiration` 配置
- 前端应实现 Token 自动刷新机制

### 5. Docker 安装失败

**问题**：无法连接到 Docker 官方源

**解决方案**：
```bash
# 使用阿里云镜像
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh --mirror Aliyun
```

## 📝 更新日志

### v1.0.0 (2024-01-01)
- ✨ 初始版本发布
- ✅ 完成用户认证系统
- ✅ 实现实验室管理功能
- ✅ 实现学生投递功能
- ✅ 实现审核流程
- ✅ 完成前后端分离架构

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👥 作者

- 项目维护者：[Your Name]
- 联系邮箱：[your.email@example.com]

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

**注意**：本系统仅供学习和研究使用，请勿用于商业用途。
