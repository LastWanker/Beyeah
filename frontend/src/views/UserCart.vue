<script setup>
import { useCartStore } from "@/stores/cartStore.js";
import headers from "../components/SimpleHeader.vue";
import { onMounted } from "vue";
import { handleGoodsImageError, normalizeGoodsImageUrl } from "@/utils/image";

const cartStore = useCartStore();

const singleCheck = (item, selected) => {
    cartStore.singleCheck(item.goodsId, selected);
};

const allCheck = (selected) => {
    cartStore.allCheck(selected);
};

onMounted(() => {
    cartStore.getCartList();
});
</script>

<template>
    <headers />
    <div class="xtx-cart-page">
        <div class="container m-top-20">
            <div class="cart">
                <table>
                    <thead>
                        <tr>
                            <th width="120">
                                <el-checkbox :model-value="cartStore.isAll" @change="allCheck">全选</el-checkbox>
                            </th>
                            <th width="400">商品信息</th>
                            <th width="220">单价</th>
                            <th width="180">数量</th>
                            <th width="180">小计</th>
                            <th width="140">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="i in cartStore.cartList" :key="i.goodsId">
                            <td>
                                <el-checkbox :model-value="i.selected" @change="(selected) => singleCheck(i, selected)" />
                            </td>
                            <td>
                                <div class="goods">
                                    <img :src="normalizeGoodsImageUrl(i.goodsCoverImg)" alt="" @error="handleGoodsImageError" />
                                    <div>
                                        <p class="name ellipsis">{{ i.goodsName }}</p>
                                    </div>
                                </div>
                            </td>
                            <td class="tc"><p>&yen;{{ i.sellingPrice }}</p></td>
                            <td class="tc"><el-input-number v-model="i.goodsCount" /></td>
                            <td class="tc"><p class="f16 red">&yen;{{ (i.sellingPrice * i.goodsCount).toFixed(2) }}</p></td>
                            <td class="tc">
                                <el-popconfirm title="确认删除吗?" confirm-button-text="确认" cancel-button-text="取消" @confirm="cartStore.delCart(i.cartItemId)">
                                    <template #reference>
                                        <a href="javascript:;">删除</a>
                                    </template>
                                </el-popconfirm>
                            </td>
                        </tr>
                        <tr v-if="cartStore.cartList.length === 0">
                            <td colspan="6">
                                <div class="cart-none">
                                    <el-empty description="购物车列表为空">
                                        <router-link to="/">
                                            <el-button type="primary">随便逛逛</el-button>
                                        </router-link>
                                    </el-empty>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="action">
            <div class="batch">
                共 {{ cartStore.allCount }} 件商品，已选择 {{ cartStore.selectedCount }} 件，商品合计：
                <span class="red">&yen;{{ cartStore.selectedPrice.toFixed(2) }}</span>
            </div>
            <div class="total">
                <el-button size="large" type="primary" data-testid="checkout-btn" @click="$router.push('/checkout')">下单结算</el-button>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.xtx-cart-page {
    margin-top: 20px;

    .cart {
        background: linear-gradient(140deg, rgb(24 26 33 / 92%), rgb(33 25 31 / 88%));
        color: rgb(255 255 255 / 80%);
        border: 1px solid rgb(255 255 255 / 14%);
        border-radius: 12px;

        table {
            border-spacing: 0;
            border-collapse: collapse;
            line-height: 24px;

            th,
            td {
                padding: 10px;
                border-bottom: 1px solid rgb(255 255 255 / 12%);

                &:first-child {
                    text-align: left;
                    padding-left: 30px;
                    color: rgb(255 255 255 / 58%);
                }
            }

            th {
                font-size: 16px;
                font-weight: normal;
                line-height: 50px;
                color: rgb(255 255 255 / 92%);
            }
        }
    }

    .cart-none {
        text-align: center;
        padding: 120px 0;
        background: rgb(20 22 28 / 80%);
    }

    .tc {
        text-align: center;

        a {
            color: $xtxColor;
        }
    }

    .red {
        color: $priceColor;
    }

    .f16 {
        font-size: 16px;
    }

    .goods {
        display: flex;
        align-items: center;

        img {
            width: 100px;
            height: 100px;
        }

        > div {
            width: 280px;
            font-size: 16px;
            padding-left: 10px;
        }
    }

    .action {
        display: flex;
        background: linear-gradient(140deg, rgb(24 26 33 / 92%), rgb(33 25 31 / 88%));
        margin: 20px auto 0;
        height: 80px;
        align-items: center;
        font-size: 16px;
        justify-content: space-between;
        padding: 0 30px;
        max-width: 1200px;
        border: 1px solid rgb(255 255 255 / 14%);
        border-radius: 12px;

        .red {
            font-size: 18px;
            margin-right: 20px;
            font-weight: bold;
        }
    }
}
</style>
