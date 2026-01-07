package mingovvv.turnstile.controller;

import lombok.RequiredArgsConstructor;
import mingovvv.turnstile.dto.response.EventResponse;
import mingovvv.turnstile.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 이벤트 API Controller
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 이벤트 목록 조회
     * GET /api/events
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", events
        ));
    }

    /**
     * 이벤트 상세 조회
     * GET /api/events/{eventId}
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getEvent(@PathVariable String eventId) {
        EventResponse event = eventService.getEvent(eventId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", event
        ));
    }
}
