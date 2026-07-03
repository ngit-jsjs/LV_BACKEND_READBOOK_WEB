package org.example.lv_backend.dto.request.booklist;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookListRequest {
    @NotBlank(message = "BOOKLIST_NAME_BLANK")
    private String name;
}
