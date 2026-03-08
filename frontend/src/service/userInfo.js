import httpInstance from "../utils/axios"

export const saveUserInfoAPI = ({ nickname, address }) => {
  return httpInstance({
    url: "/api/v1/users/me",
    method: "PUT",
    data: {
      nickname,
      address,
    },
  })
}

export const searchUserInfoAPI = () => {
  return httpInstance({
    url: "/api/v1/users/me",
    method: "GET",
  })
}
