package org.example.lv_backend.service.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.configuration.WebConfig;
import org.example.lv_backend.dto.request.auth.AuthenticationRequest;
import org.example.lv_backend.dto.request.auth.IntrospectRequest;
import org.example.lv_backend.dto.request.auth.LogoutRequest;
import org.example.lv_backend.dto.request.auth.VerifyOtpRequest;
import org.example.lv_backend.dto.request.auth.ResetPasswordRequest;
import org.example.lv_backend.dto.response.ApiResponse;
import org.example.lv_backend.dto.response.auth.AuthenticationResponse;
import org.example.lv_backend.dto.response.auth.IntrospectResponse;
import org.example.lv_backend.entity.InvalidatedToken;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.repository.InvalidatedTokenRepository;
import org.example.lv_backend.repository.OtpVerificationRepository;
import org.example.lv_backend.repository.RoleRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;
    private final WebConfig webConfig;
    private final OtpService otpService;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;


    @NonFinal
    @Value("${jwt.secretKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticationResponse(AuthenticationRequest authenticationRequest){

        var user = userRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );


        boolean authenticated = webConfig.passwordEncoder().matches(authenticationRequest.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);
//        log.info("Signkey {}",SIGNER_KEY);
        return AuthenticationResponse.builder()
                .authentication(true)
                .token(token)
                .build();

    }


    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        try{
            verifyToken(token);
        } catch (AppException e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
        return IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getName())
                .issuer("book_system.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("userId", user.getId())
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }

    }


    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role ->
                    stringJoiner.add(role.getRoleName().name())
            );
        }

        return stringJoiner.toString();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());
        String jit=signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        //kiem tra het han hay chua
        Date expityTime =signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier); //tra ve true hoac false
        if(!(verified && expityTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw  new AppException(ErrorCode.UNAUTHENTICATED);
        return  signedJWT;
    }


    public ApiResponse<String> verifyEmail(VerifyOtpRequest request){
        boolean isValid= otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setVerified(true);
        userRepository.save(user);

        otpVerificationRepository.deleteByEmail(request.getEmail());

        return ApiResponse.<String>builder()
                .result("Xác thực email thành công! Bạn hiện đã có thể đăng nhập.")
                .build();

    }

    public ApiResponse<String> resendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.isVerified()) {
            throw new AppException(ErrorCode.ALREADY_AUTHENTICATED);
        }

        String otp = otpService.generateAndSaveOtp(email);
        emailService.sendOtpEmail(email, otp);

        return ApiResponse.<String>builder()
                .result("Đã gửi lại mã OTP mới vào email của bạn.")
                .build();
    }

    public ApiResponse<String> forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = otpService.generateAndSaveOtp(email);
        emailService.sendForgotPasswordEmail(email, otp);

        return ApiResponse.<String>builder()
                .result("Mã OTP khôi phục mật khẩu đã được gửi đến email của bạn.")
                .build();
    }

    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(webConfig.passwordEncoder().encode(request.getNewPassword()));
        userRepository.save(user);

        otpVerificationRepository.deleteByEmail(request.getEmail());

        return ApiResponse.<String>builder()
                .result("Khôi phục mật khẩu thành công. Vui lòng đăng nhập lại với mật khẩu mới.")
                .build();
    }
}
