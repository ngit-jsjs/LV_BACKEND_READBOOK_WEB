package org.example.lv_backend.mapper;

import org.example.lv_backend.dto.request.publisher.PublisherCreationRequest;
import org.example.lv_backend.dto.response.publisher.PublisherResponse;
import org.example.lv_backend.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    @Mapping(target = "books", ignore = true)
    Publisher toPublisher(PublisherCreationRequest request);

    PublisherResponse toPublisherResponse(Publisher publisher);

    @Mapping(target = "books", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updatePublisher(@MappingTarget Publisher publisher, PublisherCreationRequest request);
}
