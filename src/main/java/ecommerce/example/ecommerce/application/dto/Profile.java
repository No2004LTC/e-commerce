package ecommerce.example.ecommerce.application.dto;

import ecommerce.example.ecommerce.domain.user.User;

public record Profile(
    String id,
    String username,
    String email,
    String avatarUrl,
    String role
) {
    public static Profile fromDomain(User user) {
        return new Profile(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getRole().getName()
        );
    }
}