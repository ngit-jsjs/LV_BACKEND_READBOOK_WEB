package org.example.lv_backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.lv_backend.util.SecurityUtil;
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
import org.example.lv_backend.repository.ChapterUnlockRepository;
import org.example.lv_backend.service.storage.EpubStorageService;
import org.example.lv_backend.service.storage.ImageStorageService;
import org.example.lv_backend.specification.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final ChapterUnlockRepository chapterUnlockRepository;

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



    public Page<BookResponse> getMyUploadBook(String keyword, int page, int size){
        String name = securityUtil.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Book> bookList=bookRepository.findByKeyword(name, keyword.trim(), pageable);

        Page<BookResponse> response = bookList.map(this::mapToBookResponse);
        return response;
    }



    public Page<BookResponse> searchBook(
            String keyword,
            String author,
            String publisher,
            Long year,
            List<Long> categoryIds,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        Specification<Book> spec = BookSpecification.filterBooks(
                BookStatus.AVAILABLE,
                keyword,
                author,
                publisher,
                year,
                categoryIds
        );

        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(this::mapToBookResponse);
    }



    @Transactional
    public BookResponse createBook (BookCreationRequest request, MultipartFile file){
        var title = request.getTitle();
        if(bookRepository.existsByTitle(title))
            throw new AppException(ErrorCode.NAMEBOOK_EXISTED);


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

            book = bookRepository.save(book);

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

        if (request.getStatus() == BookStatus.UNAVAILABLE && book.getStatus() == BookStatus.AVAILABLE) {
            if (chapterUnlockRepository.existsByChapter_Book_Id(id)) {
                throw new AppException(ErrorCode.CANNOT_HIDE_OR_DELETE_PURCHASED_BOOK);
            }
        }

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

        String newImageUrl=null;
        try {
            bookMapper.updateBook(book, request);
            if (file != null && !file.isEmpty()) {
                newImageUrl = imageStorageService.storeFile(file);
                book.setCoverImageUrl(newImageUrl);
                String oldImageUrl = book.getCoverImageUrl();
                imageStorageService.deleteFile(oldImageUrl);
            }
            book.setCategories(categories);
            BookResponse response = mapToBookResponse(bookRepository.save(book));
            return response;
        } catch (Exception e) {
            if (newImageUrl!= null) {
                imageStorageService.deleteFile(newImageUrl);
            }
            throw e;
        }
    }




    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (!securityUtil.isAdmin() && book.getStatus() == BookStatus.UNAVAILABLE) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }

        return mapToBookResponse(book);
    }



    @Transactional
    public void deleteBook(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (chapterUnlockRepository.existsByChapter_Book_Id(id)) {
            throw new AppException(ErrorCode.CANNOT_HIDE_OR_DELETE_PURCHASED_BOOK);
        }

        for (BookList bookList : book.getBookLists()) {
            bookList.getBooks().remove(book);
        }

        if (book.getChapters() != null && !book.getChapters().isEmpty()) {
            List<Chapter> chaptersToDelete = new ArrayList<>(book.getChapters());
            book.getChapters().clear();
            chapterRepository.deleteAll(chaptersToDelete);
        }

        String coverImageUrl = book.getCoverImageUrl();
        String epubStoragePath = book.getStoragePath();

        bookRepository.delete(book);

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
                        BookStatus.AVAILABLE,
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

        if (chapterUnlockRepository.existsByChapter_Book_Id(bookId)) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_EPUB_PURCHASED_BOOK);
        }

        if (book.getChapters() != null && !book.getChapters().isEmpty()) {
            List<Chapter> chaptersToDelete = new ArrayList<>(book.getChapters());
            book.getChapters().clear();
            chapterRepository.deleteAll(chaptersToDelete);
        }

        String oldEpubPath = book.getStoragePath();
        String storedPath = epubStorageService.storeFile(file);
        book.setStoragePath(storedPath);

        try {
            book = bookRepository.save(book);

            epubParserService.parseAndSaveChapters(book);

            if (oldEpubPath != null) {
                epubStorageService.deleteFile(oldEpubPath);
            }

            return mapToBookResponse(book);

        } catch (Exception e) {
            epubStorageService.deleteFile(storedPath);
            throw new RuntimeException("Import EPUB thất bại", e);
        }
    }


}
