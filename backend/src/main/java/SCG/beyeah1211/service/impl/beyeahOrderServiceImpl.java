/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.*;
import SCG.beyeah1211.controller.vo.*;
import SCG.beyeah1211.dao.beyeahGoodsMapper;
import SCG.beyeah1211.dao.beyeahOrderItemMapper;
import SCG.beyeah1211.dao.beyeahOrderMapper;
import SCG.beyeah1211.dao.beyeahShoppingCartItemMapper;
import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.entity.beyeahOrder;
import SCG.beyeah1211.entity.beyeahOrderItem;
import SCG.beyeah1211.entity.StockNumDTO;
import SCG.beyeah1211.service.beyeahOrderService;
import SCG.beyeah1211.util.BeanUtil;
import SCG.beyeah1211.util.NumberUtil;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class beyeahOrderServiceImpl implements beyeahOrderService {
    private static final Logger logger = LoggerFactory.getLogger(beyeahOrderServiceImpl.class);

    @Autowired
    private beyeahOrderMapper beyeahOrderMapper;
    @Autowired
    private beyeahOrderItemMapper beyeahOrderItemMapper;
    @Autowired
    private beyeahShoppingCartItemMapper beyeahShoppingCartItemMapper;
    @Autowired
    private beyeahGoodsMapper beyeahGoodsMapper;

    @Override
    public PageResult getbeyeahOrdersPage(PageQueryUtil pageUtil) {
        List<beyeahOrder> beyeahOrders = beyeahOrderMapper.findbeyeahOrderList(pageUtil);
        int total = beyeahOrderMapper.getTotalbeyeahOrders(pageUtil);
        PageResult pageResult = new PageResult(beyeahOrders, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    @Transactional
    public String updateOrderInfo(beyeahOrder beyeahOrder) {
        beyeahOrder temp = beyeahOrderMapper.selectByPrimaryKey(beyeahOrder.getOrderId());
        //不为空且orderStatus>=0且状态为出库之前可以修改部分信息
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(beyeahOrder.getTotalPrice());
            temp.setUserAddress(beyeahOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (beyeahOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<beyeahOrder> orders = beyeahOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (beyeahOrder beyeahOrder : orders) {
                if (beyeahOrder.getIsDeleted() == 1) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                    continue;
                }
                if (beyeahOrder.getOrderStatus() != 1) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                }
            }
            if (!StringUtils.hasText(errorOrderNos)) {
                //订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (beyeahOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<beyeahOrder> orders = beyeahOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (beyeahOrder beyeahOrder : orders) {
                if (beyeahOrder.getIsDeleted() == 1) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                    continue;
                }
                if (beyeahOrder.getOrderStatus() != 1 && beyeahOrder.getOrderStatus() != 2) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                }
            }
            if (!StringUtils.hasText(errorOrderNos)) {
                //订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (beyeahOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<beyeahOrder> orders = beyeahOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (beyeahOrder beyeahOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (beyeahOrder.getIsDeleted() == 1) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (beyeahOrder.getOrderStatus() == 4 || beyeahOrder.getOrderStatus() < 0) {
                    errorOrderNos += beyeahOrder.getOrderNo() + " ";
                }
            }
            if (!StringUtils.hasText(errorOrderNos)) {
                //订单状态正常 可以执行关闭操作 修改订单状态和更新时间&&恢复库存
                if (beyeahOrderMapper.closeOrder(Arrays.asList(ids), beyeahOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0 && recoverStockNum(Arrays.asList(ids))) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String saveOrder(beyeahUserVO user, List<beyeahShoppingCartItemVO> myShoppingCartItems, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            beyeahOrder existingOrder = beyeahOrderMapper.selectByUserIdAndIdempotencyKey(user.getUserId(), normalizedIdempotencyKey);
            if (existingOrder != null) {
                return existingOrder.getOrderNo();
            }
        }

        List<Long> itemIdList = myShoppingCartItems.stream().map(beyeahShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        List<Long> goodsIds = myShoppingCartItems.stream().map(beyeahShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        List<beyeahGoods> beyeahGoods = beyeahGoodsMapper.selectByPrimaryKeys(goodsIds);
        //检查是否包含已下架商品
        List<beyeahGoods> goodsListNotSelling = beyeahGoods.stream()
                .filter(beyeahGoodsTemp -> beyeahGoodsTemp.getGoodsSellStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodsListNotSelling)) {
            //goodsListNotSelling 对象非空则表示有下架商品
            beyeahException.fail(goodsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, beyeahGoods> beyeahGoodsMap = beyeahGoods.stream().collect(Collectors.toMap(SCG.beyeah1211.entity.beyeahGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //判断商品库存
        for (beyeahShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            //查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!beyeahGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                beyeahException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            //存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > beyeahGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                beyeahException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }

        if (CollectionUtils.isEmpty(itemIdList) || CollectionUtils.isEmpty(goodsIds) || CollectionUtils.isEmpty(beyeahGoods)) {
            beyeahException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }

        // 事务顺序：扣库存 -> 写订单 -> 写订单项 -> 删购物车
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(myShoppingCartItems, StockNumDTO.class);
        int updateStockNumResult = beyeahGoodsMapper.updateStockNum(stockNumDTOS);
        if (updateStockNumResult != stockNumDTOS.size()) {
            beyeahException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
        }

        String orderNo = NumberUtil.genOrderNo();
        int priceTotal = 0;
        for (beyeahShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            priceTotal += shoppingCartItemVO.getGoodsCount() * shoppingCartItemVO.getSellingPrice();
        }
        if (priceTotal < 1) {
            beyeahException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
        }

        beyeahOrder beyeahOrder = new beyeahOrder();
        beyeahOrder.setOrderNo(orderNo);
        beyeahOrder.setIdempotencyKey(normalizedIdempotencyKey);
        beyeahOrder.setUserId(user.getUserId());
        beyeahOrder.setUserName(user.getNickName());
        beyeahOrder.setUserPhone(user.getusername());
        beyeahOrder.setUserAddress(user.getAddress());
        beyeahOrder.setTotalPrice(priceTotal);
        beyeahOrder.setExtraInfo("");

        try {
            if (beyeahOrderMapper.insertSelective(beyeahOrder) <= 0) {
                beyeahException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
        } catch (DuplicateKeyException duplicateKeyException) {
            if (normalizedIdempotencyKey != null) {
                beyeahOrder existingOrder = beyeahOrderMapper.selectByUserIdAndIdempotencyKey(user.getUserId(), normalizedIdempotencyKey);
                if (existingOrder != null) {
                    return existingOrder.getOrderNo();
                }
            }
            throw duplicateKeyException;
        }

        List<beyeahOrderItem> beyeahOrderItems = new ArrayList<>();
        for (beyeahShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            beyeahOrderItem beyeahOrderItem = new beyeahOrderItem();
            BeanUtil.copyProperties(shoppingCartItemVO, beyeahOrderItem);
            beyeahOrderItem.setOrderId(beyeahOrder.getOrderId());
            beyeahOrderItems.add(beyeahOrderItem);
        }
        if (beyeahOrderItemMapper.insertBatch(beyeahOrderItems) <= 0) {
            beyeahException.fail(ServiceResultEnum.ORDER_GENERATE_ERROR.getResult());
        }

        int deletedCount = beyeahShoppingCartItemMapper.deleteBatch(itemIdList);
        if (deletedCount < itemIdList.size()) {
            beyeahException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }

        return orderNo;
    }

    @Override
    public beyeahOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        beyeahOrder beyeahOrder = beyeahOrderMapper.selectByOrderNo(orderNo);
        if (beyeahOrder == null) {
            beyeahException.fail(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        //验证是否是当前userId下的订单，否则报错
        if (!userId.equals(beyeahOrder.getUserId())) {
            beyeahException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        List<beyeahOrderItem> orderItems = beyeahOrderItemMapper.selectByOrderId(beyeahOrder.getOrderId());
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            beyeahException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<beyeahOrderItemVO> beyeahOrderItemVOS = BeanUtil.copyList(orderItems, beyeahOrderItemVO.class);
        beyeahOrderDetailVO beyeahOrderDetailVO = new beyeahOrderDetailVO();
        BeanUtil.copyProperties(beyeahOrder, beyeahOrderDetailVO);
        beyeahOrderDetailVO.setOrderStatusString(beyeahOrderStatusEnum.getbeyeahOrderStatusEnumByStatus(beyeahOrderDetailVO.getOrderStatus()).getName());
        beyeahOrderDetailVO.setRefundStatusString(OrderRefundStatusEnum.getByStatus(beyeahOrderDetailVO.getRefundStatus() == null ? 0 : beyeahOrderDetailVO.getRefundStatus()).getName());
        beyeahOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(beyeahOrderDetailVO.getPayType()).getName());
        beyeahOrderDetailVO.setbeyeahOrderItemVOS(beyeahOrderItemVOS);
        return beyeahOrderDetailVO;
    }

    @Override
    public beyeahOrder getbeyeahOrderByOrderNo(String orderNo) {
        return beyeahOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public beyeahOrder getOrderByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        return beyeahOrderMapper.selectByUserIdAndIdempotencyKey(userId, idempotencyKey.trim());
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = beyeahOrderMapper.getTotalbeyeahOrders(pageUtil);
        List<beyeahOrder> beyeahOrders = beyeahOrderMapper.findbeyeahOrderList(pageUtil);
        List<beyeahOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(beyeahOrders, beyeahOrderListVO.class);
            //设置订单状态中文显示值
            for (beyeahOrderListVO beyeahOrderListVO : orderListVOS) {
                beyeahOrderListVO.setOrderStatusString(beyeahOrderStatusEnum.getbeyeahOrderStatusEnumByStatus(beyeahOrderListVO.getOrderStatus()).getName());
                beyeahOrderListVO.setRefundStatusString(OrderRefundStatusEnum.getByStatus(beyeahOrderListVO.getRefundStatus() == null ? 0 : beyeahOrderListVO.getRefundStatus()).getName());
            }
            List<Long> orderIds = beyeahOrders.stream().map(beyeahOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<beyeahOrderItem> orderItems = beyeahOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<beyeahOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(beyeahOrderItem::getOrderId));
                for (beyeahOrderListVO beyeahOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(beyeahOrderListVO.getOrderId())) {
                        List<beyeahOrderItem> orderItemListTemp = itemByOrderIdMap.get(beyeahOrderListVO.getOrderId());
                        //将beyeahOrderItem对象列表转换成beyeahOrderItemVO对象列表
                        List<beyeahOrderItemVO> beyeahOrderItemVOS = BeanUtil.copyList(orderItemListTemp, beyeahOrderItemVO.class);
                        beyeahOrderListVO.setbeyeahOrderItemVOS(beyeahOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    @Transactional
    public String cancelOrder(String orderNo, Long userId) {
        beyeahOrder beyeahOrder = beyeahOrderMapper.selectByOrderNo(orderNo);
        if (beyeahOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(beyeahOrder.getUserId())) {
                beyeahException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            //订单状态判断
            if (beyeahOrder.getOrderStatus().intValue() == beyeahOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || beyeahOrder.getOrderStatus().intValue() == beyeahOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || beyeahOrder.getOrderStatus().intValue() == beyeahOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || beyeahOrder.getOrderStatus().intValue() == beyeahOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            //修改订单状态&&恢复库存
            if (beyeahOrderMapper.closeOrder(Collections.singletonList(beyeahOrder.getOrderId()), beyeahOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0 && recoverStockNum(Collections.singletonList(beyeahOrder.getOrderId()))) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        beyeahOrder beyeahOrder = beyeahOrderMapper.selectByOrderNo(orderNo);
        if (beyeahOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(beyeahOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            //订单状态判断 非出库状态下不进行修改操作
            if (beyeahOrder.getOrderStatus().intValue() != beyeahOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            beyeahOrder.setOrderStatus((byte) beyeahOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            beyeahOrder.setUpdateTime(new Date());
            if (beyeahOrderMapper.updateByPrimaryKeySelective(beyeahOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        beyeahOrder beyeahOrder = beyeahOrderMapper.selectByOrderNo(orderNo);
        if (beyeahOrder != null) {
            //订单状态判断 非待支付状态下不进行修改操作
            if (beyeahOrder.getOrderStatus().intValue() != beyeahOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            beyeahOrder.setOrderStatus((byte) beyeahOrderStatusEnum.ORDER_PAID.getOrderStatus());
            beyeahOrder.setPayType((byte) payType);
            beyeahOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            beyeahOrder.setPayTime(new Date());
            beyeahOrder.setUpdateTime(new Date());
            if (beyeahOrderMapper.updateByPrimaryKeySelective(beyeahOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public List<beyeahOrderItemVO> getOrderItems(Long id) {
        beyeahOrder beyeahOrder = beyeahOrderMapper.selectByPrimaryKey(id);
        if (beyeahOrder != null) {
            List<beyeahOrderItem> orderItems = beyeahOrderItemMapper.selectByOrderId(beyeahOrder.getOrderId());
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<beyeahOrderItemVO> beyeahOrderItemVOS = BeanUtil.copyList(orderItems, beyeahOrderItemVO.class);
                return beyeahOrderItemVOS;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public int autoCloseExpiredOrders(int expireSeconds, int batchSize) {
        int seconds = Math.max(expireSeconds, 60);
        int limit = Math.max(batchSize, 1);
        Date expireBefore = new Date(System.currentTimeMillis() - seconds * 1000L);
        List<Long> candidateOrderIds = beyeahOrderMapper.selectTimeoutUnpaidOrderIds(expireBefore, limit);
        if (CollectionUtils.isEmpty(candidateOrderIds)) {
            return 0;
        }

        List<Long> closedOrderIds = new ArrayList<>();
        for (Long orderId : candidateOrderIds) {
            int affected = beyeahOrderMapper.closeExpiredUnpaidOrderById(
                    orderId,
                    beyeahOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus(),
                    PayStatusEnum.DEFAULT.getPayStatus()
            );
            if (affected > 0) {
                closedOrderIds.add(orderId);
            }
        }

        if (CollectionUtils.isEmpty(closedOrderIds)) {
            return 0;
        }
        try {
            recoverStockNum(closedOrderIds);
        } catch (Exception exception) {
            // 避免定时任务因历史脏数据中断；订单关闭已成功，库存回补单独告警处理。
            logger.warn("auto-close recovered {} orders but stock recovery failed for orderIds={}",
                    closedOrderIds.size(), closedOrderIds);
        }
        return closedOrderIds.size();
    }

    /**
     * 恢复库存
     * @param orderIds
     * @return
     */
    public Boolean recoverStockNum(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return true;
        }
        //查询对应的订单项
        List<beyeahOrderItem> beyeahOrderItems = beyeahOrderItemMapper.selectByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(beyeahOrderItems)) {
            return true;
        }
        //获取对应的商品id和商品数量并赋值到StockNumDTO对象中
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(beyeahOrderItems, StockNumDTO.class);
        //执行恢复库存的操作
        int updateStockNumResult = beyeahGoodsMapper.recoverStockNum(stockNumDTOS);
        if (updateStockNumResult != stockNumDTOS.size()) {
            beyeahException.fail(ServiceResultEnum.CLOSE_ORDER_ERROR.getResult());
            return false;
        } else {
            return true;
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        if (normalized.length() > 64) {
            beyeahException.fail("idempotencyKey is too long");
        }
        return normalized;
    }
}
