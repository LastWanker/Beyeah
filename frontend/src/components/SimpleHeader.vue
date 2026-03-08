<script setup>
import { computed, ref } from "vue"
import { Search } from "@element-plus/icons-vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/user"

const userStore = useUserStore()
const router = useRouter()
const keyword = ref("")

const isLoggedIn = computed(() => userStore.id > 0 && !!userStore.token)

const goSearch = () => {
  const term = keyword.value.trim()
  if (!term) {
    router.push("/")
    return
  }
  router.push(`/search/${encodeURIComponent(term)}`)
}
</script>

<template>
  <header class="lux-header">
    <div class="container header-inner">
      <router-link to="/" class="brand" aria-label="Beyeah Home">
        <img src="@/assets/logo.png" alt="Beyeah logo" />
      </router-link>

      <form class="center-search" @submit.prevent="goSearch">
        <el-icon class="search-icon">
          <Search />
        </el-icon>
        <input v-model="keyword" type="text" placeholder="Search phones, beauty, accessories" aria-label="Search goods" />
        <button type="submit">Search</button>
      </form>

      <nav class="quick-nav">
        <div class="menu-group">
          <button class="menu-trigger" type="button">Phones</button>
          <div class="menu-panel">
            <router-link :to="`/category/${45}`">Honor</router-link>
            <router-link :to="`/category/${46}`">Huawei</router-link>
            <router-link :to="`/category/${47}`">iPhone</router-link>
            <router-link :to="`/category/${51}`">Xiaomi</router-link>
          </div>
        </div>

        <div class="menu-group">
          <button class="menu-trigger" type="button">Beauty</button>
          <div class="menu-panel">
            <router-link :to="`/category/${86}`">Lipstick</router-link>
          </div>
        </div>

        <router-link to="/cart" class="quick-link">Cart</router-link>
        <router-link to="/admin-login" class="quick-link">Admin</router-link>
        <router-link :to="isLoggedIn ? '/info' : '/login'" class="quick-link">
          {{ isLoggedIn ? "Profile" : "Login" }}
        </router-link>
      </nav>
    </div>
  </header>
</template>

<style scoped lang="scss">
.lux-header {
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid var(--lux-line);
  background: linear-gradient(100deg, rgb(10 10 12 / 95%), rgb(17 17 22 / 94%) 65%, rgb(78 10 18 / 86%));
  box-shadow: 0 8px 26px rgb(0 0 0 / 35%);

  &::after {
    content: "";
    display: block;
    height: 2px;
    background: linear-gradient(90deg, transparent, #b10f1a 20%, #f7253d 50%, #b10f1a 80%, transparent);
  }
}

.header-inner {
  display: grid;
  grid-template-columns: 220px minmax(380px, 1fr) auto;
  align-items: center;
  gap: 22px;
  min-height: 82px;
}

.brand {
  display: inline-block;
  line-height: 0;

  img {
    width: auto;
    height: 52px;
    max-width: 180px;
    object-fit: contain;
    display: block;
  }
}

.center-search {
  width: min(100%, 760px);
  justify-self: center;
  height: 52px;
  border-radius: 30px;
  border: 1px solid rgb(255 255 255 / 22%);
  background: linear-gradient(130deg, rgb(18 18 21 / 96%), rgb(50 15 20 / 84%));
  display: flex;
  align-items: center;
  padding: 0 8px 0 14px;
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 7%);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:focus-within {
    border-color: rgb(247 37 61 / 70%);
    box-shadow: inset 0 0 0 1px rgb(247 37 61 / 28%), 0 12px 28px rgb(193 18 31 / 25%);
  }

  .search-icon {
    color: rgb(255 255 255 / 76%);
    margin-right: 10px;
    font-size: 18px;
  }

  input {
    flex: 1;
    min-width: 0;
    border: 0;
    outline: none;
    color: #fff;
    font-size: 15px;
    background: transparent;
    font-family: "Noto Serif SC", "Source Han Serif SC", serif;
  }

  input::placeholder {
    color: rgb(255 255 255 / 48%);
    letter-spacing: 0.04em;
  }

  button {
    height: 40px;
    border: 0;
    border-radius: 22px;
    padding: 0 22px;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    background: linear-gradient(135deg, #b10f1a, #f7253d);
    font-family: "Noto Serif SC", "Source Han Serif SC", serif;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  button:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 16px rgb(247 37 61 / 34%);
  }
}

.quick-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-self: end;
}

.menu-group {
  position: relative;
}

/* bridge zone to prevent hover drop when moving from trigger to menu panel */
.menu-group::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: 100%;
  height: 14px;
}

.menu-trigger,
.quick-link {
  border: 1px solid rgb(255 255 255 / 16%);
  color: #fff;
  background: rgb(255 255 255 / 2%);
  border-radius: 999px;
  padding: 9px 14px;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: "Noto Serif SC", "Source Han Serif SC", serif;
}

.menu-trigger:hover,
.quick-link:hover {
  border-color: rgb(247 37 61 / 70%);
  color: #ffd5da;
  background: rgb(247 37 61 / 16%);
}

.menu-panel {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  min-width: 148px;
  display: none;
  padding: 10px 8px;
  border-radius: 14px;
  border: 1px solid rgb(255 255 255 / 22%);
  background: var(--lux-glass-bg);
  backdrop-filter: blur(14px) saturate(130%);
  box-shadow: 0 14px 26px rgb(0 0 0 / 28%);

  a {
    display: block;
    color: #fff;
    padding: 8px 10px;
    border-radius: 9px;
    font-size: 13px;
    white-space: nowrap;
    transition: background-color 0.2s ease;
  }

  a:hover {
    background: rgb(255 255 255 / 10%);
  }
}

.menu-group:hover .menu-panel,
.menu-group:focus-within .menu-panel {
  display: block;
}

@media (max-width: 1080px) {
  .header-inner {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px 0 14px;
  }

  .brand {
    justify-self: center;
  }

  .center-search {
    width: 100%;
  }

  .quick-nav {
    justify-self: center;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .center-search {
    height: 48px;
  }

  .center-search button {
    padding: 0 16px;
  }

  .menu-panel {
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
