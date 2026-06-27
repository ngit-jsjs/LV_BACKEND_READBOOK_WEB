package org.example.lv_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToMany
    @JoinTable(
            name = "book_list_items",
            joinColumns = @JoinColumn(name = "book_list_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    @Builder.Default
    private Set<Book> books = new HashSet<>();

}
