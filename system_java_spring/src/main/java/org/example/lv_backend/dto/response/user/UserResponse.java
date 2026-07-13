package org.example.lv_backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lv_backend.entity.RoleName;

import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String email;
    private String name;
    private Set<RoleName> roles;
    private BigDecimal amount;
    private boolean verified;
    private boolean active;
//    private List<PublishedBookResponse> publishedBooks;
//    private List<FavoriteBookResponse> favortiteBook;
}
