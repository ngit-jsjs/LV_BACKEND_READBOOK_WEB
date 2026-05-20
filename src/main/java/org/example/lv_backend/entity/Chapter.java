package org.example.lv_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Integer chapterNumber;

        private String title;

        @Column(columnDefinition = "TEXT")
        private String content;

        private Boolean isFree;

        private Boolean isPublished;

        private BigDecimal price;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @ManyToOne
        @JoinColumn(name = "book_id")
        private Book book;
}
