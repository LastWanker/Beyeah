import httpInstance from "../utils/axios"

export const getCartListAPI = () => {
  return httpInstance({
    url: "/api/v1/cart",
    method: "GET",
  })
}

export const insertCartAPI = ({ goodsId, goodsCount }) => {
  return httpInstance({
    url: "/api/v1/cart/items",
    method: "POST",
    data: {
      goodsId,
      goodsCount,
    },
  })
}

export const delCartAPI = ({ cartItemId }) => {
  return httpInstance({
    url: `/api/v1/cart/items/${cartItemId}`,
    method: "DELETE",
  })
}
