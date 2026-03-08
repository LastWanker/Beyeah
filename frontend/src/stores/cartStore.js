import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useUserStore } from './user'
import { insertCartAPI, getCartListAPI, delCartAPI } from '../service/cart'

export const useCartStore = defineStore('cart', () => {
    const userStore = useUserStore()
    const cartList = ref([])

    const getCartList = async () => {
        if (!userStore.isLoggedIn) return
        const res = await getCartListAPI()
        if (res.resultCode == "200") {
            const list = Array.isArray(res.data) ? res.data : []
            cartList.value = list.map((item) => ({
                cartItemId: item.cartItemId,
                goodsId: item.goodsId,
                goodsName: item.goodsName,
                sellingPrice: item.sellingPrice,
                goodsCount: item.goodsCount,
                goodsCoverImg: item.goodsCoverImg,
                selected: false,
            }))
        }
    }

    const addCart = async (goods) => {
        const { goodsId, goodsCount } = goods
        if (userStore.isLoggedIn) {
            const res = await insertCartAPI({ goodsId, goodsCount })
            if (res.resultCode == "200") {
                await getCartList()
                return true
            }
            return false
        }

        const item = cartList.value.find((it) => goods.goodsId === it.goodsId)
        if (item) {
            item.goodsCount++
        } else {
            cartList.value.push(goods)
        }
        return true
    }

    const delCart = async (payload) => {
        if (userStore.isLoggedIn) {
            const cartItemId = typeof payload === 'object' ? payload.cartItemId : payload
            if (!cartItemId) return
            await delCartAPI({ cartItemId })
            await getCartList()
            return
        }

        const goodsId = typeof payload === 'object' ? payload.goodsId : payload
        const idx = cartList.value.findIndex((item) => goodsId === item.goodsId)
        if (idx >= 0) {
            cartList.value.splice(idx, 1)
        }
    }

    const singleCheck = (goodsId, selected) => {
        const item = cartList.value.find((it) => it.goodsId === goodsId)
        if (item) {
            item.selected = selected
        }
    }

    const allCheck = (selected) => {
        cartList.value.forEach(item => {
            item.selected = selected
        })
    }

    const cleanCart = () => {
        cartList.value = []
    }

    const allCount = computed(() => cartList.value.reduce((a, c) => a + c.goodsCount, 0))
    const allPrice = computed(() => cartList.value.reduce((a, c) => a + c.goodsCount * c.sellingPrice, 0))
    const selectedCount = computed(() => cartList.value.filter(item => item.selected).reduce((a, c) => a + c.goodsCount, 0))
    const selectedPrice = computed(() => cartList.value.filter(item => item.selected).reduce((a, c) => a + c.goodsCount * c.sellingPrice, 0))
    const isAll = computed(() => cartList.value.length > 0 && cartList.value.every((item) => item.selected))

    return {
        cartList,
        allCount,
        allPrice,
        isAll,
        selectedCount,
        selectedPrice,
        getCartList,
        singleCheck,
        allCheck,
        addCart,
        delCart,
        cleanCart
    }
}, {
    persist: false,
})
