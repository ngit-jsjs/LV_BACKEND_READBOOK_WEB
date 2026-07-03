package org.example.lv_backend.dto.response.rating;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    private Long id;
    private Long ratings;
    private String comment;
    private LocalDateTime createdAt;
    private String userName;
    private Long userId;
}
