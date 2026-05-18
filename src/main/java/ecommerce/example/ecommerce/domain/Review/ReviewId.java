package ecommerce.example.ecommerce.domain.Review;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ReviewId implements Serializable {
    private String value;
}