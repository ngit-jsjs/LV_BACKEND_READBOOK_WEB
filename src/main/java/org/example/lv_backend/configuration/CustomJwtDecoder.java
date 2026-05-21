package org.example.lv_backend.configuration;

import lombok.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value{"$jwt.secretKey"}
    private String signerKey;


}
