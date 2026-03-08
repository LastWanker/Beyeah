import httpInstance from "../utils/axios"

export const getOrderListAPI = ({ page = 1, pageSize = 10 }) => {
  return httpInstance({
    url: "/api/v1/orders",
    method: "GET",
    params: {
      page,
      pageSize,
    },
  })
}

export const searchOrderAPI = ({ orderNo }) => {
  return httpInstance({
    url: `/api/v1/orders/${orderNo}`,
    method: "GET",
  })
}

export const createOrderAPI = ({ addressId, address, goods, idempotencyKey }) => {
  const key = (idempotencyKey || "").trim()
  return httpInstance({
    url: "/api/v1/orders",
    method: "POST",
    headers: key
      ? {
          "Idempotency-Key": key,
        }
      : undefined,
    data: {
      addressId: addressId || undefined,
      address,
      goods,
      idempotencyKey: key || undefined,
    },
  })
}

export const payOrderAPI = ({ orderNo, payType = 2 }) => {
  return httpInstance({
    url: `/api/v1/orders/${orderNo}/pay`,
    method: "POST",
    data: {
      payType,
    },
  })
}
