package mingovvv.turnstile.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Event;
import mingovvv.turnstile.domain.Seat;
import mingovvv.turnstile.domain.enums.EventStatus;
import mingovvv.turnstile.domain.enums.SeatGrade;
import mingovvv.turnstile.domain.enums.SeatStatus;
import mingovvv.turnstile.repository.memory.EventMemoryRepository;
import mingovvv.turnstile.repository.memory.SeatMemoryRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 샘플 데이터 초기화
 * <p>
 * 애플리케이션 시작 시 테스트용 이벤트와 좌석 데이터를 생성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final EventMemoryRepository eventRepository;
    private final SeatMemoryRepository seatRepository;

    @PostConstruct
    public void init() {
        log.info("Initializing sample data...");

        // 이벤트 생성
        createEvents();

        // 좌석 생성
        createSeats();

        log.info("Sample data initialization completed. Events: {}, Seats: {}",
                eventRepository.count(), seatRepository.count());
    }

    private void createEvents() {
        // 이벤트 1: 2026 신년 콘서트
        Event event1 = Event.builder()
                .eventId("EVT001")
                .name("2026 신년 콘서트")
                .venue("올림픽 체조경기장")
                .eventDate(LocalDateTime.of(2026, 2, 1, 19, 0))
                .maxConcurrentUsers(100)
                .status(EventStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        eventRepository.save(event1);

        // 이벤트 2: 봄 뮤직 페스티벌
        Event event2 = Event.builder()
                .eventId("EVT002")
                .name("2026 봄 뮤직 페스티벌")
                .venue("잠실 종합운동장")
                .eventDate(LocalDateTime.of(2026, 4, 15, 18, 0))
                .maxConcurrentUsers(200)
                .status(EventStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .build();
        eventRepository.save(event2);

        log.info("Created {} events", eventRepository.count());
    }

    private void createSeats() {
        String eventId = "EVT001";

        // VIP 구역 (A섹션): 2열 × 5석 = 10석
        createSectionSeats(eventId, "A", SeatGrade.VIP, 2, 5);

        // R석 구역 (B섹션): 4열 × 5석 = 20석
        createSectionSeats(eventId, "B", SeatGrade.R, 4, 5);

        // S석 구역 (C섹션): 4열 × 5석 = 20석
        createSectionSeats(eventId, "C", SeatGrade.S, 4, 5);

        log.info("Created {} seats for event {}", seatRepository.countByEventId(eventId), eventId);
    }

    private void createSectionSeats(String eventId, String section, SeatGrade grade, int rows, int seatsPerRow) {
        for (int row = 1; row <= rows; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                String seatId = section + "-" + row + "-" + seatNum;

                Seat seat = Seat.builder()
                        .seatId(seatId)
                        .eventId(eventId)
                        .section(section)
                        .rowNum(row)
                        .seatNum(seatNum)
                        .grade(grade)
                        .price(grade.getDefaultPrice())
                        .status(SeatStatus.AVAILABLE)
                        .build();

                seatRepository.save(seat);
            }
        }
    }
}
