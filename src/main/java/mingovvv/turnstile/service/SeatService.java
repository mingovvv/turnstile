package mingovvv.turnstile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Seat;
import mingovvv.turnstile.domain.enums.SeatStatus;
import mingovvv.turnstile.dto.response.SeatLockResponse;
import mingovvv.turnstile.dto.response.SeatResponse;
import mingovvv.turnstile.exception.ErrorCode;
import mingovvv.turnstile.exception.TurnstileException;
import mingovvv.turnstile.repository.memory.ReservationMemoryRepository;
import mingovvv.turnstile.repository.memory.SeatMemoryRepository;
import mingovvv.turnstile.repository.redis.SeatLockRedisRepository;
import mingovvv.turnstile.repository.redis.SeatLockRedisRepository.LockResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 좌석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final EventService eventService;
    private final SeatMemoryRepository seatRepository;
    private final SeatLockRedisRepository seatLockRepository;
    private final ReservationMemoryRepository reservationRepository;

    /**
     * 이벤트의 전체 좌석 목록 조회
     * Redis 선점 상태를 반영하여 반환
     */
    public List<SeatResponse> getSeats(String eventId) {
        eventService.validateEventOpen(eventId);

        List<Seat> seats = seatRepository.findByEventId(eventId);

        return seats.stream()
                .map(seat -> {
                    SeatStatus effectiveStatus = getEffectiveStatus(seat);
                    return SeatResponse.from(seat, effectiveStatus);
                })
                .collect(Collectors.toList());
    }

    /**
     * 구역별 좌석 목록 조회
     */
    public List<SeatResponse> getSeatsBySection(String eventId, String section) {
        eventService.validateEventOpen(eventId);

        List<Seat> seats = seatRepository.findByEventIdAndSection(eventId, section);

        return seats.stream()
                .map(seat -> {
                    SeatStatus effectiveStatus = getEffectiveStatus(seat);
                    return SeatResponse.from(seat, effectiveStatus);
                })
                .collect(Collectors.toList());
    }

    /**
     * 좌석 상세 조회
     */
    public SeatResponse getSeat(String eventId, String seatId) {
        Seat seat = findSeatOrThrow(eventId, seatId);
        SeatStatus effectiveStatus = getEffectiveStatus(seat);
        return SeatResponse.from(seat, effectiveStatus);
    }

    /**
     * 좌석 선점
     */
    public SeatLockResponse lockSeat(String eventId, String seatId, String userId) {
        eventService.validateEventOpen(eventId);
        Seat seat = findSeatOrThrow(eventId, seatId);

        // 이미 예약 완료된 좌석인지 확인
        if (seat.isReserved() || reservationRepository.existsBySeat(eventId, seatId)) {
            throw new TurnstileException(ErrorCode.SEAT_ALREADY_RESERVED, seatId);
        }

        // Redis를 통한 선점 시도
        LockResult result = seatLockRepository.tryLock(eventId, seatId, userId);

        switch (result) {
            case SUCCESS:
                log.info("Seat locked: eventId={}, seatId={}, userId={}", eventId, seatId, userId);
                return SeatLockResponse.success(eventId, seatId, userId, (int) seatLockRepository.getLockTtlSeconds());

            case ALREADY_OWNED:
                int remainingSeconds = (int) seatLockRepository.getRemainingTtl(eventId, seatId);
                log.info("Seat already owned by user: eventId={}, seatId={}, userId={}", eventId, seatId, userId);
                return SeatLockResponse.alreadyOwned(eventId, seatId, userId, remainingSeconds);

            case LOCKED:
                log.info("Seat already locked by another user: eventId={}, seatId={}", eventId, seatId);
                throw new TurnstileException(ErrorCode.SEAT_ALREADY_LOCKED, seatId);

            default:
                throw new TurnstileException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 좌석 선점 해제
     */
    public void unlockSeat(String eventId, String seatId, String userId) {
        findSeatOrThrow(eventId, seatId);

        boolean unlocked = seatLockRepository.unlock(eventId, seatId, userId);
        if (!unlocked) {
            throw new TurnstileException(ErrorCode.SEAT_NOT_LOCKED_BY_USER, seatId);
        }

        log.info("Seat unlocked: eventId={}, seatId={}, userId={}", eventId, seatId, userId);
    }

    /**
     * 좌석 선점 검증 (결제 시 사용)
     */
    public void validateSeatLock(String eventId, String seatId, String userId) {
        if (!seatLockRepository.isLockedBy(eventId, seatId, userId)) {
            if (seatLockRepository.isLocked(eventId, seatId)) {
                throw new TurnstileException(ErrorCode.SEAT_NOT_LOCKED_BY_USER, seatId);
            } else {
                throw new TurnstileException(ErrorCode.SEAT_LOCK_EXPIRED, seatId);
            }
        }
    }

    /**
     * 좌석 예약 완료 처리
     */
    public void reserveSeat(String eventId, String seatId) {
        Seat seat = findSeatOrThrow(eventId, seatId);
        seat.reserve();
        seatRepository.save(seat);

        // Redis 선점 락 해제
        seatLockRepository.forceUnlock(eventId, seatId);

        log.info("Seat reserved: eventId={}, seatId={}", eventId, seatId);
    }

    /**
     * 좌석 엔티티 조회 (내부용)
     */
    public Seat findSeatOrThrow(String eventId, String seatId) {
        return seatRepository.findById(eventId, seatId)
                .orElseThrow(() -> new TurnstileException(ErrorCode.SEAT_NOT_FOUND, seatId));
    }

    /**
     * 실제 유효한 좌석 상태 반환
     * Memory 상태 + Redis 선점 상태를 조합
     */
    private SeatStatus getEffectiveStatus(Seat seat) {
        // 이미 예약 완료된 좌석
        if (seat.isReserved()) {
            return SeatStatus.RESERVED;
        }

        // Redis에서 선점 중인지 확인
        if (seatLockRepository.isLocked(seat.getEventId(), seat.getSeatId())) {
            return SeatStatus.LOCKED;
        }

        return SeatStatus.AVAILABLE;
    }
}
