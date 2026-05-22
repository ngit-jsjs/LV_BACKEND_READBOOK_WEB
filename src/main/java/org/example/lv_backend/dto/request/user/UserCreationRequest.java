package org.example.lv_backend.dto.request.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
