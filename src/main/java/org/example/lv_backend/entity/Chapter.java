package org.example.lv_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        private Integer sectionIndex;

        private String fragmentId;

        private String nextAnchor;

//        @Lob
//        @Column(columnDefinition = "TEXT")
//        private String content;

        private Boolean isFree;


        private BigDecimal price;



        @CreationTimestamp
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "book_id")
        private Book book;

        @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<ChapterUnlock> chapterUnlocks = new ArrayList<>();


        public void markAsFree() {
                this.isFree = true;
                this.price = BigDecimal.ZERO;
        }

        public void setPrice(BigDecimal price) {

                if(price.compareTo(BigDecimal.ZERO) < 0){
                        throw new IllegalArgumentException();
                }

                this.price = price;
        }
}
