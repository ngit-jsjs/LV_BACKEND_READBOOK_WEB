package org.example.lv_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USERNAME_INVALID(1006,"Username phải có ít nhất 6 kí tự", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized Exceptions", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1007,"Không có quyền",HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1008, "Role không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1009,"User không tồn tại",HttpStatus.NOT_FOUND),
    REVIEW_EXISTED(1030, "Bạn đã đánh giá sách này rồi", HttpStatus.BAD_REQUEST),
    CHAPTER_NOT_EXISTED(1031, "Chương không tồn tại", HttpStatus.NOT_FOUND),
    CHAPTER_NUMBER_EXISTED(1032, "Số thứ tự chương đã tồn tại trong sách này", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1010,"User này đã tồn tại",HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1011,"Email này đã tồn tại",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1012,"Lỗi token",HttpStatus.UNAUTHORIZED),
    ALREADY_AUTHENTICATED(1013,"Người dùng đã đăng nhập",HttpStatus.BAD_REQUEST),
    NAMEBOOK_EXISTED(1014,"Tên sách này đã tồn tại",HttpStatus.BAD_REQUEST),
    BOOK_NOT_EXISTED(1015,"Sách không tồn tại",HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BOOK(1007,"Không có quyền chỉnh sửa nội dung sách",HttpStatus.FORBIDDEN),
    FILE_UPLOAD_FAILED(1016, "Lỗi khi lưu file ảnh vào hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(1017, "Lỗi khi xóa file ảnh cũ khỏi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    
    EMAIL_BLANK(1018, "Email không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID_FORMAT(1019, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    USERNAME_BLANK(1020, "Tên tài khoản không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_BLANK(1021, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1022, "Mật khẩu phải có ít nhất 6 ký tự", HttpStatus.BAD_REQUEST),
    TITLE_BLANK(1023, "Tên sách không được để trống", HttpStatus.BAD_REQUEST),
    STATUS_NULL(1025, "Trạng thái sách không được để trống", HttpStatus.BAD_REQUEST),

    INVALID_CHAPTER_PRICE(1026, "Gía tiền không hợp lệ",HttpStatus.BAD_REQUEST),


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
