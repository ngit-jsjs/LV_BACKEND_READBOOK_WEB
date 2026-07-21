package org.example.lv_backend.service;

import org.springframework.transaction.annotation.Transactional;
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
import org.example.lv_backend.repository.AuthorRepository;
import org.example.lv_backend.repository.PublisherRepository;
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

import java.time.Year;
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
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;



    private Author resolveAuthor(Long authorId, String authorName) {
        if (authorId != null) {
            return authorRepository.findById(authorId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        }
        if (authorName != null && !authorName.trim().isEmpty()) {
            return authorRepository.findByName(authorName.trim())
                    .orElseGet(() -> authorRepository.save(
                            Author.builder()
                                    .name(authorName.trim())
                                    .build()
                    ));
        }
        return null;
    }

    private Publisher resolvePublisher(Long publisherId, String publisherName) {
        if (publisherId != null) {
            return publisherRepository.findById(publisherId)
                    .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_EXISTED));
        }
        if (publisherName != null && !publisherName.trim().isEmpty()) {
            return publisherRepository.findByName(publisherName.trim())
                    .orElseGet(() -> publisherRepository.save(
                            Publisher.builder()
                                    .name(publisherName.trim())
                                    .build()
                    ));
        }
        return null;
    }

    private void validatePublishYear(Long year) {
        if (year != null && year > Year.now().getValue()) {
            throw new AppException(ErrorCode.INVALID_PUBLISH_YEAR);
        }
    }



    @Transactional(readOnly = true)
    public Page<BookResponse> getMyUploadBook(
            BookStatus status,
            String keyword,
            String author,
            String publisher,
            Long year,
            List<Long> categoryIds,
            int page,
            int size){
        Long userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Specification<Book> spec = BookSpecification.filterBooks(
                status,
                keyword,
                author,
                publisher,
                year,
                categoryIds,
                userId
        );

        Page<Book> bookList = bookRepository.findAll(spec, pageable);
        return bookList.map(bookMapper::toBookResponse);
    }



    @Transactional(readOnly = true)
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
                categoryIds,
                null
        );

        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(bookMapper::toBookResponse);
    }



    @Transactional
    public BookResponse createBook (BookCreationRequest request, MultipartFile file){
        validatePublishYear(request.getYear());
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
            Author authorVal = resolveAuthor(request.getAuthorId(), request.getAuthor());
            book.setAuthor(authorVal);
            book.setAuthorName(authorVal != null ? authorVal.getName() : request.getAuthor());

            Publisher publisherVal = resolvePublisher(request.getPublisherId(), request.getPublisher());
            book.setPublisher(publisherVal);
            book.setPublisherName(publisherVal != null ? publisherVal.getName() : request.getPublisher());
            book.setCategories(categories);

            Long userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                book.setUser(user);
            }

            book = bookRepository.save(book);

            return bookMapper.toBookResponse(book);

        } catch (Exception e) {
            if (imageUrl != null) {
                imageStorageService.deleteFile(imageUrl);
            }
            throw e;
        }
    }


    @Transactional
    public BookResponse updateBook(Long id, BookCreationRequest request, MultipartFile file){
        validatePublishYear(request.getYear());
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

        String newImageUrl = null;
        try {
            bookMapper.updateBook(book, request);
            Author authorVal = resolveAuthor(request.getAuthorId(), request.getAuthor());
            book.setAuthor(authorVal);
            book.setAuthorName(authorVal != null ? authorVal.getName() : request.getAuthor());

            Publisher publisherVal = resolvePublisher(request.getPublisherId(), request.getPublisher());
            book.setPublisher(publisherVal);
            book.setPublisherName(publisherVal != null ? publisherVal.getName() : request.getPublisher());
            if (file != null && !file.isEmpty()) {
                String oldImageUrl = book.getCoverImageUrl(); // lấy URL cũ TRƯỚC khi ghi đè
                newImageUrl = imageStorageService.storeFile(file);
                book.setCoverImageUrl(newImageUrl);
                if (oldImageUrl != null) {
                    imageStorageService.deleteFile(oldImageUrl);
                }
            }
            book.setCategories(categories);
            return bookMapper.toBookResponse(bookRepository.save(book));
        } catch (Exception e) {
            if (newImageUrl != null) {
                imageStorageService.deleteFile(newImageUrl);
            }
            throw e;
        }
    }




    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (!securityUtil.isAdmin() && book.getStatus() == BookStatus.UNAVAILABLE) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }

        return bookMapper.toBookResponse(book);
    }



    @Transactional
    public void deleteBook(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (chapterUnlockRepository.existsByChapter_Book_Id(id)) {
            throw new AppException(ErrorCode.CANNOT_HIDE_OR_DELETE_PURCHASED_BOOK);
        }

        book.setStatus(BookStatus.UNAVAILABLE);
        bookRepository.save(book);
    }



    @Transactional(readOnly = true)
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
                .map(bookMapper::toBookResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getUnratedFinishedBooks(int page, int size) {
        Long userId = securityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> books = bookRepository.findUnratedFinishedBooks(userId, pageable);
        return books.map(bookMapper::toBookResponse);
    }




    @Transactional
    public BookResponse importEpub(Long bookId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.EPUB_FILE_MISSING);
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

            return bookMapper.toBookResponse(book);

        } catch (Exception e) {
            epubStorageService.deleteFile(storedPath);
            throw new AppException(ErrorCode.EPUB_IMPORT_FAILED);
        }
    }


}
