package org.example.lv_backend.repository;

import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.entity.RoleName;
import org.example.lv_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByName(String username);

    boolean existsByEmail(String email);

    Optional<User>findByName(String name);

    Page<User> findDistinctByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

}

