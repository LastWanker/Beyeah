<script setup>
import { reactive, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import vueImgVerify from "../components/VueImageVerify.vue"
import { loginAPI, regAPI } from "../service/login_reg"
import { useUserStore } from "../stores/user"

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const verifyRef = ref(null)

const state = reactive({
  username_l: "",
  password_l: "",
  username_r: "",
  password_r: "",
  type: "reg",
  imgCode: "",
  verify: "",
})

const toggle = (type) => {
  state.type = type
  state.username_l = ""
  state.password_l = ""
  state.username_r = ""
  state.password_r = ""
  state.verify = ""
}

const onSubmit = async () => {
  state.imgCode = verifyRef.value?.state?.imgCode || ""
  if (!state.verify || state.verify.toLowerCase() !== state.imgCode.toLowerCase()) {
    ElNotification({ title: "Error", message: "验证码错误", type: "error" })
    return
  }

  if (state.type === "login") {
    try {
      const res = await loginAPI({ username: state.username_l, password: state.password_l })
      if (res.resultCode == 200 && res.data) {
        userStore.setAuth({
          userId: res.data.userId,
          userid: res.data.userid,
          token: res.data.token,
        })
        ElNotification({ title: "Success", message: "登录成功", type: "success" })
        const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/"
        router.replace(redirect)
        return
      }
      ElNotification({ title: "Error", message: res.message || "登录失败", type: "error" })
    } catch (e) {
      ElNotification({ title: "Error", message: e.message || "登录失败", type: "error" })
    }
    return
  }

  try {
    const res = await regAPI({ username: state.username_r, password: state.password_r })
    if (res.resultCode == 200) {
      ElNotification({ title: "Success", message: "注册成功", type: "success" })
      state.type = "login"
      state.verify = ""
      return
    }
    ElNotification({ title: "Error", message: res.message || "注册失败", type: "error" })
  } catch (e) {
    ElNotification({ title: "Error", message: e.message || "注册失败", type: "error" })
  }
}
</script>

<template>
  <div class="back">
    <div class="login-brand">
      <img src="@/assets/logo.png" alt="Beyeah logo" />
    </div>
    <section class="login-section">
      <div v-if="state.type === 'reg'" class="wrapper">
        <div class="top_part">
          <div class="text_1">用户注册</div>
          <div class="text_2">已有账号？</div>
          <a href="javascript:;" @click="toggle('login')">去登录</a>
        </div>
        <div class="account-box">
          <div class="form">
            <el-form label-position="right" label-width="80px">
              <el-form-item label="手机号：">
                <el-input v-model="state.username_r" placeholder="请输入手机号" maxlength="32" />
              </el-form-item>
              <el-form-item label="密码：">
                <el-input v-model="state.password_r" placeholder="请输入密码" type="password" maxlength="32" />
              </el-form-item>
              <el-form-item label="验证码：" style="margin-bottom: 15px">
                <el-row>
                  <el-col :span="12">
                    <el-input v-model="state.verify" placeholder="请输入验证码" maxlength="4" />
                  </el-col>
                  <el-col :span="12">
                    <div style="display: flex; justify-content: flex-end">
                      <vueImgVerify ref="verifyRef" />
                    </div>
                  </el-col>
                </el-row>
              </el-form-item>
              <el-row>
                <el-button size="large" class="subBtn" @click="onSubmit">立即注册</el-button>
              </el-row>
              <el-row>
                <el-button size="large" class="subBtn" @click="$router.push('/')">回到首页</el-button>
              </el-row>
            </el-form>
          </div>
        </div>
      </div>

      <div v-else class="wrapper">
        <div class="top_part">
          <div class="text_1">用户登录</div>
          <div class="text_2">还没有账号？</div>
          <a href="javascript:;" @click="toggle('reg')">去注册</a>
        </div>
        <div class="account-box">
          <div class="form">
            <el-form label-position="right" label-width="80px">
              <el-form-item label="手机号：">
                <el-input v-model="state.username_l" placeholder="请输入手机号" maxlength="32" />
              </el-form-item>
              <el-form-item label="密码：">
                <el-input v-model="state.password_l" placeholder="请输入密码" type="password" maxlength="32" />
              </el-form-item>
              <el-form-item label="验证码：" style="margin-bottom: 15px">
                <el-row>
                  <el-col :span="12">
                    <el-input v-model="state.verify" placeholder="请输入验证码" maxlength="4" />
                  </el-col>
                  <el-col :span="12">
                    <div style="display: flex; justify-content: flex-end">
                      <vueImgVerify ref="verifyRef" />
                    </div>
                  </el-col>
                </el-row>
              </el-form-item>
              <el-row>
                <el-button size="large" class="subBtn" @click="onSubmit">立即登录</el-button>
              </el-row>
              <el-row>
                <el-button size="large" class="subBtn" @click="$router.push('/')">回到首页</el-button>
              </el-row>
            </el-form>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.back {
  background:
    radial-gradient(circle at 15% 15%, rgb(193 18 31 / 22%), transparent 35%),
    radial-gradient(circle at 85% 5%, rgb(247 37 61 / 14%), transparent 30%),
    linear-gradient(165deg, #08090d 0%, #11131a 60%, #181720 100%);
  min-height: 100vh;
  padding: 36px 0;
}

.login-brand {
  display: flex;
  justify-content: center;

  img {
    width: 180px;
    height: auto;
    filter: drop-shadow(0 12px 20px rgb(0 0 0 / 45%));
  }
}

.login-section {
  min-height: 560px;
  position: relative;

  .text_1 {
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    color: #fff;
  }

  .text_2 {
    font-size: 14px;
    text-align: right;
    color: rgb(255 255 255 / 70%);
  }

  .wrapper {
    width: 420px;
    background: linear-gradient(135deg, rgb(20 22 30 / 94%), rgb(34 24 31 / 90%));
    border: 1px solid rgb(255 255 255 / 18%);
    border-radius: 14px;
    position: absolute;
    left: 50%;
    top: 90px;
    transform: translateX(-50%);
    box-shadow: 0 16px 34px rgb(0 0 0 / 34%);

    .top_part {
      font-size: 14px;
      height: 55px;
      margin-bottom: 20px;
      border-bottom: 1px solid rgb(255 255 255 / 14%);
      display: flex;
      justify-content: space-around;
      align-items: center;

      div {
        width: 150px;
      }

      a {
        font-size: 14px;
        color: #f77480;
      }
    }
  }
}

.account-box {
  .form {
    padding: 0 30px 20px 20px;

    .el-form-item {
      margin-bottom: 30px;
    }
  }

  :deep(.el-form-item__label) {
    color: rgb(255 255 255 / 86%);
  }

  :deep(.el-input__wrapper) {
    background: rgb(255 255 255 / 10%);
    box-shadow: inset 0 0 0 1px rgb(255 255 255 / 16%);
  }

  :deep(.el-input__inner) {
    color: #fff;
  }

  :deep(.el-input__inner::placeholder) {
    color: rgb(255 255 255 / 44%);
  }
}

.subBtn {
  background: $btnColor;
  width: 100%;
  color: #fff;
  margin-top: 10px;
}
</style>
