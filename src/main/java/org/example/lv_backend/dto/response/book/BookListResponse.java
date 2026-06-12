package org.example.lv_backend.dto.response.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookListResponse {
    private Long id;
    private String name;
    private Set<Long> bookIds;
}
