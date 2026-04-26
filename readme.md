🛒  Modern E-commerce & Real-time Platform
It is a high-performance E-commerce backend system built with Java 17 and Spring Boot 3. The project strictly adheres to Clean Architecture principles to ensure maintainability, scalability, and high testability. It features real-time interactions, secure authentication, and a robust system design.

🛠 Tech Stack
Language: Java 17

Framework: Spring Boot 3

Architecture: Clean Architecture (Domain, Application, Infrastructure, Adapter)

Database: MySQL / PostgreSQL (RDBMS)

In-Memory Data / Cache: Redis (OTP handling, Session management)

Security: Spring Security, JWT (JSON Web Tokens), Argon2 Password Hashing

Real-time Engine: Raw WebSocket (Custom Handler for high-control communication)

Build Tool: Maven

Containerization: Docker & Docker Compose

🌟 Core Features
1. Authentication & Security
JWT Authentication: Secure stateless session management.

Role-Based Access Control (RBAC): Fine-grained permissions for Users and Admins.

OTP System: 16-character App Password integration with Gmail SMTP for password recovery and account verification, cached in Redis with TTL.

2. Real-time Interaction Engine
Raw WebSocket Chat: 1-1 real-time messaging between Customers and Shops.

Automated Greeting System: Intelligent system signals (INIT_CHAT) to trigger personalized welcome messages.

State Management: Tracking active sessions and user interaction states.

3. E-commerce Logistics
Product Management: Cataloging with category and supplier associations.

Order Processing: Cleanly decoupled logic for transaction management.

User Profiles: Comprehensive profile management including bio and secure credential storage.

🏗 Project Structure
Plaintext
src/main/java/ecommerce/example/ecommerce/
├── domain/         # Core business logic and entities (Pure Java)
├── application/    # Use cases and service orchestrators
├── adapter/        # External interfaces (Web Controllers, Persistence Impl)
│   ├── web/        # WebSocket Handlers and REST Controllers
│   └── persistence/# JpaRepositories and Entity Mappings
└── infrastructure/ # Framework configurations (Security, WebSocket Config)
🚀 Getting Started
Prerequisites
JDK 17

Docker & Docker Compose

Maven

Installation
Clone the repository: git clone https://github.com/No2004LTC/e-commerce.git

Setup infrastructure: docker-compose up -d (Redis, MySQL)

Configure application.properties (see Security section below).

Run the application: make run

🛡 Security Note
Sensitive information (Email App Passwords, DB Credentials) must be managed via environment variables. Never commit .env or sensitive properties to version control.