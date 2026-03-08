/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahOrder;
import SCG.beyeah1211.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface beyeahOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(beyeahOrder record);

    int insertSelective(beyeahOrder record);

    beyeahOrder selectByPrimaryKey(Long orderId);

    beyeahOrder selectByOrderNo(String orderNo);

    beyeahOrder selectByUserIdAndIdempotencyKey(@Param("userId") Long userId, @Param("idempotencyKey") String idempotencyKey);

    int updateByPrimaryKeySelective(beyeahOrder record);

    int updateByPrimaryKey(beyeahOrder record);

    List<beyeahOrder> findbeyeahOrderList(PageQueryUtil pageUtil);

    int getTotalbeyeahOrders(PageQueryUtil pageUtil);

    List<beyeahOrder> selectByPrimaryKeys(@Param("orderIds") List<Long> orderIds);

    int checkOut(@Param("orderIds") List<Long> orderIds);

    int closeOrder(@Param("orderIds") List<Long> orderIds, @Param("orderStatus") int orderStatus);

    int checkDone(@Param("orderIds") List<Long> asList);

    List<Long> selectTimeoutUnpaidOrderIds(@Param("expireBefore") Date expireBefore, @Param("limit") int limit);

    int closeExpiredUnpaidOrderById(@Param("orderId") Long orderId,
                                    @Param("orderStatus") int orderStatus,
                                    @Param("payStatus") int payStatus);

    int updateRefundStatusByOrderId(@Param("orderId") Long orderId,
                                    @Param("refundStatus") int refundStatus,
                                    @Param("refundTime") Date refundTime);
}
