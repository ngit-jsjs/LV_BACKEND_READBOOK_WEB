package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.lv_backend.dto.response.book.BookListResponse;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.mapper.BookListMapper;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.BookListRepository;
import org.example.lv_backend.repository.BookRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookListService {
    //list sach yeu thich

//    private final BookListMapper bookMapper;
//    private final BookListRepository bookRepository;
//    public BookListResponse getMyFavoriteBookList(){
//        var context= SecurityContextHolder.getContext();
//        String name=context.getAuthentication().getName();
//
//        List<Book> bookList = bookRepository.findByUserName(name);
//
//        List<BookResponse> bookResponseList = bookMapper.toPublishedBookList(bookList);
//
//        BookListResponse response = new BookListResponse();
//        response.setBooks(bookResponseList);
//
//        return response;
//    }
}
