package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.configuration.SecurityUtil;
import org.example.lv_backend.dto.request.book.BookCreationRequest;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.*;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.CategoryRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SecurityUtil securityUtil;

    private BookResponse mapToBookResponse(Book book) {
        BookResponse response = bookMapper.toBookResponse(book);
        if (book.getCategories() != null) {
            response.setCategories(book.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet()));
        } else {
            response.setCategories(new HashSet<>());
        }
        return response;
    }



    public Page<BookResponse> getMyBook(int page, int size){
        var context= SecurityContextHolder.getContext();
        String name=context.getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> bookList = bookRepository.findByUserName(name,pageable);

        Page<BookResponse> response = bookList.map(this::mapToBookResponse);
        return response;
    }


    public Page<BookResponse> searchBook (String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> books =
                bookRepository
                        .findByStatusAndTitleContainingIgnoreCaseOrStatusAndAuthorContainingIgnoreCase(
                                BookStatus.PUBLISHED,
                                keyword,
                                BookStatus.PUBLISHED,
                                keyword,
                                pageable
                        );


        return books.map(this::mapToBookResponse);
    }


    public BookResponse createBook (BookCreationRequest request, MultipartFile file){

        //chac book nen them 1 bang publisher

        String imageUrl = fileStorageService.storeFile(file);
        if (imageUrl != null) {
            request.setCoverImageUrl(imageUrl);
        }

        var title = request.getTitle();

        if(bookRepository.existsByTitle(title))
            throw new AppException(ErrorCode.NAMEBOOK_EXISTED);


        User user = userRepository.findByName(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.getCategoryIds());
            categories.addAll(categoryList);
        }

        Book book = bookMapper.toBook(request);

        book.setCategories(categories);
        book.setUser(user);

        BookResponse response = mapToBookResponse(bookRepository.save(book));

        return response;

    }


    public BookResponse updateBook(Long id, BookCreationRequest request, MultipartFile file){
        Book book=bookRepository.findById(id).
                orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_EXISTED));

        String imageUrl = fileStorageService.storeFile(file);

        if (imageUrl != null) {
            fileStorageService.deleteFile(book.getCoverImageUrl());
            request.setCoverImageUrl(imageUrl);
        } else {
            request.setCoverImageUrl(book.getCoverImageUrl());
        }

        var title = request.getTitle();                          

        if(bookRepository.existsByTitleAndIdNot(title, id))
            throw new AppException(ErrorCode.NAMEBOOK_EXISTED);



        if (!book.getUser().getName().equals(securityUtil.getCurrentUsername()) && !securityUtil.isAdmin()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOK);
        }

        bookMapper.updateBook(book, request);


        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.getCategoryIds());
            categories.addAll(categoryList);
        }


        book.setCategories(categories);

        return mapToBookResponse(bookRepository.save(book));

    }




    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        boolean isOwner = book.getUser()
                .getName()
                .equals(securityUtil.getCurrentUsername());

        if (book.getStatus() == BookStatus.DRAFT
                && !isOwner
                && !securityUtil.isAdmin()) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }


        return mapToBookResponse(book);
    }

    public void deleteBook(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (!book.getUser().getName().equals(securityUtil.getCurrentUsername()) && !securityUtil.isAdmin()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOK);
        }

        fileStorageService.deleteFile(book.getCoverImageUrl());

        bookRepository.deleteById(id);
    }


    public Page<BookResponse> getPublishedBooks(
            Long id,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page, size);

        return bookRepository
                .findByUserIdAndStatus(
                        id,
                        BookStatus.PUBLISHED,
                        pageable
                )
                .map(this::mapToBookResponse);
    }


}
