package mingovvv.turnstile.domain.enums;

/**
 * 예약 상태
 */
public enum ReservationStatus {

    CONFIRMED("예약 확정"),
    CANCELLED("예약 취소");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
