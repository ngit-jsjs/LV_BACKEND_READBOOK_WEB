package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.SubscriptionResponse;
import org.example.lv_backend.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "planId", source = "plan.id")
    @Mapping(target = "planName", source = "plan.name")
    @Mapping(target = "planPrice", source = "plan.price")
    @Mapping(target = "planAmount", source = "plan.amount")
    SubscriptionResponse toSubscriptionResponse(Subscription subscription);
}
