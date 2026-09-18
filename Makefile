.DEFAULT_GOAL := help
.PHONY: help init hooks run test test-integration verify build clean arch \
        infra-up infra-down db-up db-down redis-up redis-down \
        localstack-up localstack-down \
        docker-build docker-up docker-down logs swagger deps-tree

MVN := ./mvnw
DC  := docker compose

help: ## List available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ----------------------------------------------------------------------
# First use
# ----------------------------------------------------------------------
init: ## Adapts the template to the target service (interactive)
	@bash scripts/init-repo.sh

hooks: ## Installs the git hooks (author identity and secret detection)
	@git config core.hooksPath .githooks
	@echo "Hooks active. Manual check: .githooks/pre-commit"

# ----------------------------------------------------------------------
# Development
# ----------------------------------------------------------------------
run: ## Starts the app with the local profile (requires: make infra-up)
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=local

test: ## Unit tests + architecture rules (fast, no Docker)
	$(MVN) test

arch: ## Only the architecture rules
	$(MVN) test -Dtest=HexagonalArchitectureTest

test-integration: ## Only the integration tests (requires Docker)
	$(MVN) verify -DskipUnitTests

verify: ## Everything: unit + integration + coverage threshold
	$(MVN) verify

build: ## Packages the jar
	$(MVN) clean package

clean: ## Cleans target/
	$(MVN) clean

deps-tree: ## Shows which version of each library the Spring Boot BOM resolved
	$(MVN) dependency:tree

# ----------------------------------------------------------------------
# Local infrastructure
# ----------------------------------------------------------------------
infra-up: ## Starts Postgres + Redis
	$(DC) up -d postgres redis

infra-down: ## Stops Postgres + Redis
	$(DC) stop postgres redis

db-up: ## Only Postgres
	$(DC) up -d postgres

db-down: ## Stops Postgres
	$(DC) stop postgres

redis-up: ## Only Redis
	$(DC) up -d redis

redis-down: ## Stops Redis
	$(DC) stop redis

localstack-up: ## LocalStack (only if the service uses SQS/SNS/DynamoDB)
	$(DC) --profile aws up -d localstack

localstack-down: ## Stops LocalStack
	$(DC) --profile aws stop localstack

# ----------------------------------------------------------------------
# Docker
# ----------------------------------------------------------------------
docker-build: ## Builds the platform image (requires access to Justo's ECR)
	docker build -f .docker/Dockerfile.api --build-arg VERSION=local -t $$(basename $$PWD):local .

docker-up: ## Starts the full stack
	$(DC) up -d --build

docker-down: ## Stops the stack and removes the containers
	$(DC) down

logs: ## Follows the stack's logs
	$(DC) logs -f

swagger: ## Opens Swagger UI (the app must be running)
	open http://localhost:8080/swagger-ui.html
