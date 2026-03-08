package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.AfterSaleStatusEnum;
import SCG.beyeah1211.common.OrderRefundStatusEnum;
import SCG.beyeah1211.common.PayStatusEnum;
import SCG.beyeah1211.common.RefundStatusEnum;
import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.controller.vo.AfterSaleVO;
import SCG.beyeah1211.dao.beyeahAfterSaleMapper;
import SCG.beyeah1211.dao.beyeahOrderItemMapper;
import SCG.beyeah1211.dao.beyeahOrderMapper;
import SCG.beyeah1211.dao.beyeahRefundMapper;
import SCG.beyeah1211.entity.beyeahAfterSale;
import SCG.beyeah1211.entity.beyeahOrder;
import SCG.beyeah1211.entity.beyeahOrderItem;
import SCG.beyeah1211.entity.beyeahRefund;
import SCG.beyeah1211.service.beyeahAfterSaleService;
import SCG.beyeah1211.util.NumberUtil;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class beyeahAfterSaleServiceImpl implements beyeahAfterSaleService {
    private static final int REASON_MAX_LENGTH = 120;
    private static final int DESC_MAX_LENGTH = 255;
    private static final int REJECT_REASON_MAX_LENGTH = 120;

    @Resource
    private beyeahAfterSaleMapper beyeahAfterSaleMapper;
    @Resource
    private beyeahRefundMapper beyeahRefundMapper;
    @Resource
    private beyeahOrderMapper beyeahOrderMapper;
    @Resource
    private beyeahOrderItemMapper beyeahOrderItemMapper;

    @Override
    @Transactional
    public AfterSaleVO createAfterSale(Long userId,
                                       String orderNo,
                                       Long orderItemId,
                                       Integer refundAmount,
                                       String reason,
                                       String description) {
        if (!StringUtils.hasText(orderNo)) {
            beyeahException.fail("orderNo is required");
        }
        if (orderItemId == null || orderItemId <= 0) {
            beyeahException.fail("orderItemId is required");
        }
        if (refundAmount == null || refundAmount <= 0) {
            beyeahException.fail("refundAmount is required");
        }
        String normalizedReason = normalizeText(reason, REASON_MAX_LENGTH, "reason");
        String normalizedDescription = normalizeOptionalText(description, DESC_MAX_LENGTH);

        beyeahOrder order = beyeahOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null || !userId.equals(order.getUserId())) {
            beyeahException.fail("order not found");
        }
        if (order.getPayStatus() == null || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            beyeahException.fail("order is not paid");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() < 0) {
            beyeahException.fail("order status is invalid");
        }

        List<beyeahOrderItem> orderItems = beyeahOrderItemMapper.selectByOrderId(order.getOrderId());
        if (CollectionUtils.isEmpty(orderItems)) {
            beyeahException.fail("order item not found");
        }

        beyeahOrderItem matchedItem = null;
        for (beyeahOrderItem item : orderItems) {
            if (orderItemId.equals(item.getOrderItemId())) {
                matchedItem = item;
                break;
            }
        }
        if (matchedItem == null) {
            beyeahException.fail("order item not found");
        }

        int maxRefundAmount = matchedItem.getSellingPrice() * matchedItem.getGoodsCount();
        if (refundAmount > maxRefundAmount) {
            beyeahException.fail("refundAmount exceeds order item amount");
        }

        beyeahAfterSale existing = beyeahAfterSaleMapper.selectByOrderItemId(orderItemId);
        if (existing != null) {
            beyeahException.fail("after-sale already exists");
        }

        Date now = new Date();
        beyeahAfterSale afterSale = new beyeahAfterSale();
        afterSale.setAfterSaleNo(genAfterSaleNo());
        afterSale.setUserId(userId);
        afterSale.setOrderId(order.getOrderId());
        afterSale.setOrderNo(order.getOrderNo());
        afterSale.setOrderItemId(matchedItem.getOrderItemId());
        afterSale.setGoodsId(matchedItem.getGoodsId());
        afterSale.setGoodsName(matchedItem.getGoodsName());
        afterSale.setRefundAmount(refundAmount);
        afterSale.setReason(normalizedReason);
        afterSale.setDescription(normalizedDescription);
        afterSale.setRejectReason("");
        afterSale.setAfterSaleStatus((byte) AfterSaleStatusEnum.PENDING.getStatus());
        afterSale.setIsDeleted((byte) 0);
        afterSale.setCreateTime(now);
        afterSale.setUpdateTime(now);
        if (beyeahAfterSaleMapper.insertSelective(afterSale) <= 0 || afterSale.getAfterSaleId() == null) {
            beyeahException.fail("create after-sale failed");
        }
        beyeahAfterSale saved = beyeahAfterSaleMapper.selectByPrimaryKey(afterSale.getAfterSaleId());
        return toVO(saved, null);
    }

    @Override
    public PageResult getMyAfterSales(PageQueryUtil pageUtil) {
        Map<String, Object> params = new HashMap<>(pageUtil);
        List<beyeahAfterSale> list = beyeahAfterSaleMapper.findAfterSaleList(params);
        int total = beyeahAfterSaleMapper.getTotalAfterSales(params);
        List<AfterSaleVO> voList = buildVOList(list);
        return new PageResult(voList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public AfterSaleVO getMyAfterSaleDetail(Long userId, String afterSaleNo) {
        if (!StringUtils.hasText(afterSaleNo)) {
            beyeahException.fail("afterSaleNo is required");
        }
        beyeahAfterSale afterSale = beyeahAfterSaleMapper.selectByAfterSaleNo(afterSaleNo.trim());
        if (afterSale == null || !userId.equals(afterSale.getUserId())) {
            beyeahException.fail("after-sale not found");
        }
        beyeahRefund refund = beyeahRefundMapper.selectByAfterSaleId(afterSale.getAfterSaleId());
        return toVO(afterSale, refund);
    }

    @Override
    public PageResult getAdminAfterSales(PageQueryUtil pageUtil) {
        Map<String, Object> params = new HashMap<>(pageUtil);
        params.remove("userId");
        List<beyeahAfterSale> list = beyeahAfterSaleMapper.findAfterSaleList(params);
        int total = beyeahAfterSaleMapper.getTotalAfterSales(params);
        List<AfterSaleVO> voList = buildVOList(list);
        return new PageResult(voList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    @Transactional
    public AfterSaleVO approveAfterSale(Long afterSaleId) {
        beyeahAfterSale afterSale = requireAfterSale(afterSaleId);
        if (afterSale.getAfterSaleStatus() != null
                && afterSale.getAfterSaleStatus() == AfterSaleStatusEnum.REJECTED.getStatus()) {
            beyeahException.fail("after-sale already rejected");
        }
        if (afterSale.getAfterSaleStatus() != null
                && afterSale.getAfterSaleStatus() != AfterSaleStatusEnum.PENDING.getStatus()) {
            beyeahRefund existingRefund = beyeahRefundMapper.selectByAfterSaleId(afterSale.getAfterSaleId());
            return toVO(afterSale, existingRefund);
        }

        Date now = new Date();
        beyeahOrderMapper.updateRefundStatusByOrderId(
                afterSale.getOrderId(),
                OrderRefundStatusEnum.REFUNDING.getStatus(),
                null
        );

        beyeahRefund refund = beyeahRefundMapper.selectByAfterSaleId(afterSale.getAfterSaleId());
        if (refund == null) {
            refund = new beyeahRefund();
            refund.setRefundNo(genRefundNo());
            refund.setAfterSaleId(afterSale.getAfterSaleId());
            refund.setAfterSaleNo(afterSale.getAfterSaleNo());
            refund.setUserId(afterSale.getUserId());
            refund.setOrderId(afterSale.getOrderId());
            refund.setOrderNo(afterSale.getOrderNo());
            refund.setRefundAmount(afterSale.getRefundAmount());
            refund.setRefundStatus((byte) RefundStatusEnum.PENDING.getStatus());
            refund.setRefundChannel((byte) 0);
            refund.setIdempotencyKey(genRefundIdempotencyKey(afterSale.getAfterSaleNo()));
            refund.setCallbackPayload("");
            refund.setFailReason("");
            refund.setCreateTime(now);
            refund.setUpdateTime(now);
            if (beyeahRefundMapper.insertSelective(refund) <= 0 || refund.getRefundId() == null) {
                beyeahException.fail("create refund failed");
            }
            refund = beyeahRefundMapper.selectByPrimaryKey(refund.getRefundId());
        }

        afterSale.setAfterSaleStatus((byte) AfterSaleStatusEnum.APPROVED.getStatus());
        afterSale.setHandleTime(now);
        afterSale.setRefundNo(refund.getRefundNo());
        afterSale.setUpdateTime(now);
        if (beyeahAfterSaleMapper.updateByPrimaryKeySelective(afterSale) <= 0) {
            beyeahException.fail("approve after-sale failed");
        }

        // mock refund pipeline: approved -> refunding -> refunded
        afterSale.setAfterSaleStatus((byte) AfterSaleStatusEnum.REFUNDING.getStatus());
        afterSale.setUpdateTime(now);
        beyeahAfterSaleMapper.updateByPrimaryKeySelective(afterSale);

        refund.setRefundStatus((byte) RefundStatusEnum.SUCCESS.getStatus());
        refund.setCallbackPayload("{\"channel\":\"mock\",\"result\":\"SUCCESS\"}");
        refund.setSuccessTime(now);
        refund.setUpdateTime(now);
        if (beyeahRefundMapper.updateByPrimaryKeySelective(refund) <= 0) {
            beyeahException.fail("update refund failed");
        }

        afterSale.setAfterSaleStatus((byte) AfterSaleStatusEnum.REFUNDED.getStatus());
        afterSale.setUpdateTime(now);
        if (beyeahAfterSaleMapper.updateByPrimaryKeySelective(afterSale) <= 0) {
            beyeahException.fail("update after-sale status failed");
        }

        beyeahOrderMapper.updateRefundStatusByOrderId(
                afterSale.getOrderId(),
                OrderRefundStatusEnum.REFUNDED.getStatus(),
                now
        );

        beyeahAfterSale latestAfterSale = beyeahAfterSaleMapper.selectByPrimaryKey(afterSaleId);
        beyeahRefund latestRefund = beyeahRefundMapper.selectByAfterSaleId(afterSaleId);
        return toVO(latestAfterSale, latestRefund);
    }

    @Override
    @Transactional
    public AfterSaleVO rejectAfterSale(Long afterSaleId, String rejectReason) {
        beyeahAfterSale afterSale = requireAfterSale(afterSaleId);
        if (afterSale.getAfterSaleStatus() != null
                && afterSale.getAfterSaleStatus() == AfterSaleStatusEnum.REJECTED.getStatus()) {
            beyeahRefund refund = beyeahRefundMapper.selectByAfterSaleId(afterSale.getAfterSaleId());
            return toVO(afterSale, refund);
        }
        if (afterSale.getAfterSaleStatus() != null
                && afterSale.getAfterSaleStatus() != AfterSaleStatusEnum.PENDING.getStatus()) {
            beyeahException.fail("after-sale status cannot be rejected");
        }

        String normalizedRejectReason = normalizeText(rejectReason, REJECT_REASON_MAX_LENGTH, "rejectReason");
        Date now = new Date();
        afterSale.setAfterSaleStatus((byte) AfterSaleStatusEnum.REJECTED.getStatus());
        afterSale.setRejectReason(normalizedRejectReason);
        afterSale.setHandleTime(now);
        afterSale.setUpdateTime(now);
        if (beyeahAfterSaleMapper.updateByPrimaryKeySelective(afterSale) <= 0) {
            beyeahException.fail("reject after-sale failed");
        }

        beyeahOrder order = beyeahOrderMapper.selectByPrimaryKey(afterSale.getOrderId());
        if (order != null && (order.getRefundStatus() == null || order.getRefundStatus() == OrderRefundStatusEnum.NONE.getStatus())) {
            beyeahOrderMapper.updateRefundStatusByOrderId(
                    afterSale.getOrderId(),
                    OrderRefundStatusEnum.REFUND_REJECTED.getStatus(),
                    null
            );
        }

        beyeahAfterSale latestAfterSale = beyeahAfterSaleMapper.selectByPrimaryKey(afterSaleId);
        return toVO(latestAfterSale, null);
    }

    private beyeahAfterSale requireAfterSale(Long afterSaleId) {
        if (afterSaleId == null || afterSaleId <= 0) {
            beyeahException.fail("afterSaleId is required");
        }
        beyeahAfterSale afterSale = beyeahAfterSaleMapper.selectByPrimaryKey(afterSaleId);
        if (afterSale == null) {
            beyeahException.fail("after-sale not found");
        }
        return afterSale;
    }

    private List<AfterSaleVO> buildVOList(List<beyeahAfterSale> list) {
        List<AfterSaleVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        for (beyeahAfterSale afterSale : list) {
            beyeahRefund refund = beyeahRefundMapper.selectByAfterSaleId(afterSale.getAfterSaleId());
            result.add(toVO(afterSale, refund));
        }
        return result;
    }

    private AfterSaleVO toVO(beyeahAfterSale afterSale, beyeahRefund refund) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setAfterSaleId(afterSale.getAfterSaleId());
        vo.setAfterSaleNo(afterSale.getAfterSaleNo());
        vo.setOrderNo(afterSale.getOrderNo());
        vo.setOrderItemId(afterSale.getOrderItemId());
        vo.setGoodsId(afterSale.getGoodsId());
        vo.setGoodsName(afterSale.getGoodsName());
        vo.setRefundAmount(afterSale.getRefundAmount());
        vo.setReason(afterSale.getReason());
        vo.setDescription(afterSale.getDescription());
        vo.setRejectReason(afterSale.getRejectReason());
        vo.setAfterSaleStatus(afterSale.getAfterSaleStatus());
        vo.setAfterSaleStatusString(AfterSaleStatusEnum.getByStatus(afterSale.getAfterSaleStatus() == null ? 0 : afterSale.getAfterSaleStatus()).getName());
        vo.setRefundNo(afterSale.getRefundNo());
        vo.setHandleTime(afterSale.getHandleTime());
        vo.setCreateTime(afterSale.getCreateTime());
        vo.setUpdateTime(afterSale.getUpdateTime());
        if (refund != null) {
            vo.setRefundStatus(refund.getRefundStatus());
            vo.setRefundStatusString(RefundStatusEnum.getByStatus(refund.getRefundStatus() == null ? 0 : refund.getRefundStatus()).getName());
            vo.setFailReason(refund.getFailReason());
            vo.setRefundSuccessTime(refund.getSuccessTime());
        } else {
            vo.setRefundStatus((byte) RefundStatusEnum.PENDING.getStatus());
            vo.setRefundStatusString("未发起");
            vo.setFailReason("");
        }
        return vo;
    }

    private String normalizeText(String raw, int maxLength, String fieldName) {
        if (!StringUtils.hasText(raw)) {
            beyeahException.fail(fieldName + " is required");
        }
        String value = raw.trim();
        if (value.length() > maxLength) {
            beyeahException.fail(fieldName + " is too long");
        }
        return sanitize(value);
    }

    private String normalizeOptionalText(String raw, int maxLength) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String value = raw.trim();
        if (value.length() > maxLength) {
            beyeahException.fail("description is too long");
        }
        return sanitize(value);
    }

    private String sanitize(String value) {
        return value.replace("<", "&lt;").replace(">", "&gt;");
    }

    private String genAfterSaleNo() {
        return "AS" + NumberUtil.genOrderNo();
    }

    private String genRefundNo() {
        return "RF" + NumberUtil.genOrderNo();
    }

    private String genRefundIdempotencyKey(String afterSaleNo) {
        return "refund-" + afterSaleNo;
    }
}
