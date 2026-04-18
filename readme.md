🛒 E-commerce Backend System
A high-performance, scalable e-commerce backend built with Java 17, Spring Boot 3, and Clean Architecture. This system features a multi-channel payment gateway, real-time order tracking, and automated customer communication.

📑 Table of Contents
Tech Stack

System Architecture

Key Features

Database Migrations

Installation & Setup

Postman Testing Guide

🚀 Tech Stack
Core: Java 17, Spring Boot 3.2+

Storage: * MySQL: Primary relational database for persistence.

Redis: High-speed storage for Shopping Carts, Caching, and Pub/Sub events.

MinIO: Object storage for product images and digital assets.

Security: Spring Security + JWT (Stateless Authentication).

Real-time: WebSocket (STOMP protocol) & SockJS.

Payments: Integrated Multi-Gateway (MoMo, VNPAY, VietQR).

DevOps: Docker Compose, Makefile (Automation), Liquibase (Migrations).

🏗 System Architecture
The project follows Clean Architecture to ensure the core business logic remains independent of external frameworks:

Domain: Core Entities, Value Objects (e.g., UserId), and Repository Interfaces.

Application: Use Cases (e.g., PlaceOrderUseCase), DTOs, and internal services.

Adapters:

Web: REST Controllers and WebSocket Message Handlers.

Persistence: JPA Entities and JpaRepository implementations.

Infrastructure: External service configurations (Mail, Security, Payment Gateways).

✨ Key Features
💳 Integrated Payment Gateway
A unified interface supporting multiple payment providers:

VietQR: Dynamic QR code generation for bank transfers.

MoMo & VNPAY: Secure payment processing with IPN (Instant Payment Notification) support.

📦 Real-time Order Tracking
Leveraging Event-Driven logic to keep users informed:

Technology: WebSocket + Redis Pub/Sub.

Workflow: When an order status changes (e.g., PENDING → SHIPPING), the system triggers a background event that updates the DB, sends an Email, and pushes a real-time notification to the UI.

📧 Automated Email Integration
Sends detailed transaction receipts once payment is confirmed.

Automated status updates (Packing, Shipping, Delivered) sent directly to the user's registered email.

Processed asynchronously using @Async to maintain high API responsiveness.

💬 Real-time 1-1 Chat
Support for direct customer-to-shop communication.

Auto-greeting: Instant "lónghop" welcome message when a user initiates a chat.

History: Persistent chat logs stored in MySQL for seamless continuity across sessions.

🛠 Installation & Setup
We use a Makefile to automate environment setup. Ensure you have Docker, Java 17, and Maven installed.

1. Initialize Infrastructure
Start MySQL, Redis, and MinIO containers:

Bash
make up-db
2. Database Migration
Run Liquibase to create tables and seed initial data:

Bash
make migrate-up
3. Run Application
Build and start the Spring Boot server:

Bash
make build
make run
🧪 Postman Testing Guide
Flow A: Payment & Tracking
Auth: POST /api/auth/login to obtain a JWT.

Order: POST /api/orders (Include Bearer Token). Note the orderId.

Confirm: POST /api/payment/confirm-payment/{orderId}.

Track: Update status via PUT /api/orders/{orderId}/status. Observe the real-time log and check your Inbox for the notification.

Flow B: WebSocket Chat
Connect: Open a WebSocket request to ws://localhost:8080/ws-raw.

Subscribe: Listen on /user/{username}/queue/messages.

Start Chat: Send a message to /app/start-chat to trigger the longshop auto-greeting.