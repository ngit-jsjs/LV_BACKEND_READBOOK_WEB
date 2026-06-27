package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.PlanResponse;
import org.example.lv_backend.entity.Plan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    PlanResponse toPlanResponse(Plan plan);
}
