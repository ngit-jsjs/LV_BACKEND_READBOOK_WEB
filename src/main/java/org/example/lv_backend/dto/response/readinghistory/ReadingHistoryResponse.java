package org.example.lv_backend.dto.response.readinghistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadingHistoryResponse {
    private Long id;

    private Long bookId;

    private Long lastChapterId;
    private Long lastChapterNumber;
    private String lastChapterTitle;

    private String bookTitle;
    private String bookAuthor;
    private String coverImageUrl;

    private LocalDateTime updatedAt;
}
