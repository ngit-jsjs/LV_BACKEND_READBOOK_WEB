package org.example.lv_backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.dto.response.book.BookListSummaryResponse;
import org.example.lv_backend.dto.response.book.BookSummaryResponse;
import org.example.lv_backend.entity.RoleName;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String email;
    private String name;
    private Set<RoleName> roles;
    private List<BookSummaryResponse> publishedBooks;
    private List<BookListSummaryResponse> bookLists;
}
