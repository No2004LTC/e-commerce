package ecommerce.example.ecommerce.adapter.web.cart;

/**
 * Request body cho thao tác thêm / cập nhật sản phẩm vào giỏ hàng.
 */
public record CartItemRequest(
    String productId,
    int quantity
) {}
