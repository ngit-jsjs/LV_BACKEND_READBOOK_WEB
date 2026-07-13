package org.example.lv_backend.dto.request.author;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AuthorCreationRequest {
    @NotBlank(message = "AUTHOR_NAME_BLANK")
    private String name;
}
