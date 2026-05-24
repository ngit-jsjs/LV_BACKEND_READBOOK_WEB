package org.example.lv_backend.dto.response.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.BookStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSummaryResponse {
    private Long id;
    private String title;
    private String author;
    private String coverImageUrl;
    private String slug;
    private BookStatus status;
    private Long totalChapters;
}
