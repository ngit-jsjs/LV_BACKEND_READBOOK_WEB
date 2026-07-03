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
public class ChapterUnlockResponse {
    private Long userId;
    private String userName;
    private String userEmail;

    private Long bookId;
    private String bookTitle;

    private Long chapterId;
    private Integer chapterNumber;
    private String chapterTitle;

    private BigDecimal price;
    private LocalDateTime createdAt;
}
