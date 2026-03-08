import httpInstance from "../utils/axios";

export const loginAPI = ({ username, password }) => {
    return httpInstance({
        url: '/api/v1/auth/login',
        method: 'POST',
        data: {
            username,
            password
        }
    })
}

export const regAPI = ({ username, password }) => {
    return httpInstance({
        url: '/api/v1/auth/register',
        method: 'POST',
        data: {
            username,
            password
        }
    })
}
