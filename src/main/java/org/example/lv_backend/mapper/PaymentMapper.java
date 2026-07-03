package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.response.PaymentResponse;
import org.example.lv_backend.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "planId", source = "plan.id")
    @Mapping(target = "planName", source = "plan.name")
    PaymentResponse toPaymentResponse(Payment payment);
}
