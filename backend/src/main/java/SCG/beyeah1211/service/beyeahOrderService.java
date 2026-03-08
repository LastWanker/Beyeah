/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.service;

import SCG.beyeah1211.controller.vo.beyeahOrderDetailVO;
import SCG.beyeah1211.controller.vo.beyeahOrderItemVO;
import SCG.beyeah1211.controller.vo.beyeahShoppingCartItemVO;
import SCG.beyeah1211.controller.vo.beyeahUserVO;
import SCG.beyeah1211.entity.beyeahOrder;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;

import java.util.List;

public interface beyeahOrderService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getbeyeahOrdersPage(PageQueryUtil pageUtil);

    /**
     * 订单信息修改
     *
     * @param beyeahOrder
     * @return
     */
    String updateOrderInfo(beyeahOrder beyeahOrder);

    /**
     * 配货
     *
     * @param ids
     * @return
     */
    String checkDone(Long[] ids);

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    String closeOrder(Long[] ids);

    /**
     * 保存订单
     *
     * @param user
     * @param myShoppingCartItems
     * @return
     */
    String saveOrder(beyeahUserVO user, List<beyeahShoppingCartItemVO> myShoppingCartItems, String idempotencyKey);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    beyeahOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @return
     */
    beyeahOrder getbeyeahOrderByOrderNo(String orderNo);

    beyeahOrder getOrderByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    /**
     * 我的订单列表
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * 手动取消订单
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    List<beyeahOrderItemVO> getOrderItems(Long id);

    int autoCloseExpiredOrders(int expireSeconds, int batchSize);
}
