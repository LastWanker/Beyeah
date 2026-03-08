const { test, expect } = require("@playwright/test");

const USER_STORAGE = JSON.stringify({
  id: 10001,
  token: "e2e-token",
});

const GOODS = {
  goodsId: 10001,
  goodsName: "E2E Phone",
  goodsIntro: "playwright goods",
  goodsCoverImg: "https://example.com/e2e-phone.png",
  originalPrice: 1999,
  sellingPrice: 1499,
};

async function installApiMocks(page) {
  let orderCreated = false;
  let authHeaderSeen = false;
  let cartItems = [];
  const addresses = [
    {
      addressId: 7001,
      consignee: "E2E User",
      phone: "13800138000",
      province: "Shanghai",
      city: "Shanghai",
      district: "Pudong",
      detail: "E2E Road 1",
      isDefault: 1,
    },
  ];

  await page.route("**/api/v1/**", async (route, request) => {
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    const authHeader = request.headers()["authorization"] || "";

    if (authHeader === "Bearer e2e-token") {
      authHeaderSeen = true;
    }

    const json = async (payload, status = 200) => {
      await route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify(payload),
      });
    };

    if (method === "GET" && path === "/api/v1/goods") {
      return json({
        resultCode: 200,
        data: {
          list: [GOODS],
          totalCount: 1,
          currentPage: 1,
          totalPage: 1,
        },
      });
    }

    if (method === "GET" && path === `/api/v1/goods/${GOODS.goodsId}`) {
      return json({
        resultCode: 200,
        data: GOODS,
      });
    }

    if (method === "POST" && path === "/api/v1/cart/items") {
      cartItems = [
        {
          cartItemId: 9001,
          goodsId: GOODS.goodsId,
          goodsName: GOODS.goodsName,
          sellingPrice: GOODS.sellingPrice,
          goodsCount: 1,
          goodsCoverImg: GOODS.goodsCoverImg,
        },
      ];
      return json({
        resultCode: 200,
        data: null,
      });
    }

    if (method === "GET" && path === "/api/v1/cart") {
      return json({
        resultCode: 200,
        data: orderCreated ? [] : cartItems,
      });
    }

    if (method === "GET" && path === "/api/v1/users/me") {
      return json({
        resultCode: 200,
        data: {
          nickname: "E2E User",
          username: "13800138000",
        },
      });
    }

    if (method === "GET" && path === "/api/v1/addresses") {
      return json({
        resultCode: 200,
        data: addresses,
      });
    }

    if (method === "POST" && path === "/api/v1/orders") {
      const payload = request.postDataJSON();
      if (!payload?.goods?.length || !payload?.addressId) {
        return json({
          resultCode: 500,
          message: "invalid order payload",
        });
      }

      orderCreated = true;
      return json({
        resultCode: 200,
        data: {
          orderNo: "E2E202603080001",
        },
      });
    }

    return json(
      {
        resultCode: 404,
        message: `unhandled mock: ${method} ${path}`,
      },
      404,
    );
  });

  return {
    isOrderCreated: () => orderCreated,
    hasAuthHeader: () => authHeaderSeen,
  };
}

test("unauthenticated user should be redirected to login for protected route", async ({ page }) => {
  await page.goto("/cart");
  await expect(page).toHaveURL(/\/login/);
});

test("logged-in user can finish search to checkout flow", async ({ page }) => {
  const apiState = await installApiMocks(page);
  await page.addInitScript((value) => {
    localStorage.setItem("user", value);
  }, USER_STORAGE);

  await page.goto("/search/phone");
  await expect(page.getByTestId("goods-item-link").first()).toBeVisible();

  await page.getByTestId("goods-item-link").first().click();
  await expect(page).toHaveURL(/\/product\/10001/);
  await page.getByTestId("add-cart-btn").click();

  await page.goto("/cart");
  await expect(page.locator(".xtx-cart-page .goods .name").first()).toContainText("E2E Phone");
  await page.locator(".xtx-cart-page thead .el-checkbox").click();
  await page.getByTestId("checkout-btn").click();

  await expect(page).toHaveURL(/\/checkout/);
  await page.getByTestId("submit-order-btn").click();

  await expect.poll(apiState.isOrderCreated).toBe(true);
  await expect.poll(apiState.hasAuthHeader).toBe(true);
});
