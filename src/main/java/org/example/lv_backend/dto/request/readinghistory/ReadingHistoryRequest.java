package org.example.lv_backend.dto.request.readinghistory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingHistoryRequest {
    @NotNull(message = "bookId không được để trống")
    private Long bookId;

    @NotNull(message = "chapterId không được để trống")
    private Long chapterId;
}
