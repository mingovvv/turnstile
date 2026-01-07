package mingovvv.turnstile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mingovvv.turnstile.dto.request.SeatLockRequest;
import mingovvv.turnstile.dto.response.SeatLockResponse;
import mingovvv.turnstile.dto.response.SeatResponse;
import mingovvv.turnstile.service.SeatService;
import mingovvv.turnstile.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 좌석 API Controller
 */
@RestController
@RequestMapping("/api/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final TokenService tokenService;

    /**
     * 좌석 목록 조회
     * GET /api/events/{eventId}/seats
     * <p>
     * 토큰 검증은 필수는 아니지만, 토큰이 있으면 더 정확한 상태를 제공
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSeats(
            @PathVariable String eventId,
            @RequestParam(required = false) String section) {

        List<SeatResponse> seats;
        if (section != null && !section.isBlank()) {
            seats = seatService.getSeatsBySection(eventId, section);
        } else {
            seats = seatService.getSeats(eventId);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seats
        ));
    }

    /**
     * 좌석 상세 조회
     * GET /api/events/{eventId}/seats/{seatId}
     */
    @GetMapping("/{seatId}")
    public ResponseEntity<Map<String, Object>> getSeat(
            @PathVariable String eventId,
            @PathVariable String seatId) {

        SeatResponse seat = seatService.getSeat(eventId, seatId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seat
        ));
    }

    /**
     * 좌석 선점
     * POST /api/events/{eventId}/seats/{seatId}/lock
     * <p>
     * 입장 토큰이 필요함 (헤더: X-Entry-Token)
     */
    @PostMapping("/{seatId}/lock")
    public ResponseEntity<Map<String, Object>> lockSeat(
            @PathVariable String eventId,
            @PathVariable String seatId,
            @RequestHeader(value = "X-Entry-Token", required = false) String token,
            @Valid @RequestBody SeatLockRequest request) {

        // 토큰 검증 (입장권이 있는 사용자만 선점 가능)
        tokenService.validateToken(eventId, request.getUserId(), token);

        SeatLockResponse lockResponse = seatService.lockSeat(eventId, seatId, request.getUserId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", lockResponse
        ));
    }

    /**
     * 좌석 선점 해제
     * DELETE /api/events/{eventId}/seats/{seatId}/lock?userId={userId}
     */
    @DeleteMapping("/{seatId}/lock")
    public ResponseEntity<Map<String, Object>> unlockSeat(
            @PathVariable String eventId,
            @PathVariable String seatId,
            @RequestParam String userId) {

        seatService.unlockSeat(eventId, seatId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "좌석 선점이 해제되었습니다."
        ));
    }
}
