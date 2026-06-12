package org.example.lv_backend.dto.response.chapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponse {
    private Long id;
    private Long bookId;
    private Integer chapterNumber;
    Integer sectionIndex;
    private String title;
    private String content;
    private Boolean isFree;
    private Boolean isPublished;
    private BigDecimal price;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
