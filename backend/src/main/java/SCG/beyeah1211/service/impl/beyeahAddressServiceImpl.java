package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.dao.beyeahUserAddressMapper;
import SCG.beyeah1211.entity.beyeahUserAddress;
import SCG.beyeah1211.service.beyeahAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class beyeahAddressServiceImpl implements beyeahAddressService {
    private static final int CONSIGNEE_MAX_LENGTH = 50;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int LOCATION_MAX_LENGTH = 32;
    private static final int DETAIL_MAX_LENGTH = 120;

    @Resource
    private beyeahUserAddressMapper beyeahUserAddressMapper;

    @Override
    public List<beyeahUserAddress> getMyAddresses(Long userId) {
        return beyeahUserAddressMapper.selectByUserId(userId);
    }

    @Override
    public beyeahUserAddress getAddress(Long userId, Long addressId) {
        if (addressId == null || addressId <= 0) {
            return null;
        }
        return beyeahUserAddressMapper.selectByAddressIdAndUserId(addressId, userId);
    }

    @Override
    public beyeahUserAddress getDefaultAddress(Long userId) {
        return beyeahUserAddressMapper.selectDefaultByUserId(userId);
    }

    @Override
    @Transactional
    public beyeahUserAddress createAddress(Long userId, beyeahUserAddress address) {
        normalizeAndValidate(address);

        boolean hasAnyAddress = beyeahUserAddressMapper.selectFirstActiveByUserId(userId) != null;
        boolean shouldDefault = Byte.valueOf((byte) 1).equals(address.getIsDefault()) || !hasAnyAddress;

        Date now = new Date();
        address.setUserId(userId);
        address.setIsDeleted((byte) 0);
        address.setIsDefault(shouldDefault ? (byte) 1 : (byte) 0);
        address.setCreateTime(now);
        address.setUpdateTime(now);
        if (shouldDefault) {
            beyeahUserAddressMapper.clearDefaultByUserId(userId);
        }

        int inserted = beyeahUserAddressMapper.insertSelective(address);
        if (inserted <= 0 || address.getAddressId() == null) {
            beyeahException.fail("create address failed");
        }
        return beyeahUserAddressMapper.selectByAddressIdAndUserId(address.getAddressId(), userId);
    }

    @Override
    @Transactional
    public beyeahUserAddress updateAddress(Long userId, Long addressId, beyeahUserAddress patch) {
        beyeahUserAddress current = requireOwnedAddress(userId, addressId);
        applyPatch(current, patch);
        normalizeAndValidate(current);

        boolean requestDefault = patch != null && Byte.valueOf((byte) 1).equals(patch.getIsDefault());
        if (requestDefault) {
            beyeahUserAddressMapper.clearDefaultByUserId(userId);
            current.setIsDefault((byte) 1);
        }
        current.setUpdateTime(new Date());
        if (beyeahUserAddressMapper.updateByPrimaryKeySelective(current) <= 0) {
            beyeahException.fail("update address failed");
        }
        return beyeahUserAddressMapper.selectByAddressIdAndUserId(addressId, userId);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        beyeahUserAddress current = requireOwnedAddress(userId, addressId);
        if (beyeahUserAddressMapper.softDeleteById(addressId, userId) <= 0) {
            beyeahException.fail("delete address failed");
        }

        if (Byte.valueOf((byte) 1).equals(current.getIsDefault())) {
            beyeahUserAddress fallback = beyeahUserAddressMapper.selectFirstActiveByUserId(userId);
            if (fallback != null) {
                beyeahUserAddressMapper.clearDefaultByUserId(userId);
                beyeahUserAddressMapper.setDefaultById(fallback.getAddressId(), userId);
            }
        }
    }

    @Override
    @Transactional
    public beyeahUserAddress setDefaultAddress(Long userId, Long addressId) {
        beyeahUserAddress current = requireOwnedAddress(userId, addressId);
        beyeahUserAddressMapper.clearDefaultByUserId(userId);
        if (beyeahUserAddressMapper.setDefaultById(current.getAddressId(), userId) <= 0) {
            beyeahException.fail("set default address failed");
        }
        return beyeahUserAddressMapper.selectByAddressIdAndUserId(addressId, userId);
    }

    private beyeahUserAddress requireOwnedAddress(Long userId, Long addressId) {
        if (addressId == null || addressId <= 0) {
            beyeahException.fail("addressId is required");
        }
        beyeahUserAddress current = beyeahUserAddressMapper.selectByAddressIdAndUserId(addressId, userId);
        if (current == null) {
            beyeahException.fail("address not found");
        }
        return current;
    }

    private void applyPatch(beyeahUserAddress current, beyeahUserAddress patch) {
        if (patch == null) {
            return;
        }
        if (patch.getConsignee() != null) {
            current.setConsignee(patch.getConsignee());
        }
        if (patch.getPhone() != null) {
            current.setPhone(patch.getPhone());
        }
        if (patch.getProvince() != null) {
            current.setProvince(patch.getProvince());
        }
        if (patch.getCity() != null) {
            current.setCity(patch.getCity());
        }
        if (patch.getDistrict() != null) {
            current.setDistrict(patch.getDistrict());
        }
        if (patch.getDetail() != null) {
            current.setDetail(patch.getDetail());
        }
        if (patch.getIsDefault() != null && Byte.valueOf((byte) 1).equals(patch.getIsDefault())) {
            current.setIsDefault((byte) 1);
        }
    }

    private void normalizeAndValidate(beyeahUserAddress address) {
        if (address == null) {
            beyeahException.fail("address payload is required");
        }
        address.setConsignee(clean(address.getConsignee()));
        address.setPhone(clean(address.getPhone()));
        address.setProvince(clean(address.getProvince()));
        address.setCity(clean(address.getCity()));
        address.setDistrict(clean(address.getDistrict()));
        address.setDetail(clean(address.getDetail()));

        requireText(address.getConsignee(), "consignee");
        requireText(address.getPhone(), "phone");
        requireText(address.getProvince(), "province");
        requireText(address.getCity(), "city");
        requireText(address.getDistrict(), "district");
        requireText(address.getDetail(), "detail");

        if (address.getConsignee().length() > CONSIGNEE_MAX_LENGTH) {
            beyeahException.fail("consignee is too long");
        }
        if (address.getPhone().length() > PHONE_MAX_LENGTH) {
            beyeahException.fail("phone is too long");
        }
        if (address.getProvince().length() > LOCATION_MAX_LENGTH
                || address.getCity().length() > LOCATION_MAX_LENGTH
                || address.getDistrict().length() > LOCATION_MAX_LENGTH) {
            beyeahException.fail("region field is too long");
        }
        if (address.getDetail().length() > DETAIL_MAX_LENGTH) {
            beyeahException.fail("detail is too long");
        }
        if (!address.getPhone().matches("^[0-9+\\-() ]{6,20}$")) {
            beyeahException.fail("invalid phone");
        }
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            beyeahException.fail(field + " is required");
        }
    }

    private String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
