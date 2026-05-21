package ecommerce.example.ecommerce.domain.customers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String id;
    private String phone;
    private String fullName;
    private String customerType; // NEW, POTENTIAL, LOYAL, VIP, SUPER_VIP, DIAMOND
    private BigDecimal totalSpent;
    private String notes;

    // Hàm lõi nghiệp vụ: Tự động tính % giảm giá mặc định dựa trên số tiền tích lũy
    public int getDiscountPercentage() {
        if (this.totalSpent == null) return 0;
        double spent = this.totalSpent.doubleValue();
        
        if (spent >= 50000000) return 25; // >= 50 Triệu
        if (spent >= 20000000) return 20; // >= 20 Triệu
        if (spent >= 10000000) return 15; // >= 10 Triệu
        if (spent >= 5000000)  return 10; // >= 5 Triệu
        if (spent >= 1000000)  return 5;  // >= 1 Triệu
        return 0;
    }

    // Hàm tự động cập nhật phân hạng dựa trên tổng tiền chi tiêu
    public void updateCustomerTypeBasedOnSpent() {
        if (this.totalSpent == null) {
            this.customerType = "NEW";
            return;
        }
        double spent = this.totalSpent.doubleValue();
        if (spent >= 50000000) this.customerType = "DIAMOND";
        else if (spent >= 20000000) this.customerType = "SUPER_VIP";
        else if (spent >= 10000000) this.customerType = "VIP";
        else if (spent >= 5000000)  this.customerType = "LOYAL";
        else if (spent >= 1000000)  this.customerType = "POTENTIAL";
        else this.customerType = "NEW";
    }
}