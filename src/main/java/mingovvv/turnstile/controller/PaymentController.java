package mingovvv.turnstile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mingovvv.turnstile.dto.request.PaymentRequest;
import mingovvv.turnstile.dto.response.PaymentResponse;
import mingovvv.turnstile.dto.response.ReservationResponse;
import mingovvv.turnstile.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 결제 API Controller (Mock)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청
     * POST /api/payments
     */
    @PostMapping("/payments")
    public ResponseEntity<Map<String, Object>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.processPayment(
                request.getEventId(),
                request.getSeatId(),
                request.getUserId()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", payment
        ));
    }

    /**
     * 결제 정보 조회
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable String paymentId) {
        PaymentResponse payment = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", payment
        ));
    }

    /**
     * 사용자 예약 목록 조회
     * GET /api/users/{userId}/reservations
     */
    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<Map<String, Object>> getUserReservations(@PathVariable String userId) {
        List<ReservationResponse> reservations = paymentService.getUserReservations(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations
        ));
    }

    /**
     * 예약 정보 조회
     * GET /api/reservations/{reservationId}
     */
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservation(@PathVariable String reservationId) {
        ReservationResponse reservation = paymentService.getReservation(reservationId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservation
        ));
    }
}
