package mingovvv.turnstile.domain;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.enums.SeatGrade;
import mingovvv.turnstile.domain.enums.SeatStatus;

/**
 * 좌석 도메인
 */
@Getter
@Builder
public class Seat {

    private String seatId;       // A-1, A-2, B-1 ...
    private String eventId;
    private String section;      // A, B, C 구역
    private int rowNum;
    private int seatNum;
    private SeatGrade grade;
    private int price;
    private SeatStatus status;

    public void lock() {
        this.status = SeatStatus.LOCKED;
    }

    public void reserve() {
        this.status = SeatStatus.RESERVED;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    public boolean isReserved() {
        return this.status == SeatStatus.RESERVED;
    }

    /**
     * 복합 키 생성 (eventId:seatId)
     */
    public String getCompositeKey() {
        return eventId + ":" + seatId;
    }
}
