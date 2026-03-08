<script setup>
import { computed, onMounted, reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { adminLogout } from "../service/adminAuth"
import { getAdminDashboardAPI } from "../service/adminDashboard"
import { listAdminGoodsAPI, updateAdminGoodsStatusAPI } from "../service/adminGoods"
import {
  checkDoneAdminOrderAPI,
  checkOutAdminOrderAPI,
  closeAdminOrderAPI,
  listAdminOrdersAPI,
} from "../service/adminOrders"
import { listAdminUsersAPI, updateAdminUserLockAPI } from "../service/adminUsers"
import {
  approveAdminAfterSaleAPI,
  listAdminAfterSalesAPI,
  rejectAdminAfterSaleAPI,
} from "../service/adminAfterSales"
import { getAdminAuth } from "../utils/adminAuth"

const router = useRouter()
const activeTab = ref("goods")
const refreshing = ref(false)

const adminAuth = ref(getAdminAuth())
const adminName = computed(() => adminAuth.value?.nickName || adminAuth.value?.userName || "admin")
const adminRoles = computed(() => (adminAuth.value?.roles || []).join(", ") || "N/A")

const dashboard = ref({
  goodsCount: 0,
  orderCount: 0,
  userCount: 0,
  afterSalePendingCount: 0,
})

const goodsState = reactive({
  loading: false,
  page: 1,
  pageSize: 10,
  total: 0,
  list: [],
})

const orderState = reactive({
  loading: false,
  page: 1,
  pageSize: 10,
  total: 0,
  orderNo: "",
  list: [],
})

const userState = reactive({
  loading: false,
  page: 1,
  pageSize: 10,
  total: 0,
  list: [],
})

const afterSaleState = reactive({
  loading: false,
  page: 1,
  pageSize: 10,
  total: 0,
  orderNo: "",
  afterSaleStatus: undefined,
  list: [],
})

const readPage = (data) => {
  const list = Array.isArray(data?.list) ? data.list : []
  return {
    list,
    total: Number(data?.totalCount || list.length || 0),
    page: Number(data?.currPage || data?.currentPage || 1),
  }
}

const assertSuccess = (res, fallbackMessage) => {
  if (res?.resultCode !== 200) {
    throw new Error(res?.message || fallbackMessage)
  }
  return res.data
}

const showError = (e, fallback) => {
  ElMessage.error(e?.message || fallback)
}

const loadDashboard = async () => {
  const data = assertSuccess(await getAdminDashboardAPI(), "Load dashboard failed")
  dashboard.value = {
    goodsCount: Number(data?.goodsCount || 0),
    orderCount: Number(data?.orderCount || 0),
    userCount: Number(data?.userCount || 0),
    afterSalePendingCount: Number(data?.afterSalePendingCount || 0),
  }
}

const loadGoods = async () => {
  goodsState.loading = true
  try {
    const data = assertSuccess(
      await listAdminGoodsAPI({ page: goodsState.page, pageSize: goodsState.pageSize }),
      "Load goods failed",
    )
    const page = readPage(data)
    goodsState.list = page.list
    goodsState.total = page.total
  } finally {
    goodsState.loading = false
  }
}

const loadOrders = async () => {
  orderState.loading = true
  try {
    const data = assertSuccess(
      await listAdminOrdersAPI({
        page: orderState.page,
        pageSize: orderState.pageSize,
        orderNo: orderState.orderNo.trim() || undefined,
      }),
      "Load orders failed",
    )
    const page = readPage(data)
    orderState.list = page.list
    orderState.total = page.total
  } finally {
    orderState.loading = false
  }
}

const loadUsers = async () => {
  userState.loading = true
  try {
    const data = assertSuccess(
      await listAdminUsersAPI({ page: userState.page, pageSize: userState.pageSize }),
      "Load users failed",
    )
    const page = readPage(data)
    userState.list = page.list
    userState.total = page.total
  } finally {
    userState.loading = false
  }
}

const loadAfterSales = async () => {
  afterSaleState.loading = true
  try {
    const data = assertSuccess(
      await listAdminAfterSalesAPI({
        page: afterSaleState.page,
        pageSize: afterSaleState.pageSize,
        orderNo: afterSaleState.orderNo.trim() || undefined,
        afterSaleStatus: afterSaleState.afterSaleStatus,
      }),
      "Load after-sales failed",
    )
    const page = readPage(data)
    afterSaleState.list = page.list
    afterSaleState.total = page.total
  } finally {
    afterSaleState.loading = false
  }
}

const refreshAll = async () => {
  refreshing.value = true
  try {
    await Promise.all([loadDashboard(), loadGoods(), loadOrders(), loadUsers(), loadAfterSales()])
  } catch (e) {
    showError(e, "Refresh failed")
  } finally {
    refreshing.value = false
  }
}

const updateGoodsStatus = async (goodsId, sellStatus) => {
  try {
    assertSuccess(await updateAdminGoodsStatusAPI({ goodsId, sellStatus }), "Update goods status failed")
    ElMessage.success("Goods status updated")
    await Promise.all([loadGoods(), loadDashboard()])
  } catch (e) {
    showError(e, "Update goods status failed")
  }
}

const orderAction = async (fn, orderId, successText) => {
  try {
    assertSuccess(await fn({ orderId }), "Order action failed")
    ElMessage.success(successText)
    await Promise.all([loadOrders(), loadDashboard()])
  } catch (e) {
    showError(e, "Order action failed")
  }
}

const toggleUserLock = async (userId, lockStatus) => {
  try {
    assertSuccess(await updateAdminUserLockAPI({ userId, lockStatus }), "Update user lock failed")
    ElMessage.success(lockStatus === 1 ? "User locked" : "User unlocked")
    await loadUsers()
  } catch (e) {
    showError(e, "Update user lock failed")
  }
}

const approveAfterSale = async (afterSaleId) => {
  try {
    assertSuccess(await approveAdminAfterSaleAPI({ afterSaleId }), "Approve failed")
    ElMessage.success("After-sale approved")
    await Promise.all([loadAfterSales(), loadDashboard()])
  } catch (e) {
    showError(e, "Approve failed")
  }
}

const rejectAfterSale = async (afterSaleId) => {
  try {
    const { value } = await ElMessageBox.prompt("Input reject reason", "Reject After-sale", {
      confirmButtonText: "Reject",
      cancelButtonText: "Cancel",
      inputPlaceholder: "reason",
    })
    assertSuccess(await rejectAdminAfterSaleAPI({ afterSaleId, rejectReason: value || "" }), "Reject failed")
    ElMessage.success("After-sale rejected")
    await Promise.all([loadAfterSales(), loadDashboard()])
  } catch (e) {
    if (e === "cancel") {
      return
    }
    showError(e, "Reject failed")
  }
}

const statusLabel = (status) => {
  if (status === 0) return "Pending"
  if (status === 1) return "Approved"
  if (status === 2) return "Refunding"
  if (status === 3) return "Refunded"
  if (status === -1) return "Rejected"
  return String(status ?? "")
}

const logout = async () => {
  adminLogout()
  await router.replace("/admin-login")
}

onMounted(async () => {
  await refreshAll()
})
</script>

<template>
  <main class="admin-console">
    <header class="console-header">
      <div>
        <h1>Admin Console</h1>
        <p>
          Operator: <strong>{{ adminName }}</strong> | Roles: {{ adminRoles }}
        </p>
      </div>
      <div class="actions">
        <el-button :loading="refreshing" @click="refreshAll">Refresh</el-button>
        <el-button type="danger" plain @click="logout">Logout</el-button>
      </div>
    </header>

    <section class="dashboard-grid">
      <article>
        <h3>{{ dashboard.goodsCount }}</h3>
        <p>Goods</p>
      </article>
      <article>
        <h3>{{ dashboard.orderCount }}</h3>
        <p>Orders</p>
      </article>
      <article>
        <h3>{{ dashboard.userCount }}</h3>
        <p>Users</p>
      </article>
      <article>
        <h3>{{ dashboard.afterSalePendingCount }}</h3>
        <p>After-sale Pending</p>
      </article>
    </section>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="Goods" name="goods">
        <el-table :data="goodsState.list" v-loading="goodsState.loading" border>
          <el-table-column prop="goodsId" label="ID" width="90" />
          <el-table-column prop="goodsName" label="Goods Name" min-width="220" />
          <el-table-column prop="sellingPrice" label="Price" width="110" />
          <el-table-column prop="stockNum" label="Stock" width="100" />
          <el-table-column prop="goodsSellStatus" label="Sell Status" width="120" />
          <el-table-column label="Actions" width="180">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="updateGoodsStatus(row.goodsId, 0)">Up</el-button>
              <el-button size="small" type="warning" @click="updateGoodsStatus(row.goodsId, 1)">Down</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Orders" name="orders">
        <div class="toolbar">
          <el-input v-model="orderState.orderNo" placeholder="Order No" clearable />
          <el-button type="primary" @click="loadOrders">Search</el-button>
        </div>
        <el-table :data="orderState.list" v-loading="orderState.loading" border>
          <el-table-column prop="orderId" label="ID" width="90" />
          <el-table-column prop="orderNo" label="Order No" min-width="190" />
          <el-table-column prop="orderStatus" label="Order Status" width="120" />
          <el-table-column prop="payStatus" label="Pay Status" width="100" />
          <el-table-column prop="refundStatus" label="Refund" width="90" />
          <el-table-column label="Actions" width="260">
            <template #default="{ row }">
              <el-button size="small" @click="orderAction(checkDoneAdminOrderAPI, row.orderId, 'Checked done')">
                Check Done
              </el-button>
              <el-button size="small" @click="orderAction(checkOutAdminOrderAPI, row.orderId, 'Checked out')">
                Check Out
              </el-button>
              <el-button size="small" type="danger" @click="orderAction(closeAdminOrderAPI, row.orderId, 'Closed')">
                Close
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Users" name="users">
        <el-table :data="userState.list" v-loading="userState.loading" border>
          <el-table-column prop="userId" label="ID" width="90" />
          <el-table-column prop="loginName" label="Login Name" min-width="180" />
          <el-table-column prop="nickName" label="Nick Name" min-width="160" />
          <el-table-column prop="lockedFlag" label="Locked" width="100" />
          <el-table-column label="Actions" width="180">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="toggleUserLock(row.userId, 1)">Lock</el-button>
              <el-button size="small" type="success" @click="toggleUserLock(row.userId, 0)">Unlock</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="After-sales" name="afterSales">
        <div class="toolbar">
          <el-input v-model="afterSaleState.orderNo" placeholder="Order No" clearable />
          <el-select v-model="afterSaleState.afterSaleStatus" placeholder="Status" clearable>
            <el-option :value="0" label="Pending" />
            <el-option :value="1" label="Approved" />
            <el-option :value="2" label="Refunding" />
            <el-option :value="3" label="Refunded" />
            <el-option :value="-1" label="Rejected" />
          </el-select>
          <el-button type="primary" @click="loadAfterSales">Search</el-button>
        </div>
        <el-table :data="afterSaleState.list" v-loading="afterSaleState.loading" border>
          <el-table-column prop="afterSaleId" label="ID" width="90" />
          <el-table-column prop="orderNo" label="Order No" min-width="180" />
          <el-table-column label="Status" width="130">
            <template #default="{ row }">{{ statusLabel(row.afterSaleStatus) }}</template>
          </el-table-column>
          <el-table-column prop="refundAmount" label="Refund Amount" width="130" />
          <el-table-column prop="reason" label="Reason" min-width="180" />
          <el-table-column prop="rejectReason" label="Reject Reason" min-width="180" />
          <el-table-column label="Actions" width="180">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="approveAfterSale(row.afterSaleId)">Approve</el-button>
              <el-button size="small" type="danger" @click="rejectAfterSale(row.afterSaleId)">Reject</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </main>
</template>

<style scoped lang="scss">
.admin-console {
  min-height: 100vh;
  padding: 20px;
  background: #f3f4f6;
}

.console-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;

  h1 {
    margin: 0 0 8px;
  }

  p {
    margin: 0;
    color: #4b5563;
  }
}

.actions {
  display: flex;
  gap: 10px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 16px;

  article {
    border-radius: 12px;
    background: #fff;
    padding: 12px 14px;
    border: 1px solid #e5e7eb;
  }

  h3 {
    margin: 0;
    font-size: 26px;
    color: #111827;
  }

  p {
    margin: 6px 0 0;
    color: #6b7280;
  }
}

.tabs {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;

  .el-input,
  .el-select {
    width: 200px;
  }
}
</style>
