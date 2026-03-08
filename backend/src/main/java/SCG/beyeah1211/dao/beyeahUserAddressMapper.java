package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahUserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface beyeahUserAddressMapper {
    int insertSelective(beyeahUserAddress record);

    int updateByPrimaryKeySelective(beyeahUserAddress record);

    beyeahUserAddress selectByAddressId(@Param("addressId") Long addressId);

    beyeahUserAddress selectByAddressIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);

    List<beyeahUserAddress> selectByUserId(@Param("userId") Long userId);

    beyeahUserAddress selectDefaultByUserId(@Param("userId") Long userId);

    beyeahUserAddress selectFirstActiveByUserId(@Param("userId") Long userId);

    int clearDefaultByUserId(@Param("userId") Long userId);

    int setDefaultById(@Param("addressId") Long addressId, @Param("userId") Long userId);

    int softDeleteById(@Param("addressId") Long addressId, @Param("userId") Long userId);
}
