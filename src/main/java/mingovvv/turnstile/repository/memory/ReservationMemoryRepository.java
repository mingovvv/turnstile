package mingovvv.turnstile.repository.memory;

import mingovvv.turnstile.domain.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 예약 In-Memory Repository (RDB 대체)
 */
@Repository
public class ReservationMemoryRepository {

    private final Map<String, Reservation> store = new ConcurrentHashMap<>();

    // eventId:seatId → reservationId 매핑 (좌석 중복 예약 방지)
    private final Map<String, String> seatReservationIndex = new ConcurrentHashMap<>();

    public Reservation save(Reservation reservation) {
        store.put(reservation.getReservationId(), reservation);
        // 좌석-예약 인덱스 업데이트
        String seatKey = reservation.getSeatCompositeKey();
        seatReservationIndex.put(seatKey, reservation.getReservationId());
        return reservation;
    }

    public Optional<Reservation> findById(String reservationId) {
        return Optional.ofNullable(store.get(reservationId));
    }

    public Optional<Reservation> findBySeat(String eventId, String seatId) {
        String seatKey = eventId + ":" + seatId;
        String reservationId = seatReservationIndex.get(seatKey);
        if (reservationId == null) {
            return Optional.empty();
        }
        return findById(reservationId);
    }

    public List<Reservation> findByUserId(String userId) {
        return store.values().stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Reservation> findByEventId(String eventId) {
        return store.values().stream()
                .filter(r -> r.getEventId().equals(eventId))
                .collect(Collectors.toList());
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsBySeat(String eventId, String seatId) {
        String seatKey = eventId + ":" + seatId;
        return seatReservationIndex.containsKey(seatKey);
    }

    public void deleteById(String reservationId) {
        Reservation reservation = store.remove(reservationId);
        if (reservation != null) {
            seatReservationIndex.remove(reservation.getSeatCompositeKey());
        }
    }

    public void deleteAll() {
        store.clear();
        seatReservationIndex.clear();
    }

    public long count() {
        return store.size();
    }
}
