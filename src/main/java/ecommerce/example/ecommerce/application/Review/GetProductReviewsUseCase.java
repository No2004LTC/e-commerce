package ecommerce.example.ecommerce.application.Review; // ĐÚNG PACKAGE USECASE

import ecommerce.example.ecommerce.application.dto.ReviewResponseDTO; // IMPORT ĐÚNG TỪ PACKAGE DTO
import ecommerce.example.ecommerce.domain.Review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductReviewsUseCase {
    private final ReviewRepository reviewRepository;

    public List<ReviewResponseDTO> execute(String productId) {
        // Lấy danh sách từ DB thông qua Repository và map sang DTO nằm ở package khác
        return reviewRepository.findByProductId(productId).stream()
            .map(review -> new ReviewResponseDTO(
                review.getId().getValue(),
                review.getProductId(),
                review.getUserId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}