package org.example.lv_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @OneToMany(mappedBy = "user")
    private List<Book> books = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<BookList> bookLists = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChapterUnlock> chapterUnlocks = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<InvalidatedToken> invalidatedTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "userA")
    private List<UserSim> userSimsA = new ArrayList<>();

    @OneToMany(mappedBy = "userB")
    private List<UserSim> userSimsB = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<ReadingHistory> readingHistories = new ArrayList<>();
}
