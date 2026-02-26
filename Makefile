# Makefile cho dự án E-commerce Spring Boot

APP_NAME=ecommerce-app
DOCKER_COMPOSE_FILE=compose.yaml

.PHONY: build run test clean up down logs

# Build project với Maven (dùng Maven wrapper nếu có)
build:
	./mvnw clean install -DskipTests

# Run Spring Boot app(chay app)
run:
	./mvnw spring-boot:run

# Run test
test:
	./mvnw test

# Clean target folder( don rac)
clean:
	./mvnw clean

# Khởi động Docker Compose (MySQL, Redis, MinIO)
up:
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d --remove-orphans

# Dừng Docker Compose
down:
	docker compose -f $(DOCKER_COMPOSE_FILE) down

# Xem logs
logs:
	docker compose -f $(DOCKER_COMPOSE_FILE) logs -f
