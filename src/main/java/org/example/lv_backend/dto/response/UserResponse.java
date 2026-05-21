package org.example.lv_backend.dto.response;

import org.example.lv_backend.entity.RoleName;

import java.util.Set;

public class UserResponse {

    private String userName;
    private String email;
    private String name;
    private Set<RoleName> roles;
}
