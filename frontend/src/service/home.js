import httpInstance from "../utils/axios";

export const getNewAPI = () => {
    return httpInstance({
        url: '/api/v1/home/new',
        method: 'GET'
    })
}

export const getHotAPI = () => {
    return httpInstance({
        url: '/api/v1/home/hot',
        method: 'GET'
    })
}
