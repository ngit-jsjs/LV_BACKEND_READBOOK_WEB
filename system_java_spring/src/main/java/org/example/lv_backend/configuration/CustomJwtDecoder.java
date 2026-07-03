package org.example.lv_backend.configuration;

import com.nimbusds.jose.JOSEException;
import org.example.lv_backend.dto.request.auth.IntrospectRequest;
import org.example.lv_backend.service.auth.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value ("${jwt.secretKey}")

    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService ;
    private NimbusJwtDecoder nimbusJwtDecoder = null ;

    @Override
    public Jwt decode (String token){
        try {
            authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build()
            );
        } catch (JOSEException|ParseException e) {
            throw new RuntimeException(e.getMessage() );
    }
        if(Objects.isNull(nimbusJwtDecoder )){
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(),"HS512");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}
