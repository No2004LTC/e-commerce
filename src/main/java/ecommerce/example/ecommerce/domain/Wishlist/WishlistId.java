package ecommerce.example.ecommerce.domain.Wishlist;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class WishlistId implements Serializable {
    private String value;
}