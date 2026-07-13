package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.author.AuthorCreationRequest;
import org.example.lv_backend.dto.response.author.AuthorResponse;
import org.example.lv_backend.entity.Author;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.AuthorMapper;
import org.example.lv_backend.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorResponse createAuthor(AuthorCreationRequest request) {
        if (authorRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AUTHOR_EXISTED);
        }

        Author author = authorMapper.toAuthor(request);
        return authorMapper.toAuthorResponse(authorRepository.save(author));
    }

    public Page<AuthorResponse> getAllAuthors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return authorRepository.findAll(pageable)
                .map(authorMapper::toAuthorResponse);
    }

    public AuthorResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        return authorMapper.toAuthorResponse(author);
    }

    public AuthorResponse updateAuthor(Long id, AuthorCreationRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));

        if (authorRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.AUTHOR_EXISTED);
        }

        authorMapper.updateAuthor(author, request);
        return authorMapper.toAuthorResponse(authorRepository.save(author));
    }

    public Page<AuthorResponse> searchAuthors(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return authorRepository.findDistinctByNameContainingIgnoreCase(keyword, pageable)
                .map(authorMapper::toAuthorResponse);
    }
}
