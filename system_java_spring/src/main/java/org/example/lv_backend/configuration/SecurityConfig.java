package org.example.lv_backend.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] POST_PUBLIC_ENDPOINTS = {
            "/user",
            "/auth/login",
            "/auth/introspect",
            "/auth/logout",
            "/user/create",
            "/auth/verify-email",
            "/auth/resend-otp",
            "/auth/forgot-password",
            "/auth/reset-password"
    };
    private final String[] GET_PUBLIC_ENDPOINTS = {"/user/search", "/user/{userId:[0-9]+}", "/uploads/**",
            "/books", "/books/search", "/books/*", "/chapters/**", "/categories", "/categories/**",
            "/api/payment/vnpay-return","/api/payment/vnpay-ipn","/api/plans", "/ratings/book/**"};


    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()

        );

        http.oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                        jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)

                                )
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );


        return http.build();
    }



}
