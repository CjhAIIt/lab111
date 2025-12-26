# 云服务器前端部署指南

## 🌐 前端访问原理

### 整体架构

```
┌─────────────┐         HTTP/HTTPS          ┌──────────────────┐
│   用户浏览器 │ ──────────────────────────> │   云服务器 Nginx │
│  (客户端)    │  访问公网IP或域名           │   (端口 80/443)  │
└─────────────┘                            └────────┬─────────┘
                                                    │
                                                    │ 1. 返回静态文件
                                                    │    (HTML/CSS/JS)
                                                    ↓
                                           ┌──────────────────┐
                                           │  /var/www/html/  │
                                           │  前端静态文件     │
                                           └──────────────────┘
                                                    │
                                                    │ 2. 浏览器加载后
                                                    │    发起 API 请求
                                                    ↓
                                           ┌──────────────────┐
                                           │  Nginx 代理      │
                                           │  /api/*          │
                                           └────────┬─────────┘
                                                    │
                                                    │ 3. 转发到后端
                                                    ↓
                                           ┌──────────────────┐
                                           │  Spring Boot     │
                                           │  (端口 8081)     │
                                           │  localhost:8081  │
                                           └──────────────────┘
```

## 📋 部署步骤

### 步骤 1: 本地打包前端

在本地开发机器上执行：

```bash
# 进入前端目录
cd frontend

# 安装依赖（如果还没有安装）
npm install

# 构建生产版本
npm run build
```

打包完成后，会在 `frontend/dist` 目录生成以下文件：

```
dist/
├── index.html              # 主页面
├── assets/
│   ├── index-xxxxx.js      # 打包后的 JavaScript
│   ├── index-xxxxx.css     # 打包后的 CSS
│   └── vendor-xxxxx.js     # 第三方库
└── test-labs.html          # 测试页面
```

### 步骤 2: 上传到云服务器

使用以下任一方式上传文件：

#### 方式 1: 使用 SCP（推荐）

```bash
# 在本地执行
scp -r frontend/dist/* root@your-server-ip:/var/www/html/
```

#### 方式 2: 使用 FTP/SFTP 工具

- FileZilla
- WinSCP
- Cyberduck

#### 方式 3: 使用 Git

```bash
# 在云服务器上执行
cd /var/www/html
git clone <your-repo>
cp -r lab-recruitment/frontend/dist/* .
```

### 步骤 3: 配置 Nginx

#### 3.1 安装 Nginx

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install nginx -y

# CentOS/RHEL
sudo yum install nginx -y
```

#### 3.2 创建 Nginx 配置文件

```bash
sudo nano /etc/nginx/sites-available/lab-recruitment
```

添加以下配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或IP

    root /var/www/html;
    index index.html;

    # 前端静态文件
    location / {
        try_files $uri $uri/ /index.html;
        
        # 缓存静态资源
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }

    # 代理后端 API 请求
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 启用 Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/javascript application/json;
}
```

#### 3.3 启用配置

```bash
# 创建符号链接
sudo ln -s /etc/nginx/sites-available/lab-recruitment /etc/nginx/sites-enabled/

# 删除默认配置（可选）
sudo rm /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 设置开机自启
sudo systemctl enable nginx
```

### 步骤 4: 配置防火墙

```bash
# Ubuntu/Debian (使用 ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload

# CentOS/RHEL (使用 firewalld)
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# 阿里云/腾讯云等云服务商
# 需要在控制台安全组中开放 80 和 443 端口
```

### 步骤 5: 验证部署

```bash
# 检查 Nginx 状态
sudo systemctl status nginx

# 查看访问日志
sudo tail -f /var/log/nginx/access.log

# 查看错误日志
sudo tail -f /var/log/nginx/error.log

# 测试访问
curl http://localhost
```

## 🌍 访问方式

### 1. 通过公网 IP 访问

```
http://your-server-ip
```

例如：
```
http://101.35.79.76
```

### 2. 通过域名访问（推荐）

如果有域名，配置 DNS 解析到服务器 IP：

```
http://your-domain.com
https://your-domain.com  # 配置 SSL 后
```

### 3. 本地测试

在云服务器上测试：

```bash
curl http://localhost
```

## 🔧 前端 API 配置

前端需要配置正确的 API 地址。修改 `frontend/src/utils/request.js`：

```javascript
import axios from 'axios'

const request = axios.create({
  // 开发环境使用本地后端
  // baseURL: 'http://localhost:8081',
  
  // 生产环境使用服务器域名或IP
  baseURL: 'http://your-domain.com/api/',
  // 或使用相对路径（推荐，因为 Nginx 已配置代理）
  baseURL: '/api/',
  
  timeout: 10000
})
```

## 🚀 使用自动化部署脚本

项目提供了自动化部署脚本：

```bash
# 1. 在本地打包前端
cd frontend
npm run build

# 2. 上传脚本到服务器
scp deploy-frontend.sh root@your-server-ip:/root/

# 3. 在服务器上执行脚本
ssh root@your-server-ip
cd /root
sudo chmod +x deploy-frontend.sh
sudo ./deploy-frontend.sh
```

脚本会自动完成以下操作：
- ✅ 检查前端打包文件
- ✅ 安装 Nginx
- ✅ 创建部署目录
- ✅ 复制前端文件
- ✅ 配置 Nginx
- ✅ 启用站点
- ✅ 重启服务
- ✅ 配置防火墙

## 🔐 配置 HTTPS（可选）

### 使用 Let's Encrypt 免费证书

```bash
# 安装 Certbot
sudo apt-get install certbot python3-certbot-nginx -y

# 自动配置 SSL
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 手动配置 SSL

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # 其他配置...
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 📊 性能优化

### 1. 启用 Gzip 压缩

已在 Nginx 配置中启用，可减少传输大小 60-70%

### 2. 静态资源缓存

```nginx
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

### 3. CDN 加速（可选）

将静态资源上传到 CDN，加速访问

## 🐛 常见问题

### 1. 无法访问网站

**检查项：**
- Nginx 是否运行：`sudo systemctl status nginx`
- 防火墙是否开放端口 80
- 云服务商安全组是否开放端口 80
- 域名 DNS 解析是否正确

### 2. API 请求失败

**检查项：**
- 后端服务是否运行：`curl http://localhost:8081`
- Nginx 代理配置是否正确
- 后端防火墙是否允许 8081 端口

### 3. 页面刷新 404

**解决方案：**
确保 Nginx 配置中有：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 4. 静态资源 404

**检查项：**
- 文件是否正确上传到 `/var/www/html`
- 文件权限是否正确：`sudo chown -R www-data:www-data /var/www/html`

## 📝 维护命令

```bash
# 查看 Nginx 状态
sudo systemctl status nginx

# 重启 Nginx
sudo systemctl restart nginx

# 重新加载配置（不中断服务）
sudo systemctl reload nginx

# 查看访问日志
sudo tail -f /var/log/nginx/access.log

# 查看错误日志
sudo tail -f /var/log/nginx/error.log

# 更新前端文件
# 1. 本地重新打包
cd frontend && npm run build

# 2. 上传新文件
scp -r frontend/dist/* root@your-server-ip:/var/www/html/

# 3. 清除浏览器缓存访问
```

## 🎯 总结

前端在云服务器上的访问流程：

1. **用户访问** → 通过公网 IP 或域名访问云服务器
2. **Nginx 响应** → 返回前端静态文件（HTML/CSS/JS）
3. **浏览器加载** → 执行 JavaScript，渲染页面
4. **API 请求** → 前端发起 API 请求到 `/api/*`
5. **Nginx 代理** → 将 API 请求转发到后端（localhost:8081）
6. **后端处理** → Spring Boot 处理请求，返回 JSON 数据
7. **前端渲染** → 接收数据，更新页面

通过 Nginx 作为反向代理，实现了：
- ✅ 静态文件托管
- ✅ API 请求转发
- ✅ 负载均衡（可扩展）
- ✅ SSL/TLS 加密
- ✅ Gzip 压缩
- ✅ 缓存控制
