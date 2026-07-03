package org.example.lv_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã Xác Thực Tài Khoản Đọc Sách");
            message.setText("Mã OTP của bạn là: " + otpCode + ". Mã này sẽ hết hạn trong vòng 5 phút.");
            mailSender.send(message);
        } catch (Exception e) {
        }
    }
    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Yêu Cầu Khôi Phục Mật Khẩu");
            message.setText("Mã OTP khôi phục mật khẩu của bạn là: " + otpCode + ". Mã này sẽ hết hạn trong vòng 5 phút.");
            mailSender.send(message);
        } catch (Exception e) {
        }
    }


}
