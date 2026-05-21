package org.example.lv_backend.entity;

import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSimId implements Serializable {
    private Integer userA;
    private Integer userB;
}
