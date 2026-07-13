package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.publisher.PublisherCreationRequest;
import org.example.lv_backend.dto.response.publisher.PublisherResponse;
import org.example.lv_backend.entity.Publisher;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.PublisherMapper;
import org.example.lv_backend.repository.PublisherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    public PublisherResponse createPublisher(PublisherCreationRequest request) {
        if (publisherRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PUBLISHER_EXISTED);
        }

        Publisher publisher = publisherMapper.toPublisher(request);
        return publisherMapper.toPublisherResponse(publisherRepository.save(publisher));
    }

    public Page<PublisherResponse> getAllPublishers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return publisherRepository.findAll(pageable)
                .map(publisherMapper::toPublisherResponse);
    }

    public PublisherResponse getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_EXISTED));
        return publisherMapper.toPublisherResponse(publisher);
    }

    public PublisherResponse updatePublisher(Long id, PublisherCreationRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_EXISTED));

        if (publisherRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.PUBLISHER_EXISTED);
        }

        publisherMapper.updatePublisher(publisher, request);
        return publisherMapper.toPublisherResponse(publisherRepository.save(publisher));
    }

    public Page<PublisherResponse> searchPublishers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return publisherRepository.findDistinctByNameContainingIgnoreCase(keyword, pageable)
                .map(publisherMapper::toPublisherResponse);
    }
}
