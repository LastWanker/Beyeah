package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahRefund;
import org.apache.ibatis.annotations.Param;

public interface beyeahRefundMapper {
    int insertSelective(beyeahRefund record);

    int updateByPrimaryKeySelective(beyeahRefund record);

    beyeahRefund selectByPrimaryKey(@Param("refundId") Long refundId);

    beyeahRefund selectByAfterSaleId(@Param("afterSaleId") Long afterSaleId);

    beyeahRefund selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
