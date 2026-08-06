package org.example.lv_backend.dto.request.book;

import lombok.*;
import org.example.lv_backend.entity.BookStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFilterRequest {
    private BookStatus status;
    private String keyword;
    private String author;
    private String publisher;
    private Long year;
    private List<Long> categoryIds;
}
