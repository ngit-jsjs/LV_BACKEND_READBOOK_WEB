package org.example.lv_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USERNAME_INVALID(1002,"Username must be at least 8 characters", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized Exceptions", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1007,"You don't have permission",HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1008, "Role không tồn tại", HttpStatus.NOT_FOUND)
    ;




    private int code;
    private String message;
    private HttpStatusCode statusCode;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode=statusCode;
    }


}
