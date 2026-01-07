package mingovvv.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(

    @Schema(description = "실제 데이터 리스트")
    List<T> items,

    @Schema(description = "페이지네이션 정보")
    PageInfo pageInfo

) {

    public static <T> PageResponseDto<T> of(Page<T> page) {
        return new PageResponseDto<>(
            page.getContent(),
            new PageInfo(
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1, // 0-based -> 1-based 변환
                page.getNumberOfElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.hasPrevious(),
                page.hasNext()
            )
        );
    }

}
