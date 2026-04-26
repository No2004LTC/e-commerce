# C2C E-commerce Backend

C2C E-commerce is a high-performance Consumer-to-Consumer platform built with Java 17, focusing on scalability, efficiency, and real-time user engagement. The project strictly adheres to Clean Architecture principles to ensure the business logic remains independent, maintainable, and easy to test.

---

## Tech Stack

- Language: Java 17 (Core, Multithreading, Stream API)
- Framework: Spring Boot 3.x
- Database: MySQL / PostgreSQL
- In-Memory Data / Cache: Redis (Handling OTP verification, Rate Limiting, and Session Management)
- Object Storage: MinIO (S3 Compatible - Storing product images and user avatars)
- Real-time Engine: Raw WebSocket (Low-latency Client-to-Client communication handler)
- Infrastructure: Docker & Docker Compose

---

## Key Features

- Authentication & Security:
  - Stateless authentication mechanism based on JWT (JSON Web Token).
  - OTP system integrated with Gmail SMTP (16-character App Password) for password recovery and account verification.
  - Redis integration for temporary credential storage with automatic TTL (Time-To-Live).

- User Management:
  - Detailed profile management including Bio and Avatar updates.
  - High-performance image upload and processing system via MinIO to optimize server bandwidth and storage.

- E-commerce Interactions:
  - Scalable product catalog and category management for C2C models.
  - Order processing and transaction management designed as standalone Use Cases, ensuring business logic is decoupled from external frameworks.

- Real-time Engine:
  - 1-on-1 real-time chat between buyers and sellers using raw WebSocket protocols.
  - Automated greeting mechanism (Auto-greeting) triggered upon chat entry via INIT_CHAT system signals.
  - Real-time presence tracking (Online/Offline status) for all active users.

- High-Performance Optimization:
  - Optimized database queries and resource management through Clean Architecture layers.
  - Flexible infrastructure allowing for easy upgrades or swaps of technical components (e.g., switching databases) without affecting the core business logic.