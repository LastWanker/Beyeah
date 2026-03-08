import axios from "axios"
import { getActivePinia } from "pinia"
import router from "../router"
import { useUserStore } from "../stores/user"

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

const httpInstance = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 5000,
})

const readPersistedUser = () => {
  try {
    const raw = localStorage.getItem("user")
    return raw ? JSON.parse(raw) : {}
  } catch (_) {
    return {}
  }
}

const resolveToken = () => {
  if (getActivePinia()) {
    const userStore = useUserStore()
    if (userStore.token) {
      return userStore.token
    }
  }
  return readPersistedUser().token || ""
}

const handleUnauthorized = () => {
  if (getActivePinia()) {
    useUserStore().clearInfo()
  } else {
    localStorage.removeItem("user")
  }
  if (router.currentRoute.value.path !== "/login") {
    router.replace({ path: "/login", query: { redirect: router.currentRoute.value.fullPath } })
  }
}

httpInstance.interceptors.request.use(
  (config) => {
    const token = resolveToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (e) => Promise.reject(e),
)

httpInstance.interceptors.response.use(
  (res) => res.data,
  (e) => {
    const status = e?.response?.status
    const backendMessage = e?.response?.data?.message
    const message = backendMessage || e.message || "Request failed"

    if (status === 401) {
      handleUnauthorized()
    }

    return Promise.reject({
      ...e,
      status,
      message,
    })
  },
)

export default httpInstance
