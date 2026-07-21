package org.example.lv_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "popular_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}
