package ecommerce.example.ecommerce.domain.user;

import java.util.Objects;

/**
 * Value Object representing a user identifier.
 */
public final class UserId {
    private final Long id;

    public UserId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(id, userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
