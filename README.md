# SmartDiary - 智能日记平台

<div align="center">

![SmartDiary Logo](logo.png)

一个现代化的日记记录与社交分享平台，让你轻松记录生活点滴，与好友分享精彩瞬间。



</div>

## ✨ 功能特性

### 📝 日记管理
- 富文本日记编辑（支持图片、格式化文本）
- 日记草稿自动保存
- 日记分类与标签管理
- 私密日记与公开日记设置

### 👥 社交互动
- 日记圈动态流
- 好友系统（添加好友、好友请求）
- 评论与点赞功能
- 实时互动通知

### 🔐 用户系统
- 用户注册与登录
- 手机号验证
- 密码找回功能
- 用户隐私设置
- 管理员封禁管理

### 🎨 界面设计
- 现代化 UI 设计
- 响应式布局
- 流畅的动画效果
- 视频背景支持

## 📸 应用截图

<!-- 在此处添加应用截图 -->
<!-- 
![登录页面](screenshots/login.png)
![首页](screenshots/home.png)
![日记编辑](screenshots/editor.png)
![日记圈](screenshots/circle.png)
![好友列表](screenshots/friends.png)
-->

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 3.5.6
- **语言**: Java 21
- **数据库**: MySQL
- **ORM**: MyBatis 3.0.3
- **构建工具**: Maven
- **其他**: Lombok

### 前端
- **框架**: Vue 3.5.22
- **语言**: TypeScript
- **构建工具**: Vite 7.1.11
- **UI 组件库**: Element Plus 2.11.8
- **路由**: Vue Router 4.6.3
- **HTTP 客户端**: Axios
- **富文本编辑器**: Quill 2.0.3
- **其他**: element-china-area-data（地区选择）

## 📁 项目结构

```
SmartDiary/
├── back/                      # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── org/example/back/
│   │       │       ├── config/      # 配置类（CORS等）
│   │       │       ├── controller/  # 控制器层
│   │       │       ├── mapper/      # MyBatis Mapper
│   │       │       ├── pojo/        # 实体类
│   │       │       ├── service/     # 服务层
│   │       │       └── util/        # 工具类
│   │       └── resources/
│   │           ├── application.properties  # 应用配置
│   │           └── smartdiary.sql          # 数据库脚本
│   └── pom.xml
├── front/                     # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 组件
│   │   ├── composables/       # 组合式函数
│   │   ├── router/            # 路由配置
│   │   ├── styles/            # 样式文件
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面组件
│   │   ├── App.vue
│   │   └── main.ts
│   └── package.json
└── README.md
```

## 🚀 快速开始

### 环境要求

- **后端**: JDK 21+, Maven 3.6+, MySQL 8.0+
- **前端**: Node.js 20.19.0+ 或 22.12.0+

### 后端部署

1. 克隆项目
```bash
git clone https://github.com/yourusername/SmartDiary.git
cd SmartDiary/back
```

2. 配置数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE smartdiary;
```

3. 执行数据库脚本
```bash
# 导入 smartdiary.sql 到数据库
mysql -u root -p smartdiary < src/main/resources/smartdiary.sql
```

4. 修改配置文件
```properties
# 编辑 src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartdiary
spring.datasource.username=your_username
spring.datasource.password=your_password
```

5. 启动后端服务
```bash
./mvnw spring-boot:run
# 或使用 IDE 运行 SmartDiaryApplication.java
```


### 前端部署

1. 进入前端目录
```bash
cd front
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```


4. 构建生产版本
```bash
npm run build
```

## 🔧 配置说明

### 后端配置 (application.properties)
- 数据库连接配置
- 服务器端口配置
- CORS 跨域配置

### 前端配置
- API 基础路径配置
- 路由配置 (src/router/index.ts)

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request


## 📄 许可证

本项目已获得软件著作权,已开源

## 👨‍💻 作者

ZT/YZW


<div align="center">

如果这个项目对你有帮助，请给一个 ⭐️

Made with ❤️ by SmartDiary Team

</div>