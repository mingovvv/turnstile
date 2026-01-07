package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.Event;
import mingovvv.turnstile.domain.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * 이벤트 응답
 */
@Getter
@Builder
public class EventResponse {

    private String eventId;
    private String name;
    private String venue;
    private LocalDateTime eventDate;
    private EventStatus status;
    private String statusDescription;
    private int maxConcurrentUsers;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .name(event.getName())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .status(event.getStatus())
                .statusDescription(event.getStatus().getDescription())
                .maxConcurrentUsers(event.getMaxConcurrentUsers())
                .build();
    }
}
