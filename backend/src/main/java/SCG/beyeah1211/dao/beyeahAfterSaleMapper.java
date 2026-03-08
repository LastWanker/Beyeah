package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahAfterSale;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface beyeahAfterSaleMapper {
    int insertSelective(beyeahAfterSale record);

    int updateByPrimaryKeySelective(beyeahAfterSale record);

    beyeahAfterSale selectByPrimaryKey(@Param("afterSaleId") Long afterSaleId);

    beyeahAfterSale selectByAfterSaleNo(@Param("afterSaleNo") String afterSaleNo);

    beyeahAfterSale selectByOrderItemId(@Param("orderItemId") Long orderItemId);

    List<beyeahAfterSale> findAfterSaleList(Map<String, Object> params);

    int getTotalAfterSales(Map<String, Object> params);
}
