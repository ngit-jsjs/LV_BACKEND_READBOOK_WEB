package org.example.lv_backend.dto.request.publisher;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PublisherCreationRequest {
    @NotBlank(message = "PUBLISHER_NAME_BLANK")
    private String name;
}
