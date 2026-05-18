package ecommerce.example.ecommerce.adapter.persistence.review;

import ecommerce.example.ecommerce.domain.Review.Review;
import ecommerce.example.ecommerce.domain.Review.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, ReviewId> {
    List<Review> findByProductId(String productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Optional<Double> getAverageRating(@Param("productId") String productId);
}