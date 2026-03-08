import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useCartStore } from './cartStore'

export const useUserStore = defineStore('user', () => {
    const id = ref(0)
    const token = ref('')
    const cartStore = useCartStore()

    const getId = (userid) => {
        id.value = Number(userid) || 0
    }

    const setToken = (value) => {
        token.value = value || ''
    }

    const setAuth = ({ userId, userid, token: authToken }) => {
        id.value = Number(userId || userid) || 0
        token.value = authToken || ''
    }

    const clearInfo = () => {
        id.value = 0
        token.value = ''
        cartStore.cleanCart()
    }

    const isLoggedIn = computed(() => id.value > 0 && !!token.value)

    return {
        id,
        token,
        isLoggedIn,
        getId,
        setToken,
        setAuth,
        clearInfo
    }
}, {
    persist: true,
})
