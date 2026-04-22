# LiuMing-backend（流明）

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-latest-orange)](https://spring.io/projects/spring-ai)
[![Qwen](https://img.shields.io/badge/Qwen-3.5-9cf)](https://github.com/QwenLM/Qwen)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

> 智能体照亮复杂的数据与任务

**LiuMing**（流明）是一个基于 Java 的智能体（Agent）后端项目，集成大语言模型（Qwen 3.5），提供记忆（Memory）、检索增强生成（RAG）、工具调用（Tool Calling）和模型控制协议（MCP）等核心能力，旨在构建可扩展、可编排的智能体系统。

## ✨ 核心特性

- **🤖 多模型支持**：深度集成 Qwen 3.5，支持对话、推理与工具调用
- **🧠 记忆管理**：短期记忆与会话记忆相结合，支持上下文感知
- **🔍 检索增强**：基于 ElasticSearch 与 PGVector 实现高效的 RAG 能力
- **🛠️ 工具调用**：灵活的 Tool Calling 机制，支持自定义工具注册与执行
- **🧩 MCP 支持**：模型控制协议，提供统一的智能体控制接口
- **🚀 开箱即用**：基于 Spring Boot，快速部署与扩展

## 🏗️ 技术栈

### 后端
- **Java 17+** - 基础编程语言
- **Spring Boot 3.x** - 应用开发框架
- **Spring AI** - AI 集成框架
- **PGVector** - 向量数据库（PostgreSQL 扩展）
- **ElasticSearch** - 全文检索与文档存储
- **Maven** - 项目构建与依赖管理

### 前端（待开发）
- **Vue 3** - 前端框架
- **TypeScript** - 类型安全的 JavaScript 超集
- **VibeCoding** - 前端代码生成工具

## 📁 项目结构
````
LiuMing-backend/
├── liumingservices/ # 主服务模块
├── common/ # 公共组件与工具
├── model/ # 数据模型与实体
├── sdk/ # 客户端 SDK
├── services/ # 业务服务层
├── logs/ # 日志目录
├── sql/ # 数据库脚本
├── .gitignore
├── pom.xml # Maven 配置
└── README.md
````
## 🚀 快速开始

### 环境要求
- JDK 17 或更高版本
- Maven 3.6+
- PostgreSQL 12+（启用 PGVector 扩展）
- ElasticSearch 8.x
- Qwen API 密钥（或本地部署的 Qwen 服务）

### 部署步骤

1. **克隆项目**
   bash
   git clone https://github.com/Yummy-Stack/LiuMing-backend.git
   cd LiuMing-backend
2. **配置数据库**
   sql
   -- 创建 PostgreSQL 数据库并启用 vector 扩展
   CREATE DATABASE liuming;
   CREATE EXTENSION IF NOT EXISTS vector;
3. **配置文件**
   复制 `application.yml.template` 为 `application.yml` 并修改配置：
   yaml
   spring:
   datasource:
   url: jdbc:postgresql://localhost:5432/liuming
   username: your_username
   password: your_password
   spring.ai:
   qwen:
   api-key: ${QWEN_API_KEY}
   base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
   elasticsearch:
   uris: http://localhost:9200
4. **构建与运行**
   bash
   编译项目
   mvn clean compile
   运行测试
   mvn test
   打包
   mvn package
   启动服务
   java -jar target/liuming-backend-1.0.0.jar
5. **验证部署**
   bash
   curl http://localhost:8080/actuator/health
## 📖 API 使用示例

### 工具注册示例
java
``````
package com.liumingservices.ai.love.tools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
@Configuration
@Slf4j
public class ToolRegistration {
@Bean
public ToolCallback[] allTools() {
FileOperationTool fileOperationTool = new FileOperationTool();
return ToolCallbacks.from(fileOperationTool);
}
}
```````
### 调用智能体
java
````
// 通过 Spring AI 调用智能体
AiResponse response = aiClient.call(
AiRequest.builder()
.message("分析当前系统状态")
.tools("fileOperationTool", "systemMonitorTool")
.build()
);
````
## 🔧 配置说明

### 核心配置项
| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.ai.qwen.api-key` | Qwen API 密钥 | 必填 |
| `spring.ai.qwen.base-url` | Qwen 服务地址 | https://dashscope.aliyuncs.com/compatible-mode/v1 |
| `liuming.memory.enabled` | 记忆功能开关 | true |
| `liuming.rag.enabled` | RAG 功能开关 | true |
| `liuming.tools.auto-register` | 工具自动注册 | true |

### 工具配置
项目支持以下类型工具：
- **文件操作工具**：读写本地文件
- **网络请求工具**：HTTP/HTTPS 请求
- **数据库查询工具**：SQL 执行
- **系统监控工具**：服务器状态监控
- **自定义工具**：用户自定义功能扩展

## 🤝 参与贡献

我们欢迎任何形式的贡献！以下是参与项目的方式：

1. **报告问题**：在 [Issues](https://github.com/Yummy-Stack/LiuMing-backend/issues) 页面提交 Bug 或功能请求
2. **提交代码**：Fork 项目并提交 Pull Request
3. **改进文档**：帮助完善项目文档和示例
4. **分享用例**：分享你使用 LiuMing 的案例和经验

### 贡献步骤
1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- 感谢 [Spring AI](https://spring.io/projects/spring-ai) 团队提供优秀的 AI 集成框架
- 感谢 [Qwen](https://github.com/QwenLM/Qwen) 团队开发的高质量大语言模型
- 感谢所有为项目贡献代码和提出建议的开发者

## 📞 联系方式

- **项目作者**：Yummy-Stack
- **项目主页**：https://github.com/Yummy-Stack/LiuMing-backend
- **问题反馈**：通过 GitHub Issues 提交

---

> **流明**——用智能体之光照亮数据与任务的复杂世界 ✨