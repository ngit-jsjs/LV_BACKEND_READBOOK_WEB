package org.example.lv_backend.service;

import jakarta.transaction.Transactional;
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
import org.example.lv_backend.repository.ChapterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ImageStorageService imageStorageService;
    private final SecurityUtil securityUtil;
    private final EpubStorageService epubStorageService;
    private final EpubParserService epubParserService;
    private final ChapterRepository chapterRepository;

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



    private boolean isUploader(Book book) {
        if (securityUtil.isAdmin()) {
            return true;
        }
        String currentUsername = securityUtil.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }
        return book.getUser() != null && currentUsername.equals(book.getUser().getName());
    }

    private void verifyUploader(Book book) {
        if (!isUploader(book)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOK);
        }
    }

    public Page<BookResponse> getMyUploadBook(String keyword, int page, int size){
        String name = securityUtil.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Book> bookList;
        if (keyword != null && !keyword.isBlank()) {
            bookList = bookRepository.findByUserNameAndKeyword(name, keyword.trim(), pageable);
        } else {
            bookList = bookRepository.findByUserName(name, pageable);
        }

        Page<BookResponse> response = bookList.map(this::mapToBookResponse);
        return response;
    }


    public Page<BookResponse> searchBook (String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String cleanKeyword = keyword != null ? keyword.trim() : "";

        Page<Book> books = bookRepository.findByStatusAndKeyword(
                BookStatus.PUBLISHED,
                cleanKeyword,
                pageable
        );

        return books.map(this::mapToBookResponse);
    }

    @Transactional
    public BookResponse createBook (BookCreationRequest request, MultipartFile file){
        var title = request.getTitle();
        if(bookRepository.existsByTitle(title))
            throw new AppException(ErrorCode.NAMEBOOK_EXISTED);

        User user = userRepository.findByName(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.getCategoryIds());
            if (categoryList.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
            }
            categories.addAll(categoryList);
        }

        String imageUrl = null;

        try {
            if (file != null && !file.isEmpty()) {
                imageUrl = imageStorageService.storeFile(file);
                request.setCoverImageUrl(imageUrl);
            }

            Book book = bookMapper.toBook(request);
            book.setCategories(categories);
            book.setUser(user);

            book = bookRepository.save(book);
            bookRepository.flush();

            return mapToBookResponse(book);

        } catch (Exception e) {
            if (imageUrl != null) {
                imageStorageService.deleteFile(imageUrl);
            }
            throw e;
        }
    }


    @Transactional
    public BookResponse updateBook(Long id, BookCreationRequest request, MultipartFile file){
        Book book=bookRepository.findById(id).
                orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_EXISTED));

        verifyUploader(book);

        var title = request.getTitle();
        if(bookRepository.existsByTitleAndIdNot(title, id))
            throw new AppException(ErrorCode.NAMEBOOK_EXISTED);

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.getCategoryIds());
            if (categoryList.size() != request.getCategoryIds().size()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
            }
            categories.addAll(categoryList);
        }

        String newImageUrl = null;
        String oldImageUrl = book.getCoverImageUrl();
        if (file != null && !file.isEmpty()) {
            newImageUrl = imageStorageService.storeFile(file);
            request.setCoverImageUrl(newImageUrl);
        } else {
            request.setCoverImageUrl(oldImageUrl);
        }

        try {
            bookMapper.updateBook(book, request);
            book.setCategories(categories);
            BookResponse response = mapToBookResponse(bookRepository.save(book));
            if (newImageUrl != null && oldImageUrl != null) {
                imageStorageService.deleteFile(oldImageUrl);
            }
            return response;
        } catch (Exception e) {
            if (newImageUrl != null) {
                imageStorageService.deleteFile(newImageUrl);
            }
            throw e;
        }
    }




    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (book.getStatus() == BookStatus.DRAFT && !isUploader(book)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }

        return mapToBookResponse(book);
    }

    @Transactional
    public void deleteBook(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        verifyUploader(book);

        String coverImageUrl = book.getCoverImageUrl();
        String epubStoragePath = book.getStoragePath();

        bookRepository.delete(book);
        bookRepository.flush(); 

        if (coverImageUrl != null) {
            imageStorageService.deleteFile(coverImageUrl);
        }

        if (epubStoragePath != null) {
            epubStorageService.deleteFile(epubStoragePath);
        }
    }



    public Page<BookResponse> getAllPublishedBooks(
            int page,
            int size
    )
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return bookRepository
                .findByStatus(
                        BookStatus.PUBLISHED,
                        pageable
                )
                .map(this::mapToBookResponse);
    }

    @Transactional
    public BookResponse importEpub(Long bookId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Không có file Epub");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        verifyUploader(book);

        if (book.getChapters() != null && !book.getChapters().isEmpty()) {
            chapterRepository.deleteAll(book.getChapters());
            book.getChapters().clear();
        }

        String storedPath = epubStorageService.storeFile(file);
        book.setStoragePath(storedPath);

        try {
            book = bookRepository.save(book);
            bookRepository.flush();

            epubParserService.parseAndSaveChapters(book);

            return mapToBookResponse(book);

        } catch (Exception e) {
            epubStorageService.deleteFile(storedPath);
            throw new RuntimeException("Import EPUB thất bại", e);
        }
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

}
