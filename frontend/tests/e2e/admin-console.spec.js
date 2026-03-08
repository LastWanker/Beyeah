const { test, expect } = require("@playwright/test")

async function installAdminApiMocks(page) {
  let goodsStatusUpdated = false
  let afterSaleApproved = false

  await page.route("**/api/v1/admin/**", async (route, request) => {
    const url = new URL(request.url())
    const path = url.pathname
    const method = request.method()

    const json = async (payload, status = 200) => {
      await route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify(payload),
      })
    }

    if (method === "POST" && path === "/api/v1/admin/auth/login") {
      return json({
        resultCode: 200,
        data: {
          adminId: 1,
          userName: "admin",
          nickName: "super admin",
          roles: ["SUPER_ADMIN"],
          permissions: [
            "admin:dashboard:read",
            "goods:read",
            "goods:write",
            "order:read",
            "order:fulfill",
            "order:close",
            "user:read",
            "user:lock",
            "after_sale:read",
            "after_sale:approve",
          ],
          token: "admin-e2e-token",
          tokenType: "Bearer",
        },
      })
    }

    if (method === "GET" && path === "/api/v1/admin/dashboard") {
      return json({
        resultCode: 200,
        data: {
          goodsCount: 1,
          orderCount: 1,
          userCount: 1,
          afterSalePendingCount: 1,
        },
      })
    }

    if (method === "GET" && path === "/api/v1/admin/goods") {
      return json({
        resultCode: 200,
        data: {
          list: [
            {
              goodsId: 10001,
              goodsName: "E2E Admin Goods",
              sellingPrice: 1499,
              stockNum: 99,
              goodsSellStatus: 1,
            },
          ],
          totalCount: 1,
          currentPage: 1,
        },
      })
    }

    if (method === "PUT" && path === "/api/v1/admin/goods/10001/status") {
      goodsStatusUpdated = true
      return json({ resultCode: 200, data: null })
    }

    if (method === "GET" && path === "/api/v1/admin/orders") {
      return json({
        resultCode: 200,
        data: { list: [], totalCount: 0, currentPage: 1 },
      })
    }

    if (method === "GET" && path === "/api/v1/admin/users") {
      return json({
        resultCode: 200,
        data: { list: [], totalCount: 0, currentPage: 1 },
      })
    }

    if (method === "GET" && path === "/api/v1/admin/after-sales") {
      return json({
        resultCode: 200,
        data: {
          list: [
            {
              afterSaleId: 9001,
              orderNo: "E2E-ORDER-1",
              afterSaleStatus: 0,
              refundAmount: 1499,
              reason: "changed mind",
              rejectReason: "",
            },
          ],
          totalCount: 1,
          currentPage: 1,
        },
      })
    }

    if (method === "PUT" && path === "/api/v1/admin/after-sales/9001/approve") {
      afterSaleApproved = true
      return json({
        resultCode: 200,
        data: {
          afterSaleId: 9001,
          afterSaleStatus: 3,
        },
      })
    }

    return json(
      {
        resultCode: 404,
        message: `unhandled mock: ${method} ${path}`,
      },
      404,
    )
  })

  return {
    isGoodsStatusUpdated: () => goodsStatusUpdated,
    isAfterSaleApproved: () => afterSaleApproved,
  }
}

test("admin console smoke flow should support goods status and after-sale approve", async ({ page }) => {
  const state = await installAdminApiMocks(page)

  await page.goto("/admin-login")
  await page.getByPlaceholder("admin username").fill("admin")
  await page.getByPlaceholder("admin password").fill("123456")
  await page.getByTestId("admin-login-submit").click()
  await expect(page).toHaveURL(/\/admin-console/)

  await page.getByRole("button", { name: "Up" }).first().click()
  await expect.poll(state.isGoodsStatusUpdated).toBe(true)

  await page.getByRole("tab", { name: "After-sales" }).click()
  await page.getByRole("button", { name: "Approve" }).first().click()
  await expect.poll(state.isAfterSaleApproved).toBe(true)
})
