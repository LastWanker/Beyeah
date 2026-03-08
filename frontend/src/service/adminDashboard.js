import adminHttp from "../utils/adminAxios"

export const getAdminDashboardAPI = () => {
  return adminHttp({
    url: "/api/v1/admin/dashboard",
    method: "GET",
  })
}
