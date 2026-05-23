package org.example.lv_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;


    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;


//    @CreationTimestamp
//    private Date createdAt;
//    @UpdateTimestamp
//    private Date updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
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
    private List<Recommendation> recommendations = new ArrayList<>();



    @OneToMany(mappedBy = "user")
    private List<ReadingHistory> readingHistories = new ArrayList<>();
}
