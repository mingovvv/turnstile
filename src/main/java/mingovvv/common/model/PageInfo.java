package mingovvv.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record PageInfo(

    @Schema(description = "전체 페이지 수", example = "5")
    int totalPages,

    @Schema(description = "전체 데이터 개수", example = "20")
    long totalElements,

    @Schema(description = "현재 페이지 (1-based)", example = "1")
    int currentPage,

    @Schema(description = "현재 페이지의 데이터 개수", example = "10")
    int numberOfElements,

    @Schema(description = "한 페이지당 크기", example = "10")
    int size,

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    boolean isFirst,

    @Schema(description = "마지막 페이지 여부", example = "false")
    boolean isLast,

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    boolean hasPrevious,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext

) {
}
