package org.example.lv_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lv_backend.dto.response.book.BookResponse;
import org.example.lv_backend.entity.Book;
import org.example.lv_backend.entity.Category;
import org.example.lv_backend.entity.Recommendation;
import org.example.lv_backend.mapper.BookMapper;
import org.example.lv_backend.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final BookMapper bookMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.recommender.url:http://localhost:8000/recommend/train}")
    private String recommenderUrl;


    public List<BookResponse> getRecommendationsForUser(Long userId) {
        log.info("Fetching recommendations for user id: {}", userId);
        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByScoreDesc(userId);
        
        Collections.shuffle(recommendations);
        
        return recommendations.stream()
                .limit(15)
                .map(Recommendation::getBook)
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }


    public String triggerRecommenderCalculation() {
        try {
            log.info("Triggering recommender computation at: {}", recommenderUrl);
            String response = restTemplate.postForObject(recommenderUrl, null, String.class);
            return "Successfully updated recommendations!";
        } catch (Exception e) {
            throw new RuntimeException("Recommender system calculation failed: " + e.getMessage());
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
}
