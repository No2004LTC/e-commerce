package ecommerce.example.ecommerce.domain.Category;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class CategoryId implements Serializable {
    private String value;
}