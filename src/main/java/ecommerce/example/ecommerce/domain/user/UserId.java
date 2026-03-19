package ecommerce.example.ecommerce.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class UserId implements Serializable {
    
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private final UUID id;

    // Constructor mặc định cho JPA
    protected UserId() {
        this.id = null;
    }

    public UserId(UUID id) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    public UUID getId() {
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

    @Override
    public String toString() {
        return id != null ? id.toString() : null;
    }
}