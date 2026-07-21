package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.PopularBook;
import org.example.lv_backend.entity.Recommendation;
import org.example.lv_backend.exception.AppException;
import org.example.lv_backend.exception.ErrorCode;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.entity.BookStatus;
import org.example.lv_backend.repository.BookRepository;
import org.example.lv_backend.repository.PopularBookRepository;
import org.example.lv_backend.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final BookRepository bookRepository;
    private final PopularBookRepository popularBookRepository;
    private final BookMapper bookMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.recommender.url:http://localhost:8000/recommend/train}")
    private String recommenderUrl;


    @Transactional(readOnly = true)
    public List<BookResponse> getRecommendationsForUser(Long userId) {
        log.info("Fetching recommendations for user id: {}", userId);
        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByScoreDesc(userId);
        
        if (recommendations.isEmpty()) {
            log.info("No recommendations found for user id: {}. Falling back to pre-calculated popular books.", userId);
            List<PopularBook> popularBooks = popularBookRepository.findAllWithBook();
            return popularBooks.stream()
                    .map(PopularBook::getBook)
                    .map(bookMapper::toBookResponse)
                    .collect(Collectors.toList());
        }
        
        Collections.shuffle(recommendations);
        
        return recommendations.stream()
                .limit(15)
                .map(Recommendation::getBook)
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void recalculatePopularBooks() {
        log.info("Recalculating popular books...");
        try {
            Page<Book> mostReadBooks = bookRepository.findMostReadBooks(
                    BookStatus.AVAILABLE,
                    PageRequest.of(0, 15)
            );
            List<Book> books = mostReadBooks.getContent();

            popularBookRepository.deleteAllInBatch();

            List<PopularBook> popularBooks = books.stream()
                    .map(book -> PopularBook.builder().book(book).build())
                    .collect(Collectors.toList());
            
            popularBookRepository.saveAll(popularBooks);
            log.info("Successfully updated popular books table with {} items.", popularBooks.size());
        } catch (Exception e) {
            log.error("Failed to recalculate popular books: ", e);
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    public void initPopularBooks() {
        if (popularBookRepository.count() == 0) {
            log.info("Popular books table is empty. Initializing on startup...");
            recalculatePopularBooks();
        }
    }


    public String triggerRecommenderCalculation() {
        try {
            log.info("Triggering recommender computation at: {}", recommenderUrl);
            String response = restTemplate.postForObject(recommenderUrl, null, String.class);
            
            // Recalculate popular books as well
            recalculatePopularBooks();
            
            return "Successfully updated recommendations!";
        } catch (Exception e) {
            throw new AppException(ErrorCode.RECOMMENDER_TRAIN_FAILED);
        }
    }


    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledRecalculation() {
        log.info("Starting scheduled recommendation update...");
        try {
            triggerRecommenderCalculation();
        } catch (Exception e) {
            log.error("Error during scheduled recommendation update: ", e);
        }
    }

}
