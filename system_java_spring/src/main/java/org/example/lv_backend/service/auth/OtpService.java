package org.example.lv_backend.service.auth;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.entity.OtpVerification;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.repository.OtpVerificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpVerificationRepository otpVerificationRepository;

    @Transactional
    public String generateAndSaveOtp(String email) {
        otpVerificationRepository.deleteByEmail(email);
        otpVerificationRepository.flush();
        String otp = String.format("%06d", new Random().nextInt(1000000));

        OtpVerification otpVerification = OtpVerification.builder()
                                            .email(email)
                                            .otpCode(otp)
                                            .expiryTime(LocalDateTime.now().plusMinutes(5))
                                            .build();
        otpVerificationRepository.save(otpVerification);
        return otp;
    }

    public boolean verifyOtp(String email, String code) {
        var otp=otpVerificationRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.INVALID_OTP));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return otp.getOtpCode().equals(code);
    }
}
