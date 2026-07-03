package org.example.lv_backend.dto.request.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {
    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá thấp nhất là 1")
    @Max(value = 5, message = "Điểm đánh giá cao nhất là 5")
    private Long ratings;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String comment;
}
