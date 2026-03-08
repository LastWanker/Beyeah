# 后端集成测试 + 前端 E2E + CI 落地经验（第二阶段）

## 1. 后端集成测试（Spring Boot + Testcontainers）

### 目标
- 覆盖 API 主链路：登录注册、商品查询、购物车、下单成功/失败、订单查询。
- 测试与本机 MySQL 解耦，避免“我本地能跑，CI 挂掉”。

### 做法
1. 在 `backend/pom.xml` 增加 Testcontainers 依赖（`junit-jupiter`、`mysql`）。
2. 使用 `@Testcontainers + MySQLContainer` 启动临时 MySQL 8。
3. 通过 `withInitScript("sql/test_schema.sql")` 初始化最小可用表结构与种子数据。
4. 用 `@DynamicPropertySource` 注入测试期 `spring.datasource.*` 与认证配置。
5. 每个测试前清理业务表，保证用例隔离和可重复。

### 文件
- `backend/src/test/java/SCG/beyeah1211/api/ApiIntegrationTest.java`
- `backend/src/test/resources/sql/test_schema.sql`

### 经验
- 不要依赖全量生产库结构，测试只保留“业务必需最小子集”，执行更快更稳。
- 用随机用户名避免并发冲突。
- Docker 不可用时允许跳过（`disabledWithoutDocker = true`），避免本地开发被阻塞；CI 里应确保 Docker 可用并真实执行。

## 2. 前端 E2E（Playwright）

### 目标
- 覆盖交易闭环：搜索 -> 详情 -> 加购 -> 结算 -> 下单。
- 验证受保护路由登录态守卫生效。

### 做法
1. 新增 `frontend/playwright.config.js`，用 `webServer` 启动 Vite。
2. 用 `tests/e2e/checkout-flow.spec.js` 编排关键路径。
3. 对 `http://localhost:23333/api/v1/**` 做统一 mock，确保 CI 稳定。
4. 对关键控件补 `data-testid`（加购、结算、提交订单、商品链接），减少文案/样式变更带来的脆弱性。
5. 登录步骤采用“登录态注入”（写入 `localStorage.user`），绕开验证码随机性。

### 文件
- `frontend/playwright.config.js`
- `frontend/tests/e2e/checkout-flow.spec.js`
- `frontend/src/components/GoodsItem.vue`
- `frontend/src/views/ProductDetail.vue`
- `frontend/src/views/UserCart.vue`
- `frontend/src/views/Checkout.vue`

### 经验
- 带验证码的登录页不适合做纯 UI 自动化登录，推荐“接口登录或登录态注入 + 交易链路 UI 断言”的组合。
- E2E 重点是“用户关键路径可用”，不是复刻后端逻辑；后端正确性由集成测试兜底。
- 断言请求头（`Authorization`）很关键，可避免“页面看起来正常但鉴权没带上 token”。

## 3. CI（GitHub Actions）

### 目标
- 每次提交自动验证：后端测试通过、前端 E2E 通过、前端可构建。

### 做法
1. 新增 `.github/workflows/ci.yml`。
2. 后端 Job：JDK17 + `./mvnw -B test`（运行 Testcontainers 测试）。
3. 前端 Job：`npm ci` -> `npx playwright install --with-deps chromium` -> `npm run test:e2e` -> `npm run build`。

### 文件
- `.github/workflows/ci.yml`

### 经验
- 先跑 E2E 再 build，能更快暴露业务回归问题。
- Playwright 浏览器安装建议固定在 CI 步骤中，避免 runner 环境差异。
- 后续可加：测试报告上传、失败截图/trace 上传、分支保护规则（必须 CI 通过）。

## 4. 建议的长期策略
- 后端：把异常路径再补齐（库存不足、非法参数、鉴权失败细分）。
- 前端：按业务域拆分 E2E（账号、商品、交易）并控制总时长。
- CI：增加定时 nightly 全量回归，PR 阶段跑关键冒烟集。
