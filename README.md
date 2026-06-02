# 卡多多 kaduoduo

[中文](#中文) | [English](#english)

## 中文

卡多多是一个使用 Kotlin、Jetpack Compose、MVVM 和 Room 构建的信用卡管理 Android App。项目目前处于 MVP 阶段，重点覆盖信用卡开卡行、年费达标进度、额度变更记录和权益核销等本地管理场景。

### 功能

- 信用卡列表：首页以卡片形式展示信用卡、固定额度、临时额度和年费达标进度。
- 开卡行选择：内置国内常见银行，也支持自定义银行名称。
- 年费规则：支持自然年或核卡日周期，支持按消费笔数、金额或两者共同达标。
- 额度管理：记录固定额度、临时额度及临额失效日期，并在详情页保留调额历史。
- 权益管理：支持添加权益、记录总次数和有效期，并可一键核销剩余次数。
- 本地存储：使用 Room 保存数据，不依赖远程服务。

### 技术栈

- Kotlin
- Jetpack Compose Material 3
- MVVM
- Room
- Kotlin Coroutines / Flow
- Navigation Compose

### 项目结构

```text
app/src/main/java/com/kaduoduo
├── data/local      # Room Entity、Dao、Database
├── domain          # Repository、ViewModel、ViewModelFactory
├── ui              # Compose 页面和展示格式化逻辑
└── ui/theme        # Compose 主题
```

### 本地运行

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 选择 `app` 配置并运行到模拟器或真机。

当前仓库没有提交 Gradle Wrapper。如果你希望命令行构建，先在安装 Gradle 的环境中生成 Wrapper：

```bash
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
```

### 开源发布前检查

- 不要提交 `local.properties`、签名文件、密钥或 `google-services.json`。
- 首次发布前建议补充应用图标和真实银行 Logo 资源。
- 如计划上架或分发 APK，请增加 release 签名配置，并确保签名文件不进入仓库。
- 如要让 GitHub Actions 使用 Wrapper 构建，请先生成并提交 Gradle Wrapper 文件。

### 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。

## English

kaduoduo is an Android credit card management app built with Kotlin, Jetpack Compose, MVVM, and Room. The project is currently in the MVP stage and focuses on local management workflows such as issuer selection, annual fee progress tracking, credit limit history, and benefit redemption.

### Features

- Credit card list: shows cards, fixed limits, temporary limits, and annual fee progress on the home screen.
- Bank selection: includes common Chinese banks and supports custom bank names.
- Annual fee rules: supports calendar-year or card-issue-date cycles, with spending count, spending amount, or combined requirements.
- Limit management: records fixed limits, temporary limits, temporary limit expiration dates, and limit adjustment history.
- Benefit management: supports adding benefits, tracking total and remaining uses, expiration dates, and one-tap redemption.
- Local storage: stores data locally with Room and does not depend on a remote service.

### Tech Stack

- Kotlin
- Jetpack Compose Material 3
- MVVM
- Room
- Kotlin Coroutines / Flow
- Navigation Compose

### Project Structure

```text
app/src/main/java/com/kaduoduo
├── data/local      # Room entities, DAOs, and database
├── domain          # Repository, ViewModels, and ViewModelFactory
├── ui              # Compose screens and UI formatting helpers
└── ui/theme        # Compose theme
```

### Run Locally

1. Open the project root directory with Android Studio.
2. Wait for Gradle sync to finish.
3. Select the `app` configuration and run it on an emulator or device.

This repository does not currently include a Gradle Wrapper. If you want to build from the command line, generate the Wrapper in an environment where Gradle is installed:

```bash
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
```

### Open Source Release Checklist

- Do not commit `local.properties`, signing files, secrets, or `google-services.json`.
- Add the app icon and real bank logo assets before the first public release.
- If you plan to publish or distribute APKs, add release signing configuration and keep signing files out of the repository.
- If you want GitHub Actions to build with the Wrapper, generate and commit the Gradle Wrapper files first.

### License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
