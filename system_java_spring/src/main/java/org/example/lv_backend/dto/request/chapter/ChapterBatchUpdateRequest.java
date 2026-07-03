package org.example.lv_backend.dto.request.chapter;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterBatchUpdateRequest {
    private List<Long> chapterIds;
    private Boolean isPublished;
    private Boolean isFree;
    private BigDecimal price;

}
