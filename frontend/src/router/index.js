import { createRouter, createWebHistory } from "vue-router"
import Login from "../views/Login.vue"
import Home from "../views/Home.vue"
import UserInfo from "../views/UserInfo.vue"
import UserCart from "../views/UserCart.vue"
import ProductDetail from "../views/ProductDetail.vue"
import Checkout from "../views/Checkout.vue"
import productlist from "../views/ProductList.vue"
import productsearch from "../views/ProductSearch.vue"
import AdminLogin from "../views/AdminLogin.vue"
import AdminConsole from "../views/AdminConsole.vue"
import { getAdminToken } from "../utils/adminAuth"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: "/login", component: Login },
    { path: "/", component: Home },
    { path: "/info", component: UserInfo },
    { path: "/cart", component: UserCart },
    { path: "/product/:id", component: ProductDetail },
    { path: "/checkout", component: Checkout },
    { path: "/category/:id", component: productlist },
    { path: "/search/:name", component: productsearch },
    { path: "/admin-login", component: AdminLogin },
    { path: "/admin-console", component: AdminConsole },
  ],
})

const protectedPaths = new Set(["/cart", "/checkout", "/info"])
const adminProtectedPaths = new Set(["/admin-console"])

const hasToken = () => {
  try {
    const raw = localStorage.getItem("user")
    if (!raw) {
      return false
    }
    const parsed = JSON.parse(raw)
    return !!parsed.token
  } catch (_) {
    return false
  }
}

router.beforeEach((to) => {
  const loggedIn = hasToken()
  const adminLoggedIn = !!getAdminToken()
  if (protectedPaths.has(to.path) && !loggedIn) {
    return { path: "/login", query: { redirect: to.fullPath } }
  }
  if (adminProtectedPaths.has(to.path) && !adminLoggedIn) {
    return { path: "/admin-login", query: { redirect: to.fullPath } }
  }
  if (to.path === "/login" && loggedIn) {
    const redirect = typeof to.query.redirect === "string" ? to.query.redirect : "/"
    return redirect
  }
  if (to.path === "/admin-login" && adminLoggedIn) {
    const redirect = typeof to.query.redirect === "string" ? to.query.redirect : "/admin-console"
    return redirect
  }
  return true
})

export default router
