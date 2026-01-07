package mingovvv.turnstile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Payment;
import mingovvv.turnstile.domain.Reservation;
import mingovvv.turnstile.domain.Seat;
import mingovvv.turnstile.domain.enums.PaymentStatus;
import mingovvv.turnstile.domain.enums.ReservationStatus;
import mingovvv.turnstile.dto.response.PaymentResponse;
import mingovvv.turnstile.dto.response.ReservationResponse;
import mingovvv.turnstile.exception.ErrorCode;
import mingovvv.turnstile.exception.TurnstileException;
import mingovvv.turnstile.repository.memory.PaymentMemoryRepository;
import mingovvv.turnstile.repository.memory.ReservationMemoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제 서비스 (Mock)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SeatService seatService;
    private final TokenService tokenService;
    private final PaymentMemoryRepository paymentRepository;
    private final ReservationMemoryRepository reservationRepository;

    private final Random random = new Random();

    // Mock 결제 성공률 (80%)
    private static final double SUCCESS_RATE = 0.8;

    /**
     * 결제 처리 (Mock)
     */
    public PaymentResponse processPayment(String eventId, String seatId, String userId) {
        // 좌석 선점 검증
        seatService.validateSeatLock(eventId, seatId, userId);

        // 좌석 정보 조회
        Seat seat = seatService.findSeatOrThrow(eventId, seatId);

        // 이미 예약된 좌석인지 확인
        if (reservationRepository.existsBySeat(eventId, seatId)) {
            throw new TurnstileException(ErrorCode.SEAT_ALREADY_RESERVED, seatId);
        }

        // Mock 결제 처리
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String reservationId = "RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int amount = seat.getPrice();

        boolean paymentSuccess = random.nextDouble() < SUCCESS_RATE;

        if (paymentSuccess) {
            // 결제 성공
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .reservationId(reservationId)
                    .amount(amount)
                    .status(PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationId(reservationId)
                    .eventId(eventId)
                    .seatId(seatId)
                    .userId(userId)
                    .paymentId(paymentId)
                    .amount(amount)
                    .status(ReservationStatus.CONFIRMED)
                    .confirmedAt(LocalDateTime.now())
                    .build();
            reservationRepository.save(reservation);

            // 좌석 상태 업데이트 (RESERVED) 및 Redis 락 해제
            seatService.reserveSeat(eventId, seatId);

            // 토큰 삭제 (더 이상 필요 없음)
            tokenService.deleteToken(eventId, userId);

            log.info("Payment success: paymentId={}, reservationId={}, userId={}, amount={}",
                    paymentId, reservationId, userId, amount);

            return PaymentResponse.from(payment);
        } else {
            // 결제 실패
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .reservationId(null)
                    .amount(amount)
                    .status(PaymentStatus.FAILED)
                    .paidAt(null)
                    .build();
            paymentRepository.save(payment);

            log.info("Payment failed: paymentId={}, userId={}, amount={}", paymentId, userId, amount);

            throw new TurnstileException(ErrorCode.PAYMENT_FAILED, paymentId);
        }
    }

    /**
     * 결제 정보 조회
     */
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new TurnstileException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));
        return PaymentResponse.from(payment);
    }

    /**
     * 사용자의 예약 목록 조회
     */
    public List<ReservationResponse> getUserReservations(String userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약 정보 조회
     */
    public ReservationResponse getReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new TurnstileException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));
        return ReservationResponse.from(reservation);
    }
}
