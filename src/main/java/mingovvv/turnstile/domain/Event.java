package mingovvv.turnstile.domain;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * 이벤트(공연) 도메인
 */
@Getter
@Builder
public class Event {

    private String eventId;
    private String name;
    private String venue;
    private LocalDateTime eventDate;
    private int maxConcurrentUsers;  // 동시 입장 가능 인원
    private EventStatus status;
    private LocalDateTime createdAt;

    public void updateStatus(EventStatus status) {
        this.status = status;
    }

    public boolean isOpen() {
        return this.status == EventStatus.OPEN;
    }
}
