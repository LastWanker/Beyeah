# 0) IDEA 新建项目 Beyeah（配置完整版）

## A. 新建空项目
1. 打开 IDEA -> `File` -> `New` -> `Project`。
2. 选择 `Empty Project`。
3. Name: `Beyeah`。
4. Location: `F:\GitHub\Beyeah`。
5. 勾选 `Create Git repository`（如果你准备在这里版本化）。

## B. 配置 Project SDK
1. `File` -> `Project Structure` -> `Project`。
2. `Project SDK` 优先选 JDK 17（你本机已有 17/21 时直接用 17）。
3. `Project language level` 选 `SDK default` 即可。
4. 说明：`backend/pom.xml` 里是 `java.version=1.8`，用 JDK 17 也能编译运行（按 1.8 目标字节码输出）。

## C. 导入后端模块（Maven）
1. 右键项目 -> `New` -> `Module from Existing Sources...`
2. 选择 `F:\GitHub\Beyeah\backend\pom.xml`。
3. 选择 Maven 导入。
4. 在 Maven 面板点击 Reload。

## D. 导入前端模块（Node）
1. 右键项目 -> `New` -> `Module from Existing Sources...`
2. 选择 `F:\GitHub\Beyeah\frontend`。
3. 在 `Settings -> Languages & Frameworks -> Node.js` 设置 Node 解释器（建议 Node 18 LTS）。
4. 在 Terminal 进入 `frontend` 执行 `npm install`。

## E. 配置运行项
### Backend 运行项
1. `Run -> Edit Configurations` -> `+` -> `Spring Boot`。
2. Main class: `SCG.beyeah1211.beyeah1211Application`。
3. Working directory: `F:\GitHub\Beyeah\backend`。
4. VM options 建议加：`-Dfile.encoding=UTF-8`。

### Frontend 运行项
1. `Run -> Edit Configurations` -> `+` -> `npm`。
2. Package.json: `F:\GitHub\Beyeah\frontend\package.json`。
3. Command: `run` Script: `dev`。

## F. 可选：组合运行
1. 新建 `Compound` 配置。
2. 加入上面的 Backend + Frontend。
3. 一键同时启动。
