package org.example.lv_backend.dto.response.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookListSummaryResponse {
    private Long id;
    private String name;
    private List<BookSummaryResponse> books;
}
