package mingovvv.turnstile.repository.memory;

import mingovvv.turnstile.domain.Seat;
import mingovvv.turnstile.domain.enums.SeatStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 좌석 In-Memory Repository (RDB 대체)
 * Key: eventId:seatId (복합키)
 */
@Repository
public class SeatMemoryRepository {

    private final Map<String, Seat> store = new ConcurrentHashMap<>();

    public Seat save(Seat seat) {
        String key = compositeKey(seat.getEventId(), seat.getSeatId());
        store.put(key, seat);
        return seat;
    }

    public Optional<Seat> findById(String eventId, String seatId) {
        String key = compositeKey(eventId, seatId);
        return Optional.ofNullable(store.get(key));
    }

    public List<Seat> findByEventId(String eventId) {
        return store.values().stream()
                .filter(seat -> seat.getEventId().equals(eventId))
                .collect(Collectors.toList());
    }

    public List<Seat> findByEventIdAndStatus(String eventId, SeatStatus status) {
        return store.values().stream()
                .filter(seat -> seat.getEventId().equals(eventId))
                .filter(seat -> seat.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Seat> findByEventIdAndSection(String eventId, String section) {
        return store.values().stream()
                .filter(seat -> seat.getEventId().equals(eventId))
                .filter(seat -> seat.getSection().equals(section))
                .collect(Collectors.toList());
    }

    public List<Seat> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsById(String eventId, String seatId) {
        return store.containsKey(compositeKey(eventId, seatId));
    }

    public void deleteById(String eventId, String seatId) {
        store.remove(compositeKey(eventId, seatId));
    }

    public void deleteByEventId(String eventId) {
        store.entrySet().removeIf(entry -> entry.getValue().getEventId().equals(eventId));
    }

    public void deleteAll() {
        store.clear();
    }

    public long count() {
        return store.size();
    }

    public long countByEventId(String eventId) {
        return store.values().stream()
                .filter(seat -> seat.getEventId().equals(eventId))
                .count();
    }

    private String compositeKey(String eventId, String seatId) {
        return eventId + ":" + seatId;
    }
}
