# 2) 复活执行清单

## Step 1: 数据库
1. 在 MySQL 创建库：`beyeah`。
2. 导入以下 SQL（二选一，推荐 backend 里这组）：
   - `backend/src/main/resources/sql4development/beyeah_schema.sql`
   - `backend/src/main/resources/sql4development/beyeah_DB_insertData.sql`

## Step 2: 后端配置
1. 编辑 `backend/src/main/resources/application.properties`：
   - `spring.datasource.username`
   - `spring.datasource.password`
2. 默认端口是 `23333`，与前端已对齐。

## Step 3: 前端配置
1. 确认 `frontend/src/utils/axios.js` 的 `baseURL` 为 `http://localhost:23333`。
2. 在 `frontend` 目录执行：
   - `npm install`
   - `npm run dev`
3. 前端默认端口：`22223`。

## Step 4: 启动顺序
1. 先启动 backend。
2. 再启动 frontend。
3. 打开 `http://localhost:22223`。

## Step 5: 首轮验证
- 登录/注册
- 首页商品加载
- 购物车流程
- 订单流程

如果首页接口不通，先看 `docs/03-API-Gap.md`。
