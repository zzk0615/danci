# 单词默写纸管理系统 (Danci)

一个基于Spring Boot的单词学习管理系统，支持单词管理、标签分类和PDF默写纸生成功能。

## 📋 项目简介

本项目是一个专为英语学习者设计的单词管理系统，主要功能包括：

- **单词管理**：支持单个和批量添加、编辑、删除单词
- **标签分类**：为单词添加标签，便于分类管理（如CET-4、专业词汇等）
- **PDF生成**：自动生成默写纸，支持"默写中文"和"默写英文"两种模式
- **Web界面**：提供友好的Web管理界面，支持响应式设计

## 🚀 技术栈

- **后端框架**：Spring Boot 2.7.0
- **数据库**：MySQL 8.0
- **ORM框架**：Spring Data JPA
- **模板引擎**：Thymeleaf
- **PDF生成**：Apache PDFBox 2.0.29
- **构建工具**：Maven
- **Java版本**：JDK 17

## 📁 项目结构

```
src/main/java/com/danci/
├── common/           # 通用组件
│   └── ApiResponse.java    # 统一API响应格式
├── controller/       # 控制器层
│   ├── PageController.java     # 页面控制器
│   ├── TagController.java      # 标签管理API
│   └── WordController.java     # 单词管理API
├── entity/          # 实体类
│   ├── Tag.java              # 标签实体
│   └── Word.java             # 单词实体
├── repository/      # 数据访问层
│   ├── TagRepository.java        # 标签数据访问
│   └── WordRepository.java       # 单词数据访问
├── service/         # 业务逻辑层
│   ├── impl/            # 实现类
│   │   ├── TagServiceImpl.java   # 标签服务实现
│   │   └── WordServiceImpl.java  # 单词服务实现
│   ├── TagService.java       # 标签服务接口
│   └── WordService.java      # 单词服务接口
├── web/dto/         # 数据传输对象
│   ├── PdfGenerateRequest.java      # PDF生成请求
│   ├── TagCreateRequest.java        # 标签创建请求
│   ├── TagUpdateRequest.java        # 标签更新请求
│   ├── WordBatchCreateRequest.java  # 批量创建单词请求
│   ├── WordCreateRequest.java       # 单词创建请求
│   └── WordUpdateRequest.java      # 单词更新请求
└── danciApplication.java     # 启动类
```

## 🗄️ 数据库设计

### 标签表 (tags)
- `id`: 主键ID
- `tag_name`: 标签名称（唯一）
- `description`: 标签描述
- `create_time`: 创建时间
- `update_time`: 更新时间

### 单词表 (words)
- `id`: 主键ID
- `english`: 英文单词
- `chinese`: 中文释义
- `create_time`: 创建时间
- `update_time`: 更新时间

### 关系表 (word_tag_rel)
- `word_id`: 单词ID
- `tag_id`: 标签ID
- 支持多对多关系：一个单词可以有多个标签，一个标签可以包含多个单词

## 🔧 环境配置

### 1. 数据库配置
修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/danci?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
    username: your-username
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 数据库初始化
执行 `src/main/resources/danci.sql` 中的SQL脚本创建数据库表结构。

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd danci
```

### 2. 配置数据库
- 创建MySQL数据库
- 执行 `src/main/resources/danci.sql` 初始化表结构
- 修改 `application.yml` 中的数据库连接配置

### 3. 运行项目
```bash
mvn clean install
mvn spring-boot:run
```

### 4. 访问应用
打开浏览器访问：http://localhost:8080

## 📖 API文档

### 标签管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/tags` | 创建标签 |
| GET | `/api/tags` | 获取所有标签 |
| PUT | `/api/tags` | 更新标签 |
| DELETE | `/api/tags/{id}` | 删除标签 |

### 单词管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/words` | 创建单词 |
| POST | `/api/words/batch` | 批量创建单词 |
| GET | `/api/words/{id}` | 获取单词详情 |
| PUT | `/api/words` | 更新单词 |
| DELETE | `/api/words/{id}` | 删除单词 |
| GET | `/api/words/byTag` | 按标签获取单词 |
| POST | `/api/words/generatePdf` | 生成PDF默写纸 |

## 🎯 核心功能

### 1. 单词管理
- **单个添加**：支持添加英文单词和中文释义
- **批量导入**：支持按格式批量导入单词（格式：英文,中文）
- **标签关联**：为单词添加标签进行分类管理
- **搜索功能**：支持按英文或中文搜索单词

### 2. 标签管理
- **创建标签**：如"CET-4"、"专业词汇"等
- **标签描述**：为标签添加描述信息
- **标签编辑**：支持修改标签名称和描述

### 3. PDF默写纸生成
- **两种模式**：
  - 默写中文：左侧显示英文，右侧空白
  - 默写英文：左侧显示中文，右侧空白（带四线三格）
- **智能布局**：A4纸张，3列布局，自动分页
- **字体支持**：支持中文字体显示
- **随机排序**：每次生成的单词顺序随机

### 4. Web界面特性
- **响应式设计**：支持桌面和移动设备
- **分页显示**：支持数据分页浏览
- **实时搜索**：支持关键字实时搜索
- **批量操作**：支持批量导入和删除

## 🎨 界面预览

系统提供直观的Web管理界面，包括：
- 标签管理区域：创建、编辑、删除标签
- 单词管理区域：添加、编辑、搜索单词
- PDF生成区域：选择标签和模式，生成默写纸

## 📝 使用说明

### 1. 创建标签
1. 在"标签管理"区域输入标签名称和描述
2. 点击"新增标签"按钮

### 2. 添加单词
1. 选择关联标签
2. 输入英文单词和中文释义
3. 点击"新增单词"按钮

### 3. 批量导入
1. 在文本框中按格式输入：`英文,中文`（每行一个）
2. 选择关联标签
3. 点击"批量导入"按钮

### 4. 生成PDF
1. 选择要包含的标签（可多选）
2. 选择默写模式
3. 点击"生成PDF"按钮下载

## 🔧 开发说明

### 项目特点
- **RESTful API设计**：标准的REST接口设计
- **统一响应格式**：使用`ApiResponse`统一API响应
- **数据验证**：使用Bean Validation进行参数验证
- **事务管理**：关键操作使用`@Transactional`注解
- **异常处理**：完善的异常处理机制

### 扩展建议
- 添加用户认证和权限管理
- 支持单词收藏和复习功能
- 添加学习进度统计
- 支持更多导出格式（Word、Excel等）
- 添加单词发音功能

## 📄 许可证

本项目采用MIT许可证，详情请查看LICENSE文件。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件

---

**注意**：使用前请确保已正确配置数据库连接，并执行相应的SQL脚本初始化数据库表结构。
