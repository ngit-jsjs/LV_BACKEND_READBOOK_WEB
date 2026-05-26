package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.request.book.BookCreationRequest;
import org.example.lv_backend.dto.request.user.UserCreationRequest;
import org.example.lv_backend.dto.request.user.UserUpdateRequest;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.dto.response.user.SearchingUserResponse;
import org.example.lv_backend.dto.response.user.UserResponse;
import org.example.lv_backend.entity.*;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;



    //sach viet boi user
    public Page<BookResponse> getMyPublishedBookList(int page, int size){
        var context= SecurityContextHolder.getContext();
        String name=context.getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);


        Page<Book> bookList = bookRepository.findByUserName(name,pageable);

        Page<BookResponse> response = bookList.map(bookMapper::toBookResponse);

        return response;
    }


    //list tim kiem theo user va
    public Page<BookResponse> searchBook (String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        List<BookStatus> statuses=List.of(BookStatus.ONGOING, BookStatus.COMPLETED);

        Page<Book> books =
                bookRepository
                        .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                                keyword,
                                keyword,
                                pageable,
                                statuses
                        );


        return books.map(bookMapper::toBookResponse);
    }


    public BookResponse createBook (BookCreationRequest request){

        //chac book nen them 1 bang publisher

        var title = request.getTitle();
//                            .trim()
//                            .toLowerCase()
//                            .replaceAll("[^\\p{L}\\p{N}\\s]", "")
//                            .replaceAll("\\s+", " ");
//        suy nghĩ về việc lưu thêm normalize title? để tránh nhập bậy bạ vẫn có thể trùng? hoặc xử lí ở frontend

        if(bookRepository.existsByTitle(title))
            throw new AppException(ErrorCode.BOOK_EXISTED);

        Book book= bookMapper.toBook(request);

        BookResponse response = bookMapper.toBookResponse(bookRepository.save(book));

        return response;

    }



    public BookResponse updateBook(Long id, BookCreationRequest request){
        Book book=bookRepository.findById(id).
                orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_EXISTED));
        bookMapper.updateBook(book, request);

        return bookMapper.toBookResponse(bookRepository.save(book));

    }

//    public UserResponse updateAuthor(Long id, UserUpdateRequest request){
//        User user=userRepository.findById(id).
//                orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
//        userMapper.updateUser(user, request);
//
//        return userMapper.toUserResponse(userRepository.save(user));
//
//    }


    public void deleteBook(Long id){
        bookRepository.deleteById(id);
    }




}
