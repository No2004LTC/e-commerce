🛒 E-commerce Backend - Clean Architecture
Hệ thống được xây dựng theo kiến trúc Clean Architecture & DDD, sử dụng Java 17, Spring Boot 3 và định danh UUID (UserId) cho thực thể User.

🔄 Luồng dữ liệu (Request Flow)
Web (Request) ➔ Controller ➔ Use Case ➔ Domain Entity ➔ Persistence Adapter ➔ Database

📂 Chi tiết chức năng từng thư mục
1. Tầng Domain (Vùng lõi nghiệp vụ)
user/User.java: Thực thể chính. Chứa thông tin User và các quy tắc nghiệp vụ cốt lõi.

user/UserId.java: Value Object. Định danh duy nhất cho User bằng UUID, đảm bảo Type-safety.

user/Role.java: Thực thể phân quyền (ADMIN, USER).

user/UserRepository.java: Interface quy định các hành động với dữ liệu (không quan tâm là DB nào).

2. Tầng Application (Điều hướng nghiệp vụ)
User/RegisterUserUseCase.java: Quy trình đăng ký (Check trùng ➔ Hash pass ➔ Tạo UUID ➔ Lưu).

User/LoginUserUseCase.java: Quy trình đăng nhập (Tìm User ➔ So khớp mật khẩu ➔ Tạo JWT).

User/UserService.java: Cầu nối dữ liệu hỗ trợ cho các Use Case.

common/UseCaseException.java: Xử lý các lỗi nghiệp vụ riêng biệt của Use Case.

3. Tầng Adapter (Giao tiếp bên ngoài)
Web (Cổng vào)
web/Auth/AuthController.java: Điểm tiếp nhận Request từ Frontend/Postman.

web/Auth/LoginRequest.java & RegisterRequest.java: Các bản tin dữ liệu đầu vào (DTO).

web/Auth/AuthResponse.java: Dữ liệu trả về gồm JWT Token và thông tin cơ bản.

Persistence (Dữ liệu)
persistence/User/UserJpaRepository.java: Cầu nối giữa Spring Data JPA và Database. Sử dụng UserId làm khóa chính.

persistence/User/UserDetailsServiceImpl.java: Tích hợp với Spring Security để tải thông tin người dùng khi xác thực.

Security (Bảo mật)
security/JwtTokenProvider.java: Chuyên trách việc tạo, giải mã và kiểm tra tính hợp lệ của Token JWT.

4. Tầng Infrastructure (Hạ tầng & Cấu hình)
config/SecurityConfig.java: Cấu hình phân quyền API, mã hóa mật khẩu và bộ lọc JWT.

config/CorsConfig.java: Cấu hình cho phép Frontend truy cập (CORS).

config/DataSeeder.java: Tự động tạo tài khoản Admin và các Role mặc định khi ứng dụng khởi chạy lần đầu.

db/changelog/migrations: Các file Liquibase quản lý cấu trúc bảng trong Database (đã cập nhật UUID).

🛠 Lưu ý về hệ thống
ID Strategy: Tất cả các tham chiếu khóa ngoại đến User đều sử dụng UserId (UUID) thay vì Long.

Exception Handling: Lỗi được bắt tập trung tại GlobalExceptionHandler để trả về định dạng JSON thống nhất.