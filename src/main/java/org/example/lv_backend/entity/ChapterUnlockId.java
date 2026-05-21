package org.example.lv_backend.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChapterUnlockId implements Serializable {
    private Integer user;
    private Long chapter;
}
