package org.example.lv_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USERNAME_INVALID(1002,"Username must be at least 8 characters", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized Exceptions", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1007,"Không có quyền",HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1008, "Role không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1009,"User không tồn tại",HttpStatus.NOT_FOUND),
    USER_EXISTED(1010,"User này đã tồn tại",HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1011,"Email này đã tồn tại",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1012,"Lỗi token",HttpStatus.UNAUTHORIZED),
    ALREADY_AUTHENTICATED(1013,"Người dùng đã đăng nhập",HttpStatus.BAD_REQUEST),
    BOOK_EXISTED(1014,"Book này đã tồn tại",HttpStatus.BAD_REQUEST),
    BOOK_NOT_EXISTED(1015,"Book không tồn tại",HttpStatus.NOT_FOUND)


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
