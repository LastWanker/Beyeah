import axios from "axios"
import router from "../router"
import { clearAdminAuth, getAdminToken } from "./adminAuth"

const resolveBaseURL = () => {
  const configured = (import.meta.env.VITE_API_BASE_URL || "").trim()
  if (configured) {
    return configured.replace(/\/+$/, "")
  }

  if (typeof window !== "undefined") {
    const host = window.location.hostname
    if (host === "localhost" || host === "127.0.0.1") {
      return `${window.location.protocol}//${host}:23333`
    }
  }
  return "http://localhost:23333"
}

const adminHttp = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 8000,
})

const handleUnauthorized = () => {
  clearAdminAuth()
  if (router.currentRoute.value.path !== "/admin-login") {
    router.replace({ path: "/admin-login", query: { redirect: router.currentRoute.value.fullPath } })
  }
}

adminHttp.interceptors.request.use(
  (config) => {
    const token = getAdminToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (e) => Promise.reject(e),
)

adminHttp.interceptors.response.use(
  (res) => res.data,
  (e) => {
    const status = e?.response?.status
    const backendMessage = e?.response?.data?.message
    const message =
      backendMessage || (status === 403 ? "Permission denied" : e.message || "Admin request failed")

    if (status === 401) {
      handleUnauthorized()
    }
    return Promise.reject({ ...e, status, message })
  },
)

export default adminHttp
