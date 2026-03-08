import adminHttp from "../utils/adminAxios"
import { clearAdminAuth, setAdminAuth } from "../utils/adminAuth"

export const adminLoginAPI = async ({ userName, password }) => {
  const res = await adminHttp({
    url: "/api/v1/admin/auth/login",
    method: "POST",
    data: { userName, password },
  })
  if (res?.resultCode === 200 && res?.data?.token) {
    setAdminAuth(res.data)
  }
  return res
}

export const adminMeAPI = () => {
  return adminHttp({
    url: "/api/v1/admin/auth/me",
    method: "GET",
  })
}

export const adminLogout = () => {
  clearAdminAuth()
}
