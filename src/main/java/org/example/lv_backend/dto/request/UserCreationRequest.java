package org.example.lv_backend.dto.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.bridge.IMessage;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreationRequest {
//    @Email
    private String email;
//    @Size(min=6, message = "USERNAME_INVALID")
    private String name;

//    @Size(min=6, message = "Mật khẩu không được ít hơn ")
    private String password;

}
