/**
 * 涓ヨ們澹版槑锛? * 寮€婧愮増鏈鍔″繀淇濈暀姝ゆ敞閲婂ご淇℃伅锛岃嫢鍒犻櫎鎴戞柟灏嗕繚鐣欐墍鏈夋硶寰嬭矗浠昏拷绌讹紒
 * 鏈郴缁熷凡鐢宠杞欢钁椾綔鏉冿紝鍙楀浗瀹剁増鏉冨眬鐭ヨ瘑浜ф潈浠ュ強鍥藉璁＄畻鏈鸿蒋浠惰憲浣滄潈淇濇姢锛? * 鍙甯稿垎浜拰瀛︿範婧愮爜锛屼笉寰楃敤浜庤繚娉曠姱缃椿鍔紝杩濊€呭繀绌讹紒
 * Copyright (c) 2019-2020 鍗佷笁 all rights reserved.
 * 鐗堟潈鎵€鏈夛紝渚垫潈蹇呯┒锛? */
package SCG.beyeah1211.controller.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 璁㈠崟鍒楄〃椤甸潰VO
 */
public class beyeahOrderListVO implements Serializable {

    private Long orderId;

    private String orderNo;

    private Integer totalPrice;

    private Byte payType;

    private Byte orderStatus;

    private String orderStatusString;

    private Byte refundStatus;

    private String refundStatusString;

    private String userAddress;

    private Date createTime;

    private List<beyeahOrderItemVO> beyeahOrderItemVOS;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Byte getPayType() {
        return payType;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public Byte getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Byte orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatusString() {
        return orderStatusString;
    }

    public void setOrderStatusString(String orderStatusString) {
        this.orderStatusString = orderStatusString;
    }

    public Byte getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(Byte refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getRefundStatusString() {
        return refundStatusString;
    }

    public void setRefundStatusString(String refundStatusString) {
        this.refundStatusString = refundStatusString;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<beyeahOrderItemVO> getbeyeahOrderItemVOS() {
        return beyeahOrderItemVOS;
    }

    public void setbeyeahOrderItemVOS(List<beyeahOrderItemVO> beyeahOrderItemVOS) {
        this.beyeahOrderItemVOS = beyeahOrderItemVOS;
    }
}

