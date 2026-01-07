package mingovvv.turnstile.repository.memory;

import mingovvv.turnstile.domain.Payment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 결제 In-Memory Repository (RDB 대체)
 */
@Repository
public class PaymentMemoryRepository {

    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    public Payment save(Payment payment) {
        store.put(payment.getPaymentId(), payment);
        return payment;
    }

    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(store.get(paymentId));
    }

    public Optional<Payment> findByReservationId(String reservationId) {
        return store.values().stream()
                .filter(p -> p.getReservationId().equals(reservationId))
                .findFirst();
    }

    public List<Payment> findByUserId(String userId) {
        return store.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Payment> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsById(String paymentId) {
        return store.containsKey(paymentId);
    }

    public void deleteById(String paymentId) {
        store.remove(paymentId);
    }

    public void deleteAll() {
        store.clear();
    }

    public long count() {
        return store.size();
    }
}
