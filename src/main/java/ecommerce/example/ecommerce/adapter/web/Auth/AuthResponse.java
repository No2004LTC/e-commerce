package ecommerce.example.ecommerce.adapter.web.Auth;

public class AuthResponse {
    private String id; 
    private String token;
    private String username;
    private String role;

    public AuthResponse(String id, String token, String username, String role) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}