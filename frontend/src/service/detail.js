import httpInstance from "../utils/axios";

export const getGoodDetail = (goodsId) => {
    return httpInstance({
        url: `/api/v1/goods/${goodsId}`,
        method: 'GET'
    })
}
