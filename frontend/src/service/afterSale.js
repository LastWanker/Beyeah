import httpInstance from "../utils/axios"

export const createAfterSaleAPI = ({ orderNo, orderItemId, refundAmount, reason, description }) => {
  return httpInstance({
    url: "/api/v1/after-sales",
    method: "POST",
    data: {
      orderNo,
      orderItemId,
      refundAmount,
      reason,
      description,
    },
  })
}

export const listAfterSalesAPI = ({ page = 1, pageSize = 20, orderNo }) => {
  return httpInstance({
    url: "/api/v1/after-sales",
    method: "GET",
    params: {
      page,
      pageSize,
      orderNo,
    },
  })
}

export const getAfterSaleDetailAPI = ({ afterSaleNo }) => {
  return httpInstance({
    url: `/api/v1/after-sales/${afterSaleNo}`,
    method: "GET",
  })
}
