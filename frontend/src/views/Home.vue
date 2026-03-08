<template>
    <headers />

    <main class="home-page">
        <section class="hero-shell container">
            <div class="hero-frame">
                <carousels />
                <span class="deco deco-left"></span>
                <span class="deco deco-right"></span>
            </div>
        </section>

        <section class="panel-shell container">
            <homepanel title="新品上线" sub-title="新鲜出炉 品质靠谱">
                <ul class="goods-list">
                    <li v-for="item in newList" :key="item.goodsId">
                        <goodsitem :goods="item" />
                    </li>
                </ul>
            </homepanel>

            <homepanel title="为你推荐" sub-title="人气推荐 好多商品">
                <ul class="goods-list">
                    <li v-for="item in hotList" :key="item.goodsId">
                        <goodsitem :goods="item" />
                    </li>
                </ul>
            </homepanel>
        </section>
    </main>
</template>

<script setup>
import headers from "../components/SimpleHeader.vue";
import carousels from "../components/Carousel.vue";
import homepanel from "../components/HomePanel.vue";
import goodsitem from "../components/GoodsItem.vue";
import { getNewAPI, getHotAPI } from "../service/home";
import { onMounted, ref } from "vue";

const newList = ref([]);
const hotList = ref([]);

const getNewList = async () => {
    const res = await getNewAPI();
    newList.value = Array.isArray(res?.data) ? res.data : [];
};

const getHotList = async () => {
    const res = await getHotAPI();
    hotList.value = Array.isArray(res?.data) ? res.data : [];
};

onMounted(() => {
    getNewList();
    getHotList();
});
</script>

<style scoped lang="scss">
.home-page {
    padding: 14px 0 30px;
}

.hero-shell {
    margin-top: 6px;
}

.hero-frame {
    position: relative;
}

.deco {
    position: absolute;
    border-radius: 999px;
    filter: blur(0.5px);
    pointer-events: none;
}

.deco-left {
    width: 140px;
    height: 140px;
    left: -24px;
    top: 34px;
    border: 1px solid rgb(247 37 61 / 38%);
    background: radial-gradient(circle, rgb(247 37 61 / 18%), transparent 62%);
}

.deco-right {
    width: 190px;
    height: 190px;
    right: -28px;
    bottom: -20px;
    border: 1px solid rgb(255 255 255 / 16%);
    background: radial-gradient(circle, rgb(255 255 255 / 13%), transparent 70%);
}

.panel-shell {
    margin-top: 24px;
    display: grid;
    gap: 26px;
}

.goods-list {
    margin: 0;
    padding: 0;
    list-style: none;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;

    li {
        min-width: 0;
    }
}

@media (max-width: 768px) {
    .deco-left,
    .deco-right {
        display: none;
    }
}
</style>
