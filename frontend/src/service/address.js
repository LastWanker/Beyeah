import httpInstance from "../utils/axios"

export const listAddressesAPI = () => {
  return httpInstance({
    url: "/api/v1/addresses",
    method: "GET",
  })
}

export const createAddressAPI = (payload) => {
  return httpInstance({
    url: "/api/v1/addresses",
    method: "POST",
    data: payload,
  })
}

export const updateAddressAPI = ({ addressId, ...payload }) => {
  return httpInstance({
    url: `/api/v1/addresses/${addressId}`,
    method: "PUT",
    data: payload,
  })
}

export const deleteAddressAPI = (addressId) => {
  return httpInstance({
    url: `/api/v1/addresses/${addressId}`,
    method: "DELETE",
  })
}

export const setDefaultAddressAPI = (addressId) => {
  return httpInstance({
    url: `/api/v1/addresses/${addressId}/default`,
    method: "PUT",
  })
}
