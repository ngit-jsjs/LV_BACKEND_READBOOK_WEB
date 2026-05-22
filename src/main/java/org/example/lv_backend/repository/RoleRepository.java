package org.example.lv_backend.repository;

import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.entity.Role;
import org.example.lv_backend.entity.RoleName;
import org.example.lv_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName (RoleName roleName);
    UserResponse userResponse (User user);
}