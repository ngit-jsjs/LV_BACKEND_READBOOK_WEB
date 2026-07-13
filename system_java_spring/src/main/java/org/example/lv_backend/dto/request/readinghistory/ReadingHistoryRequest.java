package org.example.lv_backend.dto.request.readinghistory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingHistoryRequest {
    @NotNull(message = "BOOK_ID_NULL")
    private Long bookId;

    @NotNull(message = "CHAPTER_ID_NULL")
    private Long chapterId;

    private Boolean isCompleted;
}
