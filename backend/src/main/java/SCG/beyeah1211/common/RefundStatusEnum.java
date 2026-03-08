package SCG.beyeah1211.common;

public enum RefundStatusEnum {
    DEFAULT(-9, "ERROR"),
    PENDING(0, "待执行"),
    SUCCESS(1, "成功"),
    FAILED(-1, "失败");

    private int status;
    private String name;

    RefundStatusEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public static RefundStatusEnum getByStatus(int status) {
        for (RefundStatusEnum value : RefundStatusEnum.values()) {
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
