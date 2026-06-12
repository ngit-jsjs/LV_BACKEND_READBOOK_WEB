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
public class ChapterCreationRequest {
    @NotNull(message = "CHAPTER_NUMBER_NULL")
    private Long chapterNumber;

    @NotBlank(message = "TITLE_BLANK")
    private String title;

    private String content;

    private Boolean isFree = false;

    private Boolean isPublished = false;

    private BigDecimal price = BigDecimal.ZERO;
}
