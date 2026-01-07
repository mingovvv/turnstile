package mingovvv.turnstile.domain.enums;

/**
 * 좌석 등급
 */
public enum SeatGrade {

    VIP("VIP석", 200_000),
    R("R석", 150_000),
    S("S석", 100_000),
    A("A석", 70_000);

    private final String description;
    private final int defaultPrice;

    SeatGrade(String description, int defaultPrice) {
        this.description = description;
        this.defaultPrice = defaultPrice;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultPrice() {
        return defaultPrice;
    }
}
