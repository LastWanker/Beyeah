# 3) 前后端接口差异（当前已知）

## 结论
`frontend(front0106)` 与 `backend(Beyeah1211)` 存在历史接口不一致，需要做一轮兼容。

## 已发现差异
1. `/getNew`
- 前端: `POST /getNew`
- 后端: `GET /getNew`（且返回字符串视图路径）

2. `/getHot`
- 前端: `POST /getHot`
- 后端: `GET /getHot`

3. 商品检索接口
- 前端使用: `/getGoodsByCate`, `/getGoodsByName`
- 后端 1211 中未找到同名接口

4. 购物车接口
- 前端使用: `/getCartList`, `POST /addCart`, `POST /delCart`
- 后端提供: `GET /cart/getCart/{userId}/{shopId}`, `POST /cart/addCart`, `DELETE /cart/delCart/{cids}`

5. 订单接口
- 前端使用: `/getOrderList`, `/searchOrderById`, `/createOrder`
- 后端 1211 中未找到同名接口

## 推荐修复策略
1. 新增 `CompatibilityController`（不推荐）
- 在 backend 增加一组 `/api/compat/*` 或直接同名接口，转调现有 service。

2. 或改前端 service（推荐）
- 把 `frontend/src/service/*.js` 改为对齐 1211 现有路由。
- 优点：后端更干净；缺点：前端改动范围更大。

## 参考来源
- 可优先对照 `references/Beyeah1206` 和 `references/Beyeah1205-resources`。
