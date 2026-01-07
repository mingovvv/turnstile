package mingovvv.turnstile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Event;
import mingovvv.turnstile.dto.response.EventResponse;
import mingovvv.turnstile.exception.ErrorCode;
import mingovvv.turnstile.exception.TurnstileException;
import mingovvv.turnstile.repository.memory.EventMemoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 이벤트 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMemoryRepository eventRepository;

    /**
     * 이벤트 목록 조회
     */
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 상세 조회
     */
    public EventResponse getEvent(String eventId) {
        Event event = findEventOrThrow(eventId);
        return EventResponse.from(event);
    }

    /**
     * 이벤트 엔티티 조회 (내부용)
     */
    public Event findEventOrThrow(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new TurnstileException(ErrorCode.EVENT_NOT_FOUND, eventId));
    }

    /**
     * 이벤트가 예매 가능한지 확인
     */
    public void validateEventOpen(String eventId) {
        Event event = findEventOrThrow(eventId);
        if (!event.isOpen()) {
            throw new TurnstileException(ErrorCode.EVENT_NOT_OPEN, eventId);
        }
    }
}
