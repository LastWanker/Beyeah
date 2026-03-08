import adminHttp from "../utils/adminAxios"

export const listAdminUsersAPI = ({ page = 1, pageSize = 10 } = {}) => {
  return adminHttp({
    url: "/api/v1/admin/users",
    method: "GET",
    params: { page, pageSize },
  })
}

export const updateAdminUserLockAPI = ({ userId, lockStatus }) => {
  return adminHttp({
    url: `/api/v1/admin/users/${userId}/lock`,
    method: "PUT",
    data: { lockStatus },
  })
}
