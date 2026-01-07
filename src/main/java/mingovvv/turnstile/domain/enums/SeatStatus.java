package mingovvv.turnstile.domain.enums;

/**
 * 좌석 상태
 */
public enum SeatStatus {

    AVAILABLE("선택 가능"),
    LOCKED("선점 중"),
    RESERVED("예약 완료");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
