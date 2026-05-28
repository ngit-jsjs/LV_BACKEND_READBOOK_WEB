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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookCreationRequest {
    
    @NotBlank(message = "TITLE_BLANK")
    @Size(max = 255, message = "TITLE_INVALID")
    private String title;
    
    private String author;
    
    private String coverImageUrl;
    
    @NotNull(message = "STATUS_NULL")
    private BookStatus status;

    private String description;
    
    private String publisher;

    private Long year;
    
    private Set<Long> categoryIds;
}
