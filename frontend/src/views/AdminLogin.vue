<script setup>
import { reactive, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { adminLoginAPI } from "../service/adminAuth"

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = reactive({
  userName: "admin",
  password: "123456",
})

const onSubmit = async () => {
  if (!form.userName.trim() || !form.password) {
    ElMessage.warning("Please input username and password")
    return
  }
  loading.value = true
  try {
    const res = await adminLoginAPI({ userName: form.userName.trim(), password: form.password })
    if (res?.resultCode !== 200 || !res?.data?.token) {
      ElMessage.error(res?.message || "Admin login failed")
      return
    }
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/admin-console"
    ElMessage.success("Admin login success")
    await router.replace(redirect)
  } catch (e) {
    ElMessage.error(e?.message || "Admin login failed")
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="admin-login">
    <section class="login-card">
      <h1>Admin Console Login</h1>
      <p>Use admin account to access management APIs.</p>
      <el-form @submit.prevent>
        <el-form-item label="Username">
          <el-input v-model="form.userName" autocomplete="username" placeholder="admin username" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="admin password"
            show-password
          />
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" :loading="loading" data-testid="admin-login-submit" @click="onSubmit">Login</el-button>
        <el-button @click="$router.push('/')">Back Home</el-button>
      </div>
    </section>
  </main>
</template>

<style scoped lang="scss">
.admin-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #111827, #1f2937 55%, #0f172a);
}

.login-card {
  width: min(96vw, 440px);
  border-radius: 14px;
  padding: 24px;
  background: rgb(255 255 255 / 95%);
  box-shadow: 0 14px 40px rgb(0 0 0 / 18%);

  h1 {
    margin: 0 0 8px;
    font-size: 24px;
  }

  p {
    margin: 0 0 18px;
    color: #4b5563;
  }
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
