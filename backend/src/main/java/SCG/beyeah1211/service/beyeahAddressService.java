package SCG.beyeah1211.service;

import SCG.beyeah1211.entity.beyeahUserAddress;

import java.util.List;

public interface beyeahAddressService {
    List<beyeahUserAddress> getMyAddresses(Long userId);

    beyeahUserAddress getAddress(Long userId, Long addressId);

    beyeahUserAddress getDefaultAddress(Long userId);

    beyeahUserAddress createAddress(Long userId, beyeahUserAddress address);

    beyeahUserAddress updateAddress(Long userId, Long addressId, beyeahUserAddress address);

    void deleteAddress(Long userId, Long addressId);

    beyeahUserAddress setDefaultAddress(Long userId, Long addressId);
}
