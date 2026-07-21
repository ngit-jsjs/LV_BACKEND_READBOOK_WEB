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
    ALREADY_AUTHENTICATED(1013,"Tài khoản này đã được xác thực email từ trước.",HttpStatus.BAD_REQUEST),
    NAMEBOOK_EXISTED(1014,"Tên sách này đã tồn tại",HttpStatus.BAD_REQUEST),
    BOOK_NOT_EXISTED(1015,"Sách không tồn tại",HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BOOK(1007,"Không có quyền chỉnh sửa nội dung sách",HttpStatus.FORBIDDEN),
    FILE_UPLOAD_FAILED(1016, "Lỗi khi lưu file ảnh vào hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(1017, "Lỗi khi xóa file ảnh cũ khỏi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    
    EMAIL_BLANK(1018, "Email không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID_FORMAT(1019, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    USERNAME_BLANK(1020, "Tên tài khoản không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_BLANK(1021, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1022, "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ viết hoa, 1 chữ viết thường và 1 ký tự đặc biệt", HttpStatus.BAD_REQUEST),
    TITLE_BLANK(1023, "Tên sách không được để trống", HttpStatus.BAD_REQUEST),
    STATUS_NULL(1025, "Trạng thái sách không được để trống", HttpStatus.BAD_REQUEST),

    INVALID_CHAPTER_PRICE(1026, "Gía tiền không hợp lệ",HttpStatus.BAD_REQUEST),

    CHAPTER_LOCKED(1033, "Chương này cần phải mua để đọc", HttpStatus.FORBIDDEN),
    NOT_ENOUGH_COIN(1034, "Bạn không đủ xu để mở khóa chương này", HttpStatus.BAD_REQUEST),
    CHAPTER_ALREADY_UNLOCKED(1035, "Chương này đã được mở khóa", HttpStatus.BAD_REQUEST),
    CHAPTER_ALREADY_FREE(1036, "Chương này hoàn toàn miễn phí", HttpStatus.BAD_REQUEST),
    DESCRIPTION_BLANK(1037,"Mô tả không được để trống",HttpStatus.BAD_REQUEST),
    AUTHOR_BLANK(1038,"Tác giả không được để trống",HttpStatus.BAD_REQUEST),
    INVALID_PRICE(1039,"Giá không hợp lệ",HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1040, "Thể loại này đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1041, "Thể loại không tồn tại", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_BLANK(1042, "Tên thể loại không được để trống", HttpStatus.BAD_REQUEST),
    BOOKLIST_NOT_EXISTED(1050, "Danh sách theo dõi không tồn tại", HttpStatus.NOT_FOUND),
    BOOKLIST_NAME_BLANK(1051, "Tên danh sách theo dõi không được để trống", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_BOOKLIST(1052, "Bạn không có quyền chỉnh sửa danh sách theo dõi này", HttpStatus.FORBIDDEN),
    BOOKLIST_EXISTED(1053, "Danh sách theo dõi này đã tồn tại", HttpStatus.BAD_REQUEST),
    READING_HISTORY_NOT_EXISTED(1060, "Lịch sử đọc không tồn tại", HttpStatus.NOT_FOUND),
    BOOK_NOT_COMPLETED_YET(1061, "Bạn phải đọc xong tác phẩm này trước khi đánh giá", HttpStatus.BAD_REQUEST),
    INVALID_EPUB_FILE(1070, "File EPUB không hợp lệ hoặc không chứa chương nào hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FILE(1071, "File ảnh bìa không hợp lệ (chỉ chấp nhận định dạng png, jpg, jpeg, webp)", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1080, "Email của bạn chưa được xác thực. Vui lòng xác thực trước khi đăng nhập.", HttpStatus.FORBIDDEN),
    INVALID_OTP(1081, "Mã OTP không chính xác hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    CANNOT_HIDE_OR_DELETE_PURCHASED_BOOK(1082, "Không thể ẩn hoặc xóa sách này vì đã có người dùng mua chương", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_EPUB_PURCHASED_BOOK(1083, "Không thể cập nhật file EPUB của sách này vì đã có người dùng mua chương", HttpStatus.BAD_REQUEST),
    AUTHOR_NOT_EXISTED(1084, "Tác giả không tồn tại", HttpStatus.NOT_FOUND),
    PUBLISHER_NOT_EXISTED(1085, "Nhà xuất bản không tồn tại", HttpStatus.NOT_FOUND),
    AUTHOR_NAME_BLANK(1086, "Tên tác giả không được để trống", HttpStatus.BAD_REQUEST),
    PUBLISHER_NAME_BLANK(1087, "Tên nhà xuất bản không được để trống", HttpStatus.BAD_REQUEST),
    AUTHOR_EXISTED(1088, "Tác giả này đã tồn tại", HttpStatus.BAD_REQUEST),
    PUBLISHER_EXISTED(1089, "Nhà xuất bản này đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_BANNED(1090, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.", HttpStatus.FORBIDDEN),
    BOOK_ID_NULL(1043, "ID sách không được để trống", HttpStatus.BAD_REQUEST),
    CHAPTER_ID_NULL(1044, "ID chương không được để trống", HttpStatus.BAD_REQUEST),
    RATING_NULL(1045, "Điểm đánh giá không được để trống", HttpStatus.BAD_REQUEST),
    RATING_MIN(1046, "Điểm đánh giá thấp nhất là 1", HttpStatus.BAD_REQUEST),
    RATING_MAX(1047, "Điểm đánh giá cao nhất là 5", HttpStatus.BAD_REQUEST),
    CHAPTER_NUMBER_NULL(1048, "Số thứ tự chương không được để trống", HttpStatus.BAD_REQUEST),
    TITLE_INVALID(1049, "Tên sách không được vượt quá 255 ký tự", HttpStatus.BAD_REQUEST),
    EPUB_FILE_EMPTY(1098, "File EPUB không được để trống", HttpStatus.BAD_REQUEST),
    EPUB_FILE_INVALID_FORMAT(1099, "Chỉ chấp nhận file định dạng .epub", HttpStatus.BAD_REQUEST),
    PLAN_NOT_EXISTED(1091, "Gói xu không tồn tại", HttpStatus.NOT_FOUND),
    RATING_NOT_EXISTED(1092, "Đánh giá không tồn tại", HttpStatus.NOT_FOUND),
    RECOMMENDER_TRAIN_FAILED(1100, "Lỗi khi huấn luyện hệ thống gợi ý", HttpStatus.INTERNAL_SERVER_ERROR),
    EPUB_PARSE_FAILED(1101, "Lỗi khi xử lý (parse) nội dung file EPUB", HttpStatus.INTERNAL_SERVER_ERROR),
    EPUB_CHAPTER_CONTENT_READ_FAILED(1102, "Không đọc được nội dung chương từ EPUB", HttpStatus.INTERNAL_SERVER_ERROR),
    EPUB_FILE_MISSING(1103, "Không tìm thấy file EPUB", HttpStatus.BAD_REQUEST),
    EPUB_IMPORT_FAILED(1104, "Import file EPUB thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_CREATION_FAILED(1105, "Không thể tạo token xác thực", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PUBLISH_YEAR(1106, "Năm xuất bản không được lớn hơn năm hiện tại", HttpStatus.BAD_REQUEST),
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
