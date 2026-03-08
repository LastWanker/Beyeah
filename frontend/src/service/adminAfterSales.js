import adminHttp from "../utils/adminAxios"

export const listAdminAfterSalesAPI = ({ page = 1, pageSize = 10, afterSaleStatus, orderNo } = {}) => {
  return adminHttp({
    url: "/api/v1/admin/after-sales",
    method: "GET",
    params: { page, pageSize, afterSaleStatus, orderNo },
  })
}

export const approveAdminAfterSaleAPI = ({ afterSaleId }) => {
  return adminHttp({
    url: `/api/v1/admin/after-sales/${afterSaleId}/approve`,
    method: "PUT",
  })
}

export const rejectAdminAfterSaleAPI = ({ afterSaleId, rejectReason }) => {
  return adminHttp({
    url: `/api/v1/admin/after-sales/${afterSaleId}/reject`,
    method: "PUT",
    data: { rejectReason },
  })
}
