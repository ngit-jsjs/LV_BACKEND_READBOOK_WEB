package org.example.lv_backend.dto.response.chapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListResponse {
    private Long id;
    private Long chapterNumber;
    private String title;
    private Boolean isFree;
    private Boolean isPublished;
    private BigDecimal price;
    private Boolean isLocked;
}
