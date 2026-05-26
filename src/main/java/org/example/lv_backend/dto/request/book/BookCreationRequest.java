package org.example.lv_backend.dto.request.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.entity.Category;
import org.example.lv_backend.entity.User;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookCreationRequest {
    private String title;
    private String author;
    private String coverImageUrl;
    private BookStatus status;
    private String description;
    private String publisher;
    private Long year;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Long> categoryIds;
}
