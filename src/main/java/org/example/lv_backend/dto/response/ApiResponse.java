package org.example.lv_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@JsonInclude(JsonInclude.Include.NON_NULL)
////loại bỏ object có properties null trong json
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse <T>{

    @Builder.Default
    private int code = 200;
    private String message;
    private T result;
}
