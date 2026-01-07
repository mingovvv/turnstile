package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.Seat;
import mingovvv.turnstile.domain.enums.SeatGrade;
import mingovvv.turnstile.domain.enums.SeatStatus;

/**
 * 좌석 응답
 */
@Getter
@Builder
public class SeatResponse {

    private String seatId;
    private String eventId;
    private String section;
    private int rowNum;
    private int seatNum;
    private SeatGrade grade;
    private String gradeDescription;
    private int price;
    private SeatStatus status;
    private String statusDescription;

    public static SeatResponse from(Seat seat) {
        return SeatResponse.builder()
                .seatId(seat.getSeatId())
                .eventId(seat.getEventId())
                .section(seat.getSection())
                .rowNum(seat.getRowNum())
                .seatNum(seat.getSeatNum())
                .grade(seat.getGrade())
                .gradeDescription(seat.getGrade().getDescription())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .statusDescription(seat.getStatus().getDescription())
                .build();
    }

    /**
     * Redis 선점 상태를 반영한 SeatResponse 생성
     */
    public static SeatResponse from(Seat seat, SeatStatus overrideStatus) {
        return SeatResponse.builder()
                .seatId(seat.getSeatId())
                .eventId(seat.getEventId())
                .section(seat.getSection())
                .rowNum(seat.getRowNum())
                .seatNum(seat.getSeatNum())
                .grade(seat.getGrade())
                .gradeDescription(seat.getGrade().getDescription())
                .price(seat.getPrice())
                .status(overrideStatus)
                .statusDescription(overrideStatus.getDescription())
                .build();
    }
}
