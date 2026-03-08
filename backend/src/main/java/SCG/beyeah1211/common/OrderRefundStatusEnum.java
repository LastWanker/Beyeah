package SCG.beyeah1211.common;

public enum OrderRefundStatusEnum {
    DEFAULT(-9, "ERROR"),
    NONE(0, "无退款"),
    REFUNDING(1, "退款中"),
    REFUNDED(2, "已退款"),
    REFUND_REJECTED(-1, "退款拒绝");

    private int status;
    private String name;

    OrderRefundStatusEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public static OrderRefundStatusEnum getByStatus(int status) {
        for (OrderRefundStatusEnum value : OrderRefundStatusEnum.values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return DEFAULT;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
