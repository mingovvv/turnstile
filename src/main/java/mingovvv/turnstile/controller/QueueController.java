package mingovvv.turnstile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.dto.request.QueueEntryRequest;
import mingovvv.turnstile.dto.response.QueueStatusResponse;
import mingovvv.turnstile.service.QueueService;
import mingovvv.turnstile.sse.QueueSseEmitterRegistry;
import mingovvv.turnstile.sse.QueueSseEvent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * 대기열 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/events/{eventId}/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final QueueSseEmitterRegistry sseRegistry;

    /**
     * 대기열 진입
     * POST /api/events/{eventId}/queue/enter
     */
    @PostMapping("/enter")
    public ResponseEntity<Map<String, Object>> enterQueue(
            @PathVariable String eventId,
            @Valid @RequestBody QueueEntryRequest request) {

        QueueStatusResponse status = queueService.enterQueue(eventId, request.getUserId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", status
        ));
    }

    /**
     * 대기열 상태 SSE 구독
     * GET /api/events/{eventId}/queue/subscribe?userId={userId}
     * <p>
     * 클라이언트는 이 엔드포인트로 SSE 연결을 맺고,
     * 서버에서 토큰 발급 시 실시간으로 알림을 받습니다.
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String eventId,
            @RequestParam String userId) {

        log.info("SSE subscribe: eventId={}, userId={}", eventId, userId);

        // SSE Emitter 등록
        SseEmitter emitter = sseRegistry.register(eventId, userId);

        // 연결 직후 현재 상태 전송
        try {
            QueueStatusResponse status = queueService.getQueueStatus(eventId, userId);

            if (status.isCanEnter()) {
                // 이미 토큰이 있으면 바로 알림
                QueueSseEvent event = QueueSseEvent.tokenIssued(eventId, userId, status.getToken());
                emitter.send(SseEmitter.event()
                        .name(event.getEventType())
                        .data(event));
            } else if (status.getPosition() >= 0) {
                // 대기 중이면 현재 순번 알림
                QueueSseEvent event = QueueSseEvent.queueUpdate(
                        eventId, userId, status.getPosition(),
                        status.getTotalWaiting(), status.getEstimatedWaitSeconds());
                emitter.send(SseEmitter.event()
                        .name(event.getEventType())
                        .data(event));
            }
        } catch (IOException e) {
            log.warn("Failed to send initial SSE: eventId={}, userId={}", eventId, userId);
        }

        return emitter;
    }

    /**
     * 대기열 상태 조회 (폴링 방식 - 하위 호환용)
     * GET /api/events/{eventId}/queue/status?userId={userId}
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus(
            @PathVariable String eventId,
            @RequestParam String userId) {

        QueueStatusResponse status = queueService.getQueueStatus(eventId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", status
        ));
    }

    /**
     * 대기열 이탈
     * DELETE /api/events/{eventId}/queue/leave?userId={userId}
     */
    @DeleteMapping("/leave")
    public ResponseEntity<Map<String, Object>> leaveQueue(
            @PathVariable String eventId,
            @RequestParam String userId) {

        queueService.leaveQueue(eventId, userId);

        // SSE 연결 종료
        sseRegistry.complete(eventId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "대기열에서 이탈하였습니다."
        ));
    }

    /**
     * 대기열 통계 조회 (관리자용)
     * GET /api/events/{eventId}/queue/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQueueStats(@PathVariable String eventId) {
        long totalWaiting = queueService.getTotalWaiting(eventId);
        long sseConnections = sseRegistry.getEmitterCountByEventId(eventId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "eventId", eventId,
                        "totalWaiting", totalWaiting,
                        "sseConnections", sseConnections
                )
        ));
    }
}
