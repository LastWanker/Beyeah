<script setup>
import { computed, reactive } from "vue";
import { onMounted } from "vue";
import { saveUserInfoAPI, searchUserInfoAPI } from "../service/userInfo";
import { getOrderListAPI, payOrderAPI, searchOrderAPI } from "../service/order";
import { createAddressAPI, deleteAddressAPI, listAddressesAPI, setDefaultAddressAPI, updateAddressAPI } from "../service/address";
import { createAfterSaleAPI, listAfterSalesAPI } from "../service/afterSale";
import { useUserStore } from "../stores/user";
import headers from "../components/SimpleHeader.vue";

const userStore = useUserStore();
const state = reactive({
    nickname: "商城用户",
    username: "",
    activeTab: "personalInfo",
    isEditingName: false,
    inputText: "",
    orderList: [],
    activeOrderId: "",
    activeOrder: {
        orderStatus: 0,
        refundStatus: 0,
        payStatus: 0,
        payType: 0,
        createTime: "",
        totalPrice: 0,
        userAddress: "",
        orderItem: [],
    },
    afterSales: [],
    afterSaleFormVisible: false,
    afterSaleForm: {
        orderItemId: null,
        goodsName: "",
        refundAmount: "",
        reason: "",
        description: "",
    },
    addresses: [],
    addressFormVisible: false,
    addressForm: {
        addressId: null,
        consignee: "",
        phone: "",
        province: "",
        city: "",
        district: "",
        detail: "",
        isDefault: 0,
    },
});

const choosePersonalInfo = () => {
    state.activeTab = "personalInfo";
};

const chooseOrderManagement = () => {
    state.activeTab = "orderManagement";
};

const startEditingName = () => {
    state.isEditingName = !state.isEditingName;
    state.inputText = state.nickname;
};

onMounted(() => {
    searchUserInfo();
    loadAddresses();
    getOrderList();
});

const searchUserInfo = async () => {
    const res = await searchUserInfoAPI();
    if (res.resultCode == "200" && res.data) {
        state.nickname = res.data.nickname;
        state.username = res.data.username;
    }
};

const saveUserInfo = async () => {
    const res = await saveUserInfoAPI({
        nickname: state.inputText,
    });
    if (res.resultCode == "200") {
        state.nickname = state.inputText;
        state.isEditingName = false;
        ElNotification({ title: "Success", message: "昵称修改成功", type: "success" });
    } else {
        ElNotification({ title: "Error", message: "昵称修改失败", type: "error" });
    }
};

const loadAddresses = async () => {
    const res = await listAddressesAPI();
    if (res.resultCode == "200" && Array.isArray(res.data)) {
        state.addresses = res.data;
        return;
    }
    state.addresses = [];
};

const resetAddressForm = () => {
    state.addressForm = {
        addressId: null,
        consignee: "",
        phone: "",
        province: "",
        city: "",
        district: "",
        detail: "",
        isDefault: 0,
    };
};

const openCreateAddress = () => {
    resetAddressForm();
    state.addressFormVisible = true;
};

const openEditAddress = (address) => {
    state.addressForm = {
        addressId: address.addressId,
        consignee: address.consignee || "",
        phone: address.phone || "",
        province: address.province || "",
        city: address.city || "",
        district: address.district || "",
        detail: address.detail || "",
        isDefault: Number(address.isDefault) || 0,
    };
    state.addressFormVisible = true;
};

const closeAddressForm = () => {
    state.addressFormVisible = false;
    resetAddressForm();
};

const submitAddress = async () => {
    const payload = {
        consignee: state.addressForm.consignee?.trim(),
        phone: state.addressForm.phone?.trim(),
        province: state.addressForm.province?.trim(),
        city: state.addressForm.city?.trim(),
        district: state.addressForm.district?.trim(),
        detail: state.addressForm.detail?.trim(),
        isDefault: state.addressForm.isDefault ? 1 : 0,
    };

    if (!payload.consignee || !payload.phone || !payload.province || !payload.city || !payload.district || !payload.detail) {
        ElNotification({ title: "Error", message: "请完整填写地址信息", type: "error" });
        return;
    }

    let res;
    if (state.addressForm.addressId) {
        res = await updateAddressAPI({
            addressId: state.addressForm.addressId,
            ...payload,
        });
    } else {
        res = await createAddressAPI(payload);
    }

    if (res.resultCode == "200") {
        ElNotification({ title: "Success", message: "地址保存成功", type: "success" });
        closeAddressForm();
        await loadAddresses();
    } else {
        ElNotification({ title: "Error", message: res.message || "地址保存失败", type: "error" });
    }
};

const removeAddress = async (addressId) => {
    const confirmed = window.confirm("确认删除该地址吗？");
    if (!confirmed) {
        return;
    }
    const res = await deleteAddressAPI(addressId);
    if (res.resultCode == "200") {
        ElNotification({ title: "Success", message: "地址删除成功", type: "success" });
        await loadAddresses();
    } else {
        ElNotification({ title: "Error", message: res.message || "地址删除失败", type: "error" });
    }
};

const setDefaultAddress = async (addressId) => {
    const res = await setDefaultAddressAPI(addressId);
    if (res.resultCode == "200") {
        ElNotification({ title: "Success", message: "默认地址已更新", type: "success" });
        await loadAddresses();
    } else {
        ElNotification({ title: "Error", message: res.message || "设置默认地址失败", type: "error" });
    }
};

const addressText = (address) => {
    return [address.province, address.city, address.district, address.detail].filter(Boolean).join(" ");
};

const getOrderList = async () => {
    const res = await getOrderListAPI({ page: 1, pageSize: 20 });
    if (res.resultCode == "200") {
        const list = Array.isArray(res.data?.list) ? res.data.list : [];
        state.orderList = list.map((item) => {
            const firstItem = Array.isArray(item.beyeahOrderItemVOS) && item.beyeahOrderItemVOS.length > 0
                ? item.beyeahOrderItemVOS[0]
                : null;
            return {
                ...item,
                goodsName: firstItem ? firstItem.goodsName : item.orderNo,
            };
        });
    }
};

const orderStatus = computed(() => (status) => {
    const n = Number(status);
    if (n < 0) return "已取消";
    if (n === 0) return "待支付";
    if (n === 1) return "已支付";
    if (n === 2) return "已配货";
    if (n === 3) return "已出库";
    if (n === 4) return "已完成";
    return "处理中";
});

const payStatus = computed(() => (status) => Number(status) === 1 ? "已支付" : "未支付");

const payType = computed(() => (type) => {
    const n = Number(type);
    if (n === 1) return "微信支付";
    if (n === 2) return "支付宝支付";
    return "未支付";
});

const getOrderDetail = async (orderNo) => {
    state.activeOrderId = orderNo;
    state.activeTab = "orderDetail";
    const res = await searchOrderAPI({ orderNo });
    if (res.resultCode == "200" && res.data) {
        state.activeOrder = {
            ...res.data,
            orderItem: Array.isArray(res.data.beyeahOrderItemVOS) ? res.data.beyeahOrderItemVOS : [],
        };
        await loadAfterSales(orderNo);
    }
};

const loadAfterSales = async (orderNo) => {
    if (!orderNo) {
        state.afterSales = [];
        return;
    }
    const res = await listAfterSalesAPI({ page: 1, pageSize: 20, orderNo });
    if (res.resultCode == "200" && Array.isArray(res.data?.list)) {
        state.afterSales = res.data.list;
        return;
    }
    state.afterSales = [];
};

const openAfterSaleForm = (orderItem) => {
    if (!orderItem?.orderItemId) {
        ElNotification({ title: "Error", message: "当前订单项缺少标识，无法发起售后", type: "error" });
        return;
    }
    state.afterSaleForm = {
        orderItemId: orderItem.orderItemId,
        goodsName: orderItem.goodsName || "",
        refundAmount: Number(orderItem.sellingPrice || 0) * Number(orderItem.goodsCount || 0),
        reason: "",
        description: "",
    };
    state.afterSaleFormVisible = true;
};

const closeAfterSaleForm = () => {
    state.afterSaleFormVisible = false;
    state.afterSaleForm = {
        orderItemId: null,
        goodsName: "",
        refundAmount: "",
        reason: "",
        description: "",
    };
};

const canApplyAfterSale = (orderItem) => {
    if (!orderItem?.orderItemId) return false;
    if (Number(state.activeOrder.payStatus) !== 1) return false;
    return !state.afterSales.some((item) => Number(item.orderItemId) === Number(orderItem.orderItemId));
};

const submitAfterSale = async () => {
    if (!state.activeOrderId || !state.afterSaleForm.orderItemId) {
        ElNotification({ title: "Error", message: "售后参数无效", type: "error" });
        return;
    }
    const refundAmount = Number(state.afterSaleForm.refundAmount);
    if (!refundAmount || refundAmount <= 0) {
        ElNotification({ title: "Error", message: "退款金额不合法", type: "error" });
        return;
    }
    if (!String(state.afterSaleForm.reason || "").trim()) {
        ElNotification({ title: "Error", message: "请填写售后原因", type: "error" });
        return;
    }
    const res = await createAfterSaleAPI({
        orderNo: state.activeOrderId,
        orderItemId: state.afterSaleForm.orderItemId,
        refundAmount,
        reason: state.afterSaleForm.reason,
        description: state.afterSaleForm.description,
    });
    if (res.resultCode == "200") {
        ElNotification({ title: "Success", message: "售后申请已提交", type: "success" });
        closeAfterSaleForm();
        await Promise.all([getOrderDetail(state.activeOrderId), getOrderList()]);
        return;
    }
    ElNotification({ title: "Error", message: res.message || "售后申请失败", type: "error" });
};

const logout = () => {
    userStore.clearInfo();
};

const deleteOrder = () => {
    ElNotification({ title: "Info", message: "阶段一暂未实现取消订单", type: "info" });
};

const payOrder = async () => {
    if (!state.activeOrderId) {
        ElNotification({ title: "Error", message: "订单号无效", type: "error" });
        return;
    }
    if (Number(state.activeOrder.payStatus) === 1 || Number(state.activeOrder.orderStatus) !== 0) {
        ElNotification({ title: "Info", message: "当前订单无需支付", type: "info" });
        return;
    }

    try {
        const res = await payOrderAPI({ orderNo: state.activeOrderId, payType: 2 });
        if (res.resultCode == "200") {
            ElNotification({ title: "Success", message: "支付成功", type: "success" });
            await Promise.all([getOrderDetail(state.activeOrderId), getOrderList()]);
            return;
        }
        ElNotification({ title: "Error", message: res.message || "支付失败", type: "error" });
    } catch (e) {
        ElNotification({ title: "Error", message: e.message || "支付失败", type: "error" });
    }
};
</script>

<template>
    <headers />
    <div class="page">
        <aside class="sidebar">
            <button :class="{ active: state.activeTab === 'personalInfo' }" @click="choosePersonalInfo">个人信息</button>
            <button :class="{ active: state.activeTab === 'orderManagement' }" @click="chooseOrderManagement">订单管理</button>
            <router-link to="/login" @click="logout">退出登录</router-link>
        </aside>

        <main class="content" v-if="state.activeTab === 'personalInfo'">
            <h2>个人信息</h2>
            <div class="row">
                <span>昵称</span>
                <div class="value" v-if="!state.isEditingName">{{ state.nickname }}</div>
                <input v-else v-model="state.inputText" class="input" />
                <button @click="startEditingName">{{ state.isEditingName ? "取消" : "编辑" }}</button>
                <button v-if="state.isEditingName" @click="saveUserInfo">保存</button>
            </div>
            <div class="row">
                <span>手机号</span>
                <div class="value">{{ state.username }}</div>
            </div>

            <section class="address-section">
                <div class="address-section-head">
                    <h3>地址簿</h3>
                    <button @click="openCreateAddress">新增地址</button>
                </div>
                <div v-if="state.addresses.length === 0" class="address-empty">
                    暂无地址，请新增。
                </div>
                <div class="address-list" v-else>
                    <article class="address-card" v-for="address in state.addresses" :key="address.addressId">
                        <div class="address-title">
                            <strong>{{ address.consignee }}</strong>
                            <span>{{ address.phone }}</span>
                            <span class="default-tag" v-if="Number(address.isDefault) === 1">默认</span>
                        </div>
                        <p class="address-text">{{ addressText(address) }}</p>
                        <div class="address-actions">
                            <button @click="openEditAddress(address)">编辑</button>
                            <button v-if="Number(address.isDefault) !== 1" @click="setDefaultAddress(address.addressId)">设为默认</button>
                            <button @click="removeAddress(address.addressId)">删除</button>
                        </div>
                    </article>
                </div>

                <div class="address-form" v-if="state.addressFormVisible">
                    <h4>{{ state.addressForm.addressId ? "编辑地址" : "新增地址" }}</h4>
                    <div class="address-form-grid">
                        <input v-model="state.addressForm.consignee" placeholder="收货人" />
                        <input v-model="state.addressForm.phone" placeholder="手机号" />
                        <input v-model="state.addressForm.province" placeholder="省份" />
                        <input v-model="state.addressForm.city" placeholder="城市" />
                        <input v-model="state.addressForm.district" placeholder="区县" />
                        <input v-model="state.addressForm.detail" placeholder="详细地址" />
                    </div>
                    <label class="default-input">
                        <input type="checkbox" v-model="state.addressForm.isDefault" :true-value="1" :false-value="0" />
                        设为默认地址
                    </label>
                    <div class="address-form-actions">
                        <button @click="submitAddress">保存地址</button>
                        <button @click="closeAddressForm">取消</button>
                    </div>
                </div>
            </section>
        </main>

        <main class="content" v-else-if="state.activeTab === 'orderManagement'">
            <h2>订单管理</h2>
            <div class="order" v-for="item in state.orderList" :key="item.orderNo">
                <div>{{ item.goodsName }}</div>
                <div>订单号: {{ item.orderNo }}</div>
                <div>下单时间: {{ item.createTime }}</div>
                <div>金额: {{ item.totalPrice }}</div>
                <div>状态: {{ orderStatus(item.orderStatus) }}</div>
                <button @click="getOrderDetail(item.orderNo)">详情</button>
            </div>
        </main>

        <main class="content" v-else>
            <h2>订单详情</h2>
            <p>订单号: {{ state.activeOrderId }}</p>
            <p>下单时间: {{ state.activeOrder.createTime }}</p>
            <p>订单金额: {{ state.activeOrder.totalPrice }}</p>
            <p>订单状态: {{ orderStatus(state.activeOrder.orderStatus) }}</p>
            <p>退款状态: {{ state.activeOrder.refundStatusString || "无退款" }}</p>
            <p>支付状态: {{ payStatus(state.activeOrder.payStatus) }}</p>
            <p>支付方式: {{ payType(state.activeOrder.payType) }}</p>
            <p>收货地址: {{ state.activeOrder.userAddress || "-" }}</p>
            <div class="order-item" v-for="item in state.activeOrder.orderItem" :key="item.orderItemId || item.goodsId">
                <span>{{ item.goodsName }}</span>
                <span>{{ item.sellingPrice }} x {{ item.goodsCount }}</span>
                <button v-if="canApplyAfterSale(item)" @click="openAfterSaleForm(item)">申请售后</button>
            </div>
            <section class="after-sale-section">
                <h3>售后记录</h3>
                <p v-if="state.afterSales.length === 0" class="after-sale-empty">暂无售后记录</p>
                <div v-else class="after-sale-list">
                    <article class="after-sale-card" v-for="item in state.afterSales" :key="item.afterSaleNo">
                        <p>售后单号: {{ item.afterSaleNo }}</p>
                        <p>商品: {{ item.goodsName }}</p>
                        <p>退款金额: {{ item.refundAmount }}</p>
                        <p>售后状态: {{ item.afterSaleStatusString || item.afterSaleStatus }}</p>
                        <p>退款状态: {{ item.refundStatusString || "-" }}</p>
                        <p v-if="item.rejectReason">拒绝原因: {{ item.rejectReason }}</p>
                    </article>
                </div>
            </section>

            <div class="after-sale-form" v-if="state.afterSaleFormVisible">
                <h4>申请售后</h4>
                <p>商品: {{ state.afterSaleForm.goodsName }}</p>
                <div class="after-sale-form-grid">
                    <input v-model="state.afterSaleForm.refundAmount" type="number" min="1" placeholder="退款金额" />
                    <input v-model="state.afterSaleForm.reason" placeholder="售后原因" />
                    <textarea v-model="state.afterSaleForm.description" rows="3" placeholder="补充说明（可选）" />
                </div>
                <div class="after-sale-form-actions">
                    <button @click="submitAfterSale">提交申请</button>
                    <button @click="closeAfterSaleForm">取消</button>
                </div>
            </div>
            <div class="actions">
                <button @click="deleteOrder">取消订单</button>
                <button :disabled="Number(state.activeOrder.payStatus) === 1 || Number(state.activeOrder.orderStatus) !== 0" @click="payOrder">支付订单</button>
            </div>
        </main>
    </div>
</template>

<style scoped>
.page {
    display: flex;
    gap: 24px;
    padding: 20px 80px;
}

.sidebar {
    width: 180px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.sidebar button {
    height: 36px;
}

.sidebar .active {
    color: #006ce4;
}

.content {
    flex: 1;
    background: linear-gradient(140deg, rgb(24 26 33 / 92%), rgb(33 25 31 / 88%));
    padding: 20px;
    border: 1px solid rgb(255 255 255 / 14%);
    border-radius: 12px;
}

.row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
}

.row span {
    width: 60px;
}

.value {
    min-width: 200px;
    color: rgb(255 255 255 / 88%);
}

.input {
    min-width: 240px;
    height: 32px;
}

.address-section {
    margin-top: 24px;
    border-top: 1px solid rgb(255 255 255 / 12%);
    padding-top: 18px;
}

.address-section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}

.address-empty {
    color: rgb(255 255 255 / 62%);
}

.address-list {
    display: grid;
    gap: 10px;
}

.address-card {
    border: 1px solid rgb(255 255 255 / 16%);
    border-radius: 8px;
    padding: 12px;
    background: rgb(255 255 255 / 4%);
}

.address-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
}

.default-tag {
    display: inline-block;
    border-radius: 999px;
    background: #e8f5e9;
    color: #2e7d32;
    font-size: 12px;
    padding: 2px 8px;
}

.address-text {
    color: rgb(255 255 255 / 74%);
    margin-bottom: 8px;
}

.address-actions {
    display: flex;
    gap: 8px;
}

.address-form {
    margin-top: 14px;
    border: 1px dashed rgb(255 255 255 / 20%);
    border-radius: 8px;
    padding: 12px;
    background: rgb(255 255 255 / 4%);
}

.address-form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(220px, 1fr));
    gap: 10px;
}

.address-form-grid input {
    height: 34px;
    padding: 0 10px;
}

.default-input {
    display: inline-flex;
    gap: 8px;
    align-items: center;
    margin-top: 10px;
}

.address-form-actions {
    margin-top: 12px;
    display: flex;
    gap: 8px;
}

.order {
    border: 1px solid #e8e8e8;
    padding: 12px;
    margin-bottom: 12px;
}

.order-item {
    display: flex;
    justify-content: space-between;
    border-bottom: 1px dashed #ddd;
    padding: 8px 0;
}

.actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
}

.after-sale-section {
    margin-top: 18px;
    border-top: 1px solid rgb(255 255 255 / 12%);
    padding-top: 12px;
}

.after-sale-empty {
    color: rgb(255 255 255 / 62%);
}

.after-sale-list {
    display: grid;
    gap: 10px;
}

.after-sale-card {
    border: 1px solid rgb(255 255 255 / 16%);
    border-radius: 8px;
    padding: 10px;
    background: rgb(255 255 255 / 4%);
}

.after-sale-form {
    margin-top: 14px;
    border: 1px dashed rgb(255 255 255 / 24%);
    border-radius: 8px;
    padding: 12px;
    background: rgb(255 255 255 / 4%);
}

.after-sale-form-grid {
    display: grid;
    gap: 8px;
}

.after-sale-form-grid input,
.after-sale-form-grid textarea {
    padding: 8px 10px;
}

.after-sale-form-actions {
    margin-top: 10px;
    display: flex;
    gap: 8px;
}
</style>
