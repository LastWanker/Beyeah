<script setup>
import { handleGoodsImageError, normalizeGoodsImageUrl } from "@/utils/image";

defineProps({
    goods: {
        type: Object,
        default: () => ({}),
    },
});
</script>

<template>
    <RouterLink :to="`/product/${goods.goodsId}`" class="goods-item" data-testid="goods-item-link">
        <div class="thumb-wrap">
            <img :src="normalizeGoodsImageUrl(goods.goodsCoverImg)" alt="" @error="handleGoodsImageError" />
        </div>
        <p class="name ellipsis">{{ goods.goodsName }}</p>
        <p class="desc ellipsis">{{ goods.goodsIntro }}</p>
        <p class="price">&yen;{{ goods.sellingPrice }}</p>
    </RouterLink>
</template>

<style scoped lang="scss">
.goods-item {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    min-height: 392px;
    border-radius: 18px;
    padding: 16px 14px 18px;
    text-align: center;
    border: 1px solid rgb(255 255 255 / 10%);
    background:
        linear-gradient(145deg, rgb(18 19 24 / 86%), rgb(37 17 24 / 84%));
    transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;

    &:hover {
        transform: translateY(-4px);
        border-color: rgb(247 37 61 / 38%);
        box-shadow: 0 16px 24px rgb(0 0 0 / 30%);
    }

    .name {
        margin: 12px 0 0;
        font-size: 15px;
        line-height: 1.45;
        min-height: calc(1.45em * 2);
        color: #fff;
        letter-spacing: 0.02em;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .desc {
        margin: 8px 0 0;
        color: rgb(255 255 255 / 66%);
        font-size: 12px;
        line-height: 1.45;
        min-height: calc(1.45em * 2);
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .price {
        margin: auto 0 0;
        padding-top: 10px;
        color: $priceColor;
        font-size: 22px;
        font-weight: 600;
        letter-spacing: 0.03em;
    }
}

.thumb-wrap {
    width: 100%;
    border-radius: 14px;
    padding: 10px;
    background: rgb(255 255 255 / 5%);
    border: 1px solid rgb(255 255 255 / 8%);

    img {
        width: 100%;
        aspect-ratio: 1 / 1;
        object-fit: cover;
        border-radius: 10px;
        display: block;
    }
}
</style>
