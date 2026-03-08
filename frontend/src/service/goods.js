import httpInstance from "../utils/axios";

/**
 * @data { 
     categoryId: 45 ,
     sortField: 'publishTime' | 'orderNum' | 'evaluateNum'
   } 
 */
export const getGoodsByCateAPI = (data) => {
    return httpInstance({
        url: '/api/v1/goods',
        method: 'GET',
        params: {
            categoryId: data.categoryId,
            sortField: data.sortField,
            page: data.page || 1,
            pageSize: data.pageSize || 20,
        }
    })
}

/**
 * @data { 
     goodsName: "Apple" ,
     sortField: 'publishTime' | 'orderNum' | 'evaluateNum'
   } 
 */
export const getGoodsByNameAPI = (data) => {
    return httpInstance({
        url: '/api/v1/goods',
        method: 'GET',
        params: {
            keyword: data.goodsName,
            sortField: data.sortField,
            page: data.page || 1,
            pageSize: data.pageSize || 20,
        }
    })
}
