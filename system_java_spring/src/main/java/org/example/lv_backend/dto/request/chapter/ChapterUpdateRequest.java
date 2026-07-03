package org.example.lv_backend.dto.request.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterUpdateRequest {
    @NotNull(message = "CHAPTER_NUMBER_NULL")
    private Integer chapterNumber;

    @NotBlank(message = "TITLE_BLANK")
    private String title;

    private String content;

    @Builder.Default
    private Boolean isFree = false;

    @Builder.Default
    private Boolean isPublished = false;

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
}
