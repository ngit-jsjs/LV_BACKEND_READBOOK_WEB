package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.SubscriptionResponse;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.SubscriptionMapper;
import org.example.lv_backend.repository.SubscriptionRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    public List<SubscriptionResponse> getMySubscriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(subscriptionMapper::toSubscriptionResponse)
                .toList();
    }
}
