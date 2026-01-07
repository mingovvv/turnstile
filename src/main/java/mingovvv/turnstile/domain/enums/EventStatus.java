package mingovvv.turnstile.domain.enums;

/**
 * 이벤트 상태
 */
public enum EventStatus {

    UPCOMING("예정"),
    OPEN("예매 진행중"),
    CLOSED("예매 종료"),
    SOLD_OUT("매진");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
