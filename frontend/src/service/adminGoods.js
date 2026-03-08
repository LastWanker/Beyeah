import adminHttp from "../utils/adminAxios"

export const listAdminGoodsAPI = ({ page = 1, pageSize = 10 }) => {
  return adminHttp({
    url: "/api/v1/admin/goods",
    method: "GET",
    params: { page, pageSize },
  })
}

export const createAdminGoodsAPI = (payload) => {
  return adminHttp({
    url: "/api/v1/admin/goods",
    method: "POST",
    data: payload,
  })
}

export const updateAdminGoodsAPI = ({ goodsId, ...payload }) => {
  return adminHttp({
    url: `/api/v1/admin/goods/${goodsId}`,
    method: "PUT",
    data: payload,
  })
}

export const updateAdminGoodsStatusAPI = ({ goodsId, sellStatus }) => {
  return adminHttp({
    url: `/api/v1/admin/goods/${goodsId}/status`,
    method: "PUT",
    data: { sellStatus },
  })
}
