package org.example.lv_backend.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryCreationRequest {
    @NotBlank(message = "CATEGORY_NAME_BLANK")
    private String name;

    private String description;
}
