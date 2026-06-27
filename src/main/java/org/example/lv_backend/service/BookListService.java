package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.util.SecurityUtil;
import org.example.lv_backend.dto.request.booklist.BookListRequest;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.BookList;
import org.example.lv_backend.entity.Category;
import org.example.lv_backend.entity.User;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.BookListMapper;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.BookListRepository;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookListService {

    private final BookListRepository bookListRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookListMapper bookListMapper;
    private final BookMapper bookMapper;
    private final SecurityUtil securityUtil;


    private BookListResponse mapToBookListResponse(BookList bookList) {
        BookListResponse response = bookListMapper.toBookListResponse(bookList);
        if (bookList.getBooks() != null) {
            response.setBookIds(bookList.getBooks().stream()
                    .map(Book::getId)
                    .collect(Collectors.toSet()));
        } else {
            response.setBookIds(new HashSet<>());
        }
        return response;
    }


    private void verifyOwnership(BookList bookList) {
        String currentUsername = securityUtil.getCurrentUsername();
        if (bookList.getUser() == null || !currentUsername.equals(bookList.getUser().getName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_BOOKLIST);
        }
    }


    public BookListResponse createBookList(BookListRequest request) {
        String username = securityUtil.getCurrentUsername();
        
        if (bookListRepository.existsByNameAndUser_Name(request.getName(), username)) {
            throw new AppException(ErrorCode.BOOKLIST_EXISTED);
        }

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        BookList bookList = bookListMapper.toBookList(request);
        bookList.setUser(user);
        bookList.setCreatedAt(LocalDateTime.now());
        bookList.setBooks(new HashSet<>());

        return mapToBookListResponse(bookListRepository.save(bookList));
    }


    public List<BookListResponse> getMyBookLists() {
        String username = securityUtil.getCurrentUsername();
        return bookListRepository.findByUser_Name(username).stream()
                .map(this::mapToBookListResponse)
                .collect(Collectors.toList());
    }


    public BookListResponse getBookListById(Long id) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);
        return mapToBookListResponse(bookList);
    }



    public BookListResponse updateBookList(Long id, BookListRequest request) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);

        String username = securityUtil.getCurrentUsername();
        
        if (bookListRepository.existsByNameAndIdNotAndUser_Name(request.getName(), id, username)) {
            throw new AppException(ErrorCode.BOOKLIST_EXISTED);
        }

        bookListMapper.updateBookList(bookList, request);
        return mapToBookListResponse(bookListRepository.save(bookList));
    }


    public void deleteBookList(Long id) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);
        bookListRepository.delete(bookList);
    }


    @Transactional
    public BookListResponse addBookToBookList(Long id, Long bookId) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (bookList.getBooks() == null) {
            bookList.setBooks(new HashSet<>());
        }
        bookList.getBooks().add(book);

        return mapToBookListResponse(bookListRepository.save(bookList));
    }


    @Transactional
    public BookListResponse removeBookFromBookList(Long id, Long bookId) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (bookList.getBooks() != null) {
            bookList.getBooks().remove(book);
        }

        return mapToBookListResponse(bookListRepository.save(bookList));
    }


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


    public Page<BookResponse> getBooksInBookList(Long id, int page, int size) {
        BookList bookList = bookListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKLIST_NOT_EXISTED));
        verifyOwnership(bookList);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> booksPage = bookRepository.findByBookLists_Id(id, pageable);
        return booksPage.map(this::mapToBookResponse);
    }
}
