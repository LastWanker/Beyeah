<script setup>
import headers from "../components/SimpleHeader.vue";
import { onMounted, reactive } from "vue";
import { useCartStore } from "../stores/cartStore";
import { getGoodDetail } from "../service/detail";
import { useRoute } from "vue-router";
import { handleGoodsImageError, normalizeGoodsImageUrl } from "@/utils/image";

const state = reactive({
    goodsId: 1,
    goodsName: "",
    goodsIntro: "",
    originalPrice: "0",
    sellingPrice: "0",
    goodsCoverImg: "",
});

const route = useRoute();
const getGoods = async () => {
    const res = await getGoodDetail(route.params.id);
    if (res.resultCode == "200" && res.data) {
        state.goodsId = res.data.goodsId;
        state.goodsName = res.data.goodsName;
        state.goodsIntro = res.data.goodsIntro;
        state.originalPrice = res.data.originalPrice;
        state.sellingPrice = res.data.sellingPrice;
        state.goodsCoverImg = res.data.goodsCoverImg;
    }
};

onMounted(() => getGoods());

const cartStore = useCartStore();
const addCart = async () => {
    const success = await cartStore.addCart({
        goodsId: state.goodsId,
        goodsName: state.goodsName,
        sellingPrice: state.sellingPrice,
        goodsCount: 1,
        goodsCoverImg: normalizeGoodsImageUrl(state.goodsCoverImg),
        selected: true,
    });
    if (success) {
        ElNotification({ title: "Success", message: "添加成功", type: "success" });
    } else {
        ElNotification({ title: "Error", message: "添加失败", type: "error" });
    }
};
</script>

<template>
    <headers />
    <div class="xtx-goods-page">
        <div class="container">
            <div class="goods-info">
                <div class="media">
                    <img :src="normalizeGoodsImageUrl(state.goodsCoverImg)" alt="" @error="handleGoodsImageError" />
                </div>
                <div class="spec">
                    <p class="g-name">{{ state.goodsName }}</p>
                    <p class="g-desc">{{ state.goodsIntro }}</p>
                    <p class="g-price">
                        <span>{{ state.sellingPrice }}</span>
                        <span>{{ state.originalPrice }}</span>
                    </p>
                    <el-button size="large" class="btn" data-testid="add-cart-btn" @click="addCart">加入购物车</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang='scss'>
.xtx-goods-page {
    .goods-info {
        min-height: 600px;
        background: linear-gradient(140deg, rgb(24 26 33 / 92%), rgb(33 25 31 / 88%));
        display: flex;
        border: 1px solid rgb(255 255 255 / 14%);
        border-radius: 12px;

        .media {
            width: 580px;
            height: 600px;
            padding: 30px 50px;

            img {
                max-width: 480px;
                max-height: 540px;
            }
        }

        .spec {
            flex: 1;
            padding: 30px 30px 30px 0;
        }
    }

    .g-name {
        font-size: 22px;
        color: #fff;
    }

    .g-desc {
        color: rgb(255 255 255 / 65%);
        margin-top: 10px;
    }

    .g-price {
        margin-top: 10px;

        span {
            &::before {
                content: "￥";
                font-size: 14px;
            }

            &:first-child {
                color: $priceColor;
                margin-right: 10px;
                font-size: 22px;
            }

            &:last-child {
                color: rgb(255 255 255 / 62%);
                text-decoration: line-through;
                font-size: 16px;
            }
        }
    }
}

.btn {
    margin-top: 20px;
}
</style>
