# Makefile (root)
ifneq (,$(wildcard .env))
    include .env
    export
endif

DOCKER_COMPOSE_FILE := compose.yaml

.PHONY: build up up-db run down tidy migrate-up migrate-down migrate-drop db-reset new-migration

# Build jar
build:
	./mvnw clean package -DskipTests

tidy:
	./mvnw -q -DskipTests validate

up:
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d --remove-orphans

up-db:
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d mysql redis minio

down:
	docker compose -f $(DOCKER_COMPOSE_FILE) down

run:
	./mvnw spring-boot:run

# -----------------------
# Migration (Liquibase)
# -----------------------

migrate-up: tidy
	@echo "🚀 Chạy Liquibase update..."
	./mvnw liquibase:update \
		-Dliquibase.url="$(LIQUIBASE_URL)" \
		-Dliquibase.username="$(LIQUIBASE_USER)" \
		-Dliquibase.password="$(LIQUIBASE_PASSWORD)" \
		-Dliquibase.changeLogFile="$(LOCAL_CHANGELOG)" \
		-Dliquibase.searchPath="src/main/resources"

migrate-down: tidy
	@echo "⏪ Rollback 1 changeset..."
	./mvnw liquibase:rollback \
		-Dliquibase.url="$(LIQUIBASE_URL)" \
		-Dliquibase.username="$(LIQUIBASE_USER)" \
		-Dliquibase.password="$(LIQUIBASE_PASSWORD)" \
		-Dliquibase.changeLogFile="$(LOCAL_CHANGELOG)" \
		-Dliquibase.searchPath="src/main/resources" \
		-Dliquibase.rollbackCount=1

migrate-drop: tidy
	@echo "⚠️ XÓA TOÀN BỘ DATABASE..."
	./mvnw liquibase:dropAll \
		-Dliquibase.url="$(LIQUIBASE_URL)" \
		-Dliquibase.username="$(LIQUIBASE_USER)" \
		-Dliquibase.password="$(LIQUIBASE_PASSWORD)"

db-reset: migrate-drop migrate-up

new-migration:
	@read -p "Nhập tên migration: " desc; \
	timestamp=$$(date +%Y%m%d%H%M); \
	mkdir -p $(CHANGELOG_DIR); \
	filename=$(CHANGELOG_DIR)/$${timestamp}_$${desc}.sql; \
	echo "-- liquibase formatted sql" > $$filename; \
	echo "" >> $$filename; \
	echo "-- changeset ltc:$${timestamp}" >> $$filename; \
	echo "-- comment: $${desc}" >> $$filename; \
	echo "CREATE TABLE users (" >> $$filename; \
	echo "    id BIGINT AUTO_INCREMENT PRIMARY KEY," >> $$filename; \
	echo "    username VARCHAR(255) NOT NULL UNIQUE," >> $$filename; \
	echo "    email VARCHAR(255) NOT NULL UNIQUE," >> $$filename; \
	echo "    password VARCHAR(255) NOT NULL," >> $$filename; \
	echo "    role VARCHAR(50) NOT NULL" >> $$filename; \
	echo ");" >> $$filename; \
	echo "" >> $$filename; \
	echo "-- rollback DROP TABLE users;" >> $$filename; \
	echo "✅ Đã tạo: $$filename"; \
	echo "👉 Thêm vào file master: - include: { file: db/changelog/migrations/$${timestamp}_$${desc}.sql }"