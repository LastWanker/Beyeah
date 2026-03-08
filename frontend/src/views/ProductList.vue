<script setup>
import headers from "../components/SimpleHeader.vue";
import goodsitem from "../components/GoodsItem.vue";
import { onMounted, reactive, ref, watch } from "vue";
import { getGoodsByCateAPI } from "../service/goods";
import { useRoute } from "vue-router";

const route = useRoute();
const goodsList = ref([]);
const reqData = reactive({
    categoryId: route.params.id,
    sortField: "createTime",
});

const getGoodsList = async () => {
    const res = await getGoodsByCateAPI(reqData);
    if (res.resultCode == "200") {
        goodsList.value = Array.isArray(res.data?.list) ? res.data.list : [];
    }
};

const tabChange = () => {
    getGoodsList();
};

watch(
    () => route.params.id,
    (id) => {
        reqData.categoryId = id;
        getGoodsList();
    },
);

onMounted(() => getGoodsList());
</script>

<template>
    <headers />
    <div class="container list-page">
        <div class="sub-container">
            <el-tabs v-model="reqData.sortField" @tab-change="tabChange">
                <el-tab-pane label="新品" name="createTime"></el-tab-pane>
                <el-tab-pane label="价格" name="sellingPrice"></el-tab-pane>
            </el-tabs>
            <div class="body">
                <goodsitem v-for="goods in goodsList" :goods="goods" :key="goods.goodsId" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.list-page {
    margin-top: 16px;
}

.sub-container {
    padding: 18px 14px 22px;
    border-radius: 18px;
    border: 1px solid rgb(255 255 255 / 12%);
    background: linear-gradient(140deg, rgb(17 18 23 / 92%), rgb(30 18 24 / 88%));

    .body {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
        gap: 14px;
    }

    :deep(.el-tabs__item) {
        color: rgb(255 255 255 / 64%);
    }

    :deep(.el-tabs__item.is-active) {
        color: #fff;
    }

    :deep(.el-tabs__active-bar) {
        background-color: $priceColor;
    }

    :deep(.el-tabs__nav-wrap::after) {
        background-color: rgb(255 255 255 / 12%);
    }
}
</style>
