package ecommerce.example.ecommerce.domain.user;

import jakarta.persistence.*;

/**
 * Domain entity: Người dùng hệ thống (User / Branch).
 *
 * Mô hình phân cấp Multi-tenant:
 *  - User "cửa hàng lớn" (parent): parentId = null
 *  - User "chi nhánh nhỏ" (child): parentId = UserId của cửa hàng lớn quản lý
 *
 * Trường parentId sử dụng kiểu String (UUID dạng chuỗi) thay vì FK JPA
 * để tránh vòng lặp lazy-loading và giữ domain entity thuần túy.
 */
@Entity
@Table(name = "users")
public class User {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)"))
    private UserId id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * ID của cửa hàng/tài khoản cha trong mô hình phân cấp Multi-tenant.
     * - NULL  → Đây là cửa hàng lớn (root), không có cha.
     * - Value → Đây là chi nhánh con thuộc quyền quản lý của cửa hàng lớn có ID này.
     *
     * Dùng String UUID thuần để tránh phức tạp hóa mapping JPA tự tham chiếu.
     */
    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    public User() {}

    public User(UserId id, String username, String email, String password, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatarUrl = null;
        this.role = role;
        this.parentId = null;
        this.fullName = null;
        this.phone = null;
        this.address = null;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public UserId getId()                        { return id; }
    public void setId(UserId id)                 { this.id = id; }
    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public String getPassword()                  { return password; }
    public void setPassword(String password)     { this.password = password; }
    public String getAvatarUrl()                 { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }
    public Role getRole()                        { return role; }
    public void setRole(Role role)               { this.role = role; }
    public String getParentId()                  { return parentId; }
    public void setParentId(String parentId)     { this.parentId = parentId; }
    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }
    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }
    public String getAddress()                  { return address; }
    public void setAddress(String address)       { this.address = address; }

    /** Kiểm tra User này có phải là chi nhánh con không. */
    public boolean isBranch()  { return parentId != null && !parentId.isBlank(); }

    /** Kiểm tra User này có phải là cửa hàng lớn (root) không. */
    public boolean isRootShop() { return parentId == null || parentId.isBlank(); }
}