package org.example.lv_backend.dto.response.publisher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PublisherResponse {
    private Long id;
    private String name;
}
