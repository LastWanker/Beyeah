package SCG.beyeah1211.common;

public enum AfterSaleStatusEnum {
    DEFAULT(-9, "ERROR"),
    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REFUNDING(2, "退款中"),
    REFUNDED(3, "已退款"),
    REJECTED(-1, "审核拒绝");

    private int status;
    private String name;

    AfterSaleStatusEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public static AfterSaleStatusEnum getByStatus(int status) {
        for (AfterSaleStatusEnum value : AfterSaleStatusEnum.values()) {
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
