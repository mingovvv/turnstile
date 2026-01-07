package mingovvv.turnstile.repository.memory;

import mingovvv.turnstile.domain.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이벤트 In-Memory Repository (RDB 대체)
 */
@Repository
public class EventMemoryRepository {

    private final Map<String, Event> store = new ConcurrentHashMap<>();

    public Event save(Event event) {
        store.put(event.getEventId(), event);
        return event;
    }

    public Optional<Event> findById(String eventId) {
        return Optional.ofNullable(store.get(eventId));
    }

    public List<Event> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsById(String eventId) {
        return store.containsKey(eventId);
    }

    public void deleteById(String eventId) {
        store.remove(eventId);
    }

    public void deleteAll() {
        store.clear();
    }

    public long count() {
        return store.size();
    }
}
