package org.example.lv_backend.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.lv_backend.entity.Author;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.entity.Category;
import org.example.lv_backend.entity.Publisher;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(
            BookStatus status,
            String keyword,
            String author,
            String publisher,
            Long year,
            List<Long> categoryIds,
            Long userId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo status (ví dụ: AVAILABLE)
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 1.1 Lọc theo userId (được truyền từ Admin uploads)
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            // 2. Tìm theo từ khóa chung (Tìm theo Tiêu đề HOẶC Tác giả)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                Predicate authorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("authorName")), pattern);
                predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
            }

            // 3. Lọc theo tên Tác giả cụ thể
            if (author != null && !author.trim().isEmpty()) {
                String pattern = "%" + author.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("authorName")), pattern));
            }

            // 4. Lọc theo tên Nhà xuất bản cụ thể
            if (publisher != null && !publisher.trim().isEmpty()) {
                String pattern = "%" + publisher.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("publisherName")), pattern));
            }

            // 5. Lọc theo đúng năm xuất bản
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("year"), year));
            }

            // 6. Lọc theo danh sách nhiều thể loại (phép IN bảng categories)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                query.distinct(true);
                Join<Book, Category> categoryJoin = root.join("categories");
                predicates.add(categoryJoin.get("id").in(categoryIds));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
