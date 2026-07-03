package org.example.lv_backend.dto.response.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.BookStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String coverImageUrl;
//    private String slug;
    private BookStatus status;
    private Integer totalChapters;
    private BigDecimal averageRating;
    private String description;
    private String publisher;
    private Long year;
    private Set<String> categories;
    private String uploaderName; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
}

