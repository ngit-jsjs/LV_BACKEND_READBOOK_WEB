package org.example.lv_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String author;

    private String publisher;

    private String coverImageUrl;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BookStatus status;

    private Integer totalChapters;

    private Integer viewCount;

    private BigDecimal averageRating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)

    private List<Chapter> chapters = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "book")
    private List<Rating> ratings = new ArrayList<>();

    @ManyToMany(mappedBy = "books")
    private Set<BookList> bookLists = new HashSet<>();

    @OneToMany(mappedBy = "book")
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "chapter")
    private List<ReadingHistory> readingHistories = new ArrayList<>();


}
