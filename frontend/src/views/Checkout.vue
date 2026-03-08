<script setup>
import headers from "../components/SimpleHeader.vue";
import { useCartStore } from "../stores/cartStore";
import { searchUserInfoAPI } from "../service/userInfo";
import { createOrderAPI } from "../service/order";
import { listAddressesAPI } from "../service/address";
import { computed, onMounted, reactive, ref } from "vue";
import { handleGoodsImageError, normalizeGoodsImageUrl } from "@/utils/image";

const checkInfo = ref([]);
const cartStore = useCartStore();
const getCheckInfo = () => {
    checkInfo.value = cartStore.cartList.filter(item => item.selected).map((item) => ({
        goodsId: item.goodsId,
        goodsName: item.goodsName,
        sellingPrice: item.sellingPrice,
        goodsCount: item.goodsCount,
        goodsCoverImg: item.goodsCoverImg,
    }));
};

const userInfo = reactive({
    nickname: "商城用户",
    username: "",
});

const addresses = ref([]);
const selectedAddressId = ref(null);

const selectedAddress = computed(() => {
    return addresses.value.find(item => Number(item.addressId) === Number(selectedAddressId.value)) || null;
});

const formatAddress = (address) => {
    if (!address) return "";
    return [address.province, address.city, address.district, address.detail]
        .filter(Boolean)
        .join(" ")
        .trim();
};

const searchUserInfo = async () => {
    const res = await searchUserInfoAPI();
    if (res.resultCode == "200" && res.data) {
        userInfo.nickname = res.data.nickname;
        userInfo.username = res.data.username;
    }
};

const loadAddresses = async () => {
    const res = await listAddressesAPI();
    if (res.resultCode == "200" && Array.isArray(res.data)) {
        addresses.value = res.data;
        const defaultAddress = res.data.find(item => Number(item.isDefault) === 1);
        const firstAddress = defaultAddress || res.data[0];
        selectedAddressId.value = firstAddress ? firstAddress.addressId : null;
        return;
    }
    addresses.value = [];
    selectedAddressId.value = null;
};

const postfee = ref(4);
const totalPayPrice = computed(() => postfee.value + cartStore.selectedPrice);
const submitting = ref(false);
const idempotencyKey = ref(generateIdempotencyKey());

function generateIdempotencyKey() {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `ck_${Date.now()}_${Math.random().toString(16).slice(2, 10)}`;
}

const createOrder = async () => {
    if (submitting.value) {
        return;
    }
    getCheckInfo();
    if (checkInfo.value.length === 0) {
        ElNotification({ title: "Error", message: "请先选择商品", type: "error" });
        return;
    }
    if (!selectedAddress.value) {
        ElNotification({ title: "Error", message: "请先选择收货地址", type: "error" });
        return;
    }
    submitting.value = true;
    try {
        const res = await createOrderAPI({
            addressId: selectedAddress.value.addressId,
            address: formatAddress(selectedAddress.value),
            goods: checkInfo.value.map((item) => ({ goodsId: item.goodsId, goodsCount: item.goodsCount })),
            idempotencyKey: idempotencyKey.value,
        });

        if (res.resultCode == "200") {
            await cartStore.getCartList();
            idempotencyKey.value = generateIdempotencyKey();
            ElNotification({ title: "Success", message: "下单成功", type: "success" });
            return;
        }
        ElNotification({ title: "Error", message: res.message || "下单失败", type: "error" });
    } catch (e) {
        ElNotification({ title: "Error", message: e.message || "下单失败", type: "error" });
    } finally {
        submitting.value = false;
    }
};

onMounted(async () => {
    await Promise.all([searchUserInfo(), loadAddresses()]);
    getCheckInfo();
});
</script>

<template>
    <headers />
    <div class="xtx-pay-checkout-page">
        <div class="container">
            <div class="wrapper">
                <h3 class="box-title">收货信息</h3>
                <div class="box-body">
                    <div v-if="addresses.length > 0">
                        <el-radio-group v-model="selectedAddressId" class="address-list">
                            <el-radio
                                v-for="item in addresses"
                                :key="item.addressId"
                                :label="item.addressId"
                                class="address-radio"
                            >
                                <div class="address-item">
                                    <p class="address-head">
                                        <strong>{{ item.consignee }}</strong>
                                        <span>{{ item.phone }}</span>
                                        <el-tag v-if="Number(item.isDefault) === 1" size="small" type="success">默认</el-tag>
                                    </p>
                                    <p class="address-text">{{ formatAddress(item) }}</p>
                                </div>
                            </el-radio>
                        </el-radio-group>
                    </div>
                    <div v-else class="address-empty">
                        <p>暂无可用收货地址，请先在个人中心新增地址。</p>
                        <router-link to="/info">去新增地址</router-link>
                    </div>
                    <p class="contact-line">账号：{{ userInfo.nickname }} / {{ userInfo.username }}</p>
                </div>

                <h3 class="box-title">商品信息</h3>
                <div class="box-body">
                    <table class="goods">
                        <thead>
                            <tr>
                                <th width="520">商品信息</th>
                                <th width="170">单价</th>
                                <th width="170">数量</th>
                                <th width="170">小计</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="i in checkInfo" :key="i.goodsId">
                                <td>
                                    <div class="info">
                                        <img :src="normalizeGoodsImageUrl(i.goodsCoverImg)" alt="" @error="handleGoodsImageError">
                                        <div class="right">
                                            <p>{{ i.goodsName }}</p>
                                        </div>
                                    </div>
                                </td>
                                <td>&yen;{{ i.sellingPrice }}</td>
                                <td>{{ i.goodsCount }}</td>
                                <td>&yen;{{ i.sellingPrice * i.goodsCount }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <h3 class="box-title">金额明细</h3>
                <div class="box-body total">
                    <p>商品件数：{{ cartStore.selectedCount }}</p>
                    <p>商品总价：&yen;{{ cartStore.selectedPrice.toFixed(2) }}</p>
                    <p>运费：&yen;{{ postfee.toFixed(2) }}</p>
                    <p class="price">应付总额：&yen;{{ totalPayPrice.toFixed(2) }}</p>
                </div>

                <div class="submit">
                    <el-button type="primary" size="large" data-testid="submit-order-btn" :loading="submitting" :disabled="submitting" @click="createOrder">提交订单</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.xtx-pay-checkout-page {
    margin-top: 20px;

    .wrapper {
        background: linear-gradient(140deg, rgb(24 26 33 / 92%), rgb(33 25 31 / 88%));
        padding: 0 20px;
        border: 1px solid rgb(255 255 255 / 14%);
        border-radius: 12px;

        .box-title {
            font-size: 16px;
            font-weight: normal;
            line-height: 60px;
            border-bottom: 1px solid rgb(255 255 255 / 12%);
            color: rgb(255 255 255 / 92%);
        }

        .box-body {
            padding: 20px 0;
        }
    }
}

.address-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
}

.address-radio {
    margin-right: 0;

    :deep(.el-radio__label) {
        width: 100%;
    }
}

.address-item {
    border: 1px solid rgb(255 255 255 / 18%);
    border-radius: 8px;
    padding: 10px 12px;
    background: rgb(255 255 255 / 4%);
}

.address-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;
}

.address-text {
    color: rgb(255 255 255 / 74%);
}

.address-empty {
    color: rgb(255 255 255 / 72%);
    line-height: 26px;
}

.contact-line {
    margin-top: 12px;
    color: rgb(255 255 255 / 64%);
}

.goods {
    width: 100%;
    border-collapse: collapse;
    border-spacing: 0;

    .info {
        display: flex;
        text-align: left;

        img {
            width: 70px;
            height: 70px;
            margin-right: 20px;
        }
    }

    tr {
        th {
            background: rgb(255 255 255 / 8%);
            font-weight: normal;
        }

        td,
        th {
            text-align: center;
            padding: 20px;
            border-bottom: 1px solid rgb(255 255 255 / 12%);
        }
    }
}

.total {
    p {
        line-height: 36px;
    }

    .price {
        font-size: 20px;
        color: $priceColor;
    }
}

.submit {
    text-align: right;
    padding: 30px 0 60px;
}
</style>
