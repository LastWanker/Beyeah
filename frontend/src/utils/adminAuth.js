const ADMIN_AUTH_KEY = "adminAuth"

export const getAdminAuth = () => {
  try {
    const raw = localStorage.getItem(ADMIN_AUTH_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch (_) {
    return {}
  }
}

export const setAdminAuth = (value) => {
  localStorage.setItem(ADMIN_AUTH_KEY, JSON.stringify(value || {}))
}

export const clearAdminAuth = () => {
  localStorage.removeItem(ADMIN_AUTH_KEY)
}

export const getAdminToken = () => {
  return getAdminAuth().token || ""
}
