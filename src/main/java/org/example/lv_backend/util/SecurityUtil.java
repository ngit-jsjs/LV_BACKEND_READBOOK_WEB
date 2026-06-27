package org.example.lv_backend.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    public boolean hasAuthority(String role) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    public boolean isAdmin() {
        return hasAuthority("SCOPE_ADMIN");
    }

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }


}
