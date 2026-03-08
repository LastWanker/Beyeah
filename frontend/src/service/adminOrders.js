import adminHttp from "../utils/adminAxios"

export const listAdminOrdersAPI = ({ page = 1, pageSize = 10, orderNo }) => {
  return adminHttp({
    url: "/api/v1/admin/orders",
    method: "GET",
    params: { page, pageSize, orderNo },
  })
}

export const checkDoneAdminOrderAPI = ({ orderId }) => {
  return adminHttp({
    url: `/api/v1/admin/orders/${orderId}/check-done`,
    method: "PUT",
  })
}

export const checkOutAdminOrderAPI = ({ orderId }) => {
  return adminHttp({
    url: `/api/v1/admin/orders/${orderId}/check-out`,
    method: "PUT",
  })
}

export const closeAdminOrderAPI = ({ orderId }) => {
  return adminHttp({
    url: `/api/v1/admin/orders/${orderId}/close`,
    method: "PUT",
    data: {},
  })
}
