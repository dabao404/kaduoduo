# 贡献指南 / Contributing Guide

## 中文

欢迎提交 Issue 和 Pull Request。为了让维护成本可控，请尽量让每次改动聚焦一个明确目标。

### 开发约定

- 使用 Kotlin 和 Jetpack Compose。
- 数据访问统一通过 Repository，不在 UI 层直接调用 Dao。
- Room 金额字段使用“分”为单位保存，避免浮点精度问题。
- 用户可见文案保持简洁，避免在页面上堆叠说明性文字。
- 新增复杂业务逻辑时，优先补充 ViewModel 或 Repository 层测试。

### Pull Request 建议

- 说明改动目的。
- 说明已验证的内容，例如构建、运行或手动测试。
- UI 改动建议附截图。
- 不要提交签名文件、密钥、构建产物或本地配置文件。

## English

Issues and Pull Requests are welcome. To keep maintenance manageable, please keep each change focused on one clear goal.

### Development Guidelines

- Use Kotlin and Jetpack Compose.
- Access data through the Repository layer. Do not call DAOs directly from UI code.
- Store money values in Room as cents to avoid floating-point precision issues.
- Keep user-facing copy concise and avoid overloading screens with explanatory text.
- For complex business logic, prefer adding tests around the ViewModel or Repository layer.

### Pull Request Suggestions

- Explain the purpose of the change.
- Describe what you verified, such as build, runtime behavior, or manual testing.
- Attach screenshots for UI changes when possible.
- Do not commit signing files, secrets, build outputs, or local configuration files.
