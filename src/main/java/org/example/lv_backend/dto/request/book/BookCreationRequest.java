package org.example.lv_backend.dto.request.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.BookStatus;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookCreationRequest {
    private String title;
    private String coverImageUrl;
    private BookStatus status;
    private String description;
    private String publisher;
    private Long year;
}
