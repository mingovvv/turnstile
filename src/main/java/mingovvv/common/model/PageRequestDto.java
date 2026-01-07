package mingovvv.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequestDto(

    @Schema(description = "페이지 번호 (1-based)", defaultValue = "1")
    int page,

    @Schema(description = "페이지 크기", defaultValue = "10")
    int size

) {
    // 기본 값 설정
    public PageRequestDto {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
    }

    // 스프링의 Pageable 객체로 변환
    public Pageable toPageable() {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page - 1, size, sort);
    }

}
