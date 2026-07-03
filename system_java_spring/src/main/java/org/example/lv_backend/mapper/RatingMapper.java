package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.rating.RatingRequest;
import org.example.lv_backend.dto.response.rating.RatingResponse;
import org.example.lv_backend.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userId", source = "user.id")
    RatingResponse toRatingResponse(Rating rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Rating toRating(RatingRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateRating(@MappingTarget Rating rating, RatingRequest request);
}
