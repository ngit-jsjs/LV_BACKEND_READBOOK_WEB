package org.example.lv_backend.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.example.lv_backend.dto.request.book.BookFilterRequest;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Category;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(BookFilterRequest filter, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                // 1. Lọc theo status (ví dụ: AVAILABLE)
                if (filter.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
                }

                // 2. Tìm theo từ khóa chung (Tìm theo Tiêu đề HOẶC Tác giả)
                if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                    String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                    Predicate authorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.join("author").get("name")), pattern);
                    predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
                }

                // 3. Lọc theo tên Tác giả cụ thể
                if (filter.getAuthor() != null && !filter.getAuthor().trim().isEmpty()) {
                    String pattern = "%" + filter.getAuthor().trim().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("author").get("name")), pattern));
                }

                // 4. Lọc theo tên Nhà xuất bản cụ thể
                if (filter.getPublisher() != null && !filter.getPublisher().trim().isEmpty()) {
                    String pattern = "%" + filter.getPublisher().trim().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("publisher").get("name")), pattern));
                }

                // 5. Lọc theo đúng năm xuất bản
                if (filter.getYear() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("year"), filter.getYear()));
                }

                // 6. Lọc theo danh sách nhiều thể loại (phép IN bảng categories)
                if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                    query.distinct(true);
                    Join<Book, Category> categoryJoin = root.join("categories");
                    predicates.add(categoryJoin.get("id").in(filter.getCategoryIds()));
                }
            }

            // 7. Lọc theo userId (được truyền từ Admin/User uploads)
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
