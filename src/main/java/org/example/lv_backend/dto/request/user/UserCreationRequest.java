package org.example.lv_backend.dto.request.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreationRequest {
    @NotBlank(message = "EMAIL_BLANK")
    @Email(message = "EMAIL_INVALID_FORMAT")
    private String email;
    
    @NotBlank(message = "USERNAME_BLANK")
    @Size(min = 6, max = 50, message = "USERNAME_INVALID")
    private String name;

    @NotBlank(message = "PASSWORD_BLANK")
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

}
