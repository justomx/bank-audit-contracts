.DEFAULT_GOAL := help
.PHONY: help hooks test arch docs-check verify build install clean deps-tree

MVN := ./mvnw

help: ## List available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ----------------------------------------------------------------------
# First use
# ----------------------------------------------------------------------
hooks: ## Installs the git hooks (author identity and secret detection)
	@git config core.hooksPath .githooks
	@echo "Hooks active. Manual check: .githooks/pre-commit"

# ----------------------------------------------------------------------
# Development
# ----------------------------------------------------------------------
test: ## Unit tests + architecture rules
	$(MVN) test

arch: ## Only the architecture rules
	$(MVN) test -Dtest='*ArchitectureTest' -DfailIfNoTests=false

docs-check: ## Only the docs-freshness gate (broken links, stale paths)
	$(MVN) -q test -Dtest=DocsFreshnessTest

verify: ## Everything: unit tests, architecture rules, docs, coverage threshold
	$(MVN) verify

build: ## Packages the jar
	$(MVN) clean package

install: ## Installs the jar into the local Maven repository
	$(MVN) install

clean: ## Cleans target/
	$(MVN) clean

deps-tree: ## Shows the resolved dependency tree
	$(MVN) dependency:tree
