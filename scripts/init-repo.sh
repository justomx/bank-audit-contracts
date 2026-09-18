#!/usr/bin/env bash
# =============================================================================
# init-repo.sh — adapts the template to the target service.
#
# A manual rename leaves residual references to "template" in pom.xml, the
# Java package, the application name, and the image publishing pipeline.
# This script updates every one of those points in a single operation and
# verifies the result.
#
# Usage:
#   bash scripts/init-repo.sh                      (interactive)
#   bash scripts/init-repo.sh card-account-service (direct)
#
# Runs exactly once, right after the repository is created from the template.
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

red()   { printf '\033[31m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }
gray()  { printf '\033[90m%s\033[0m\n' "$*"; }

# ---------------------------------------------------------------------------
# 1. Service name
# ---------------------------------------------------------------------------
SERVICE="${1:-}"
if [[ -z "$SERVICE" ]]; then
  echo "Service name in kebab-case, without the 'bank-' prefix."
  echo "Examples: card-account-service, evertec-connector, cards-bff"
  read -r -p "> " SERVICE
fi

SERVICE="${SERVICE#bank-}"

if [[ ! "$SERVICE" =~ ^[a-z][a-z0-9]*(-[a-z0-9]+)*$ ]]; then
  red "ERROR: '$SERVICE' is not valid kebab-case (lowercase letters, digits, and hyphens)."
  exit 1
fi

# ---------------------------------------------------------------------------
# 2. Derive the other names
# ---------------------------------------------------------------------------
REPO="bank-${SERVICE}"
# Java package: no hyphens, because Java does not allow them.
#   card-account-service -> cardaccount   (the generic suffix is dropped)
PKG_BASE="${SERVICE%-service}"
PKG="$(echo "$PKG_BASE" | tr -d '-')"
# Main class: card-account -> CardAccountApplication
MAIN_CLASS="$(echo "$PKG_BASE" | awk -F- '{for(i=1;i<=NF;i++) printf toupper(substr($i,1,1)) substr($i,2)}')Application"

echo
gray "─────────────────────────────────────────────"
echo "  Repository     : $REPO"
echo "  artifactId     : $REPO"
echo "  Java package   : com.justo.bank.$PKG"
echo "  Main class     : $MAIN_CLASS"
gray "─────────────────────────────────────────────"
echo
read -r -p "Continue? [y/N] " CONFIRM
[[ "$CONFIRM" =~ ^[sSyY]$ ]] || { echo "Cancelled."; exit 0; }

# ---------------------------------------------------------------------------
# 3. Move the Java package
# ---------------------------------------------------------------------------
echo
echo ">> Moving the Java package..."
for BASE in src/main/java src/test/java; do
  SOURCE="$BASE/com/justo/bank/template"
  DEST="$BASE/com/justo/bank/$PKG"
  if [[ -d "$SOURCE" ]]; then
    mkdir -p "$(dirname "$DEST")"
    git mv "$SOURCE" "$DEST" 2>/dev/null || mv "$SOURCE" "$DEST"
  fi
done

# ---------------------------------------------------------------------------
# 4. Replace references in the content
# ---------------------------------------------------------------------------
echo ">> Updating references..."

# sed -i portable between macOS (BSD) and Linux (GNU)
sed_i() { if [[ "$OSTYPE" == darwin* ]]; then sed -i '' "$@"; else sed -i "$@"; fi; }

# Java package, in sources and in any doc that mentions it.
#
# A while-read is used and NOT `mapfile`: macOS ships bash 3.2, where `mapfile`
# does not exist. This script has to run the same on a dev's Mac and on Linux.
while IFS= read -r f; do
  [[ -n "$f" ]] && sed_i "s/com\.justo\.bank\.template/com.justo.bank.$PKG/g" "$f"
done < <(grep -rl 'com\.justo\.bank\.template' \
  --include='*.java' --include='*.xml' --include='*.yml' --include='*.md' \
  --include='*.json' --include='*.properties' \
  --exclude-dir=.git --exclude-dir=target . 2>/dev/null || true)

# Main class
if [[ -f "src/main/java/com/justo/bank/$PKG/TemplateApplication.java" ]]; then
  git mv "src/main/java/com/justo/bank/$PKG/TemplateApplication.java" \
         "src/main/java/com/justo/bank/$PKG/$MAIN_CLASS.java" 2>/dev/null || \
  mv "src/main/java/com/justo/bank/$PKG/TemplateApplication.java" \
     "src/main/java/com/justo/bank/$PKG/$MAIN_CLASS.java"
  sed_i "s/TemplateApplication/$MAIN_CLASS/g" "src/main/java/com/justo/bank/$PKG/$MAIN_CLASS.java"
fi

# The main class is also read by the jar manifest and the runner image
for f in pom.xml $(find conf -name deploy-properties.json 2>/dev/null); do
  [[ -f "$f" ]] && sed_i "s/TemplateApplication/$MAIN_CLASS/g" "$f"
done

# pom.xml — artifactId, name, description
sed_i "s|<artifactId>bank-template-vertical-bank</artifactId>|<artifactId>$REPO</artifactId>|" pom.xml
sed_i "s|<name>bank-template-vertical-bank</name>|<name>$REPO</name>|" pom.xml
sed_i "s|<description>.*</description>|<description>$PKG_BASE service — Bank vertical</description>|" pom.xml

# README — title and any other mention of the template name
sed_i "s|bank-template-vertical-bank|$REPO|g" README.md
# Origin note. The template name is deliberately NOT mentioned here: that string
# is exactly what the final verification looks for, and a false positive there
# trains people to ignore the warning.
sed_i "1a\\
\\
> Generated from the Bank vertical services template.\\
> What is specific to this service goes in [\`CONTEXT.md\`](CONTEXT.md).
" README.md

# Application name and ECR repository name
sed_i "s|name: bank-template-vertical-bank|name: $REPO|" src/main/resources/application.yml
# Platform files that carry the service name
for f in .docker/Dockerfile.api justo-app.properties $(find conf -type f 2>/dev/null); do
  [[ -f "$f" ]] && sed_i "s|bank-template-vertical-bank|$REPO|g" "$f"
done
sed_i "s|SERVICE_NAME:-bank-template-vertical-bank|SERVICE_NAME:-$REPO|" src/main/resources/logback-spring.xml

# ---------------------------------------------------------------------------
# 5. Delete the sample slice (optional)
# ---------------------------------------------------------------------------
echo
read -r -p "Delete the sample (Sample)? Leaves only the skeleton. [y/N] " DELETE_SAMPLE
if [[ "$DELETE_SAMPLE" =~ ^[sSyY]$ ]]; then
  echo ">> Deleting the sample..."
  find src -name 'Sample*' -delete
  rm -f src/main/resources/db/migration/V1__init.sql
  # CacheConfig references the Sample model; it becomes unusable without it.
  rm -f "src/main/java/com/justo/bank/$PKG/config/CacheConfig.java"
  green "   Sample deleted."
  echo "   Note: CacheConfig was removed because it depends on Sample. If the service"
  echo "   requires caching, it must be recovered from the template and adapted to"
  echo "   the model. A V1__init.sql of your own must be written before the first startup."
else
  gray "   Kept. It must be deleted before the first pull request to main."
fi

# ---------------------------------------------------------------------------
# 6. Verification: make sure no trace remains
# ---------------------------------------------------------------------------
echo
echo ">> Verifying that no references to the template remain..."
LEFTOVERS=$(grep -rn 'bank-template-vertical-bank\|com\.justo\.bank\.template\|TemplateApplication' \
  --exclude-dir=.git --exclude-dir=target --exclude='init-repo.sh' . 2>/dev/null || true)

if [[ -n "$LEFTOVERS" ]]; then
  red "   References remain unupdated:"
  echo "$LEFTOVERS" | sed 's/^/     /'
  echo
  echo "   They require manual correction before committing the changes."
else
  green "   No traces of the template left."
fi

# ---------------------------------------------------------------------------
# 7. What's next
# ---------------------------------------------------------------------------
cat <<FIN

$(green "Done.")

Next steps:

  1. Write CONTEXT.md with the service's purpose and its business language.
     In its absence, both a human onboarding and an agent will infer the
     domain from the code.
  2. Update .github/CODEOWNERS with the corresponding teams.
     One team per path: one required reviewer per pull request.
  3. Configure branch protection on main: 1 required approval,
     review from Code Owners, dismiss stale approvals, no force pushes.
     See "Review scheme" in README.md.
  4. Add the repository topics on GitHub. They are the wiring to Jenkins:
     without them the repository does not enter any pipeline.
       squad-tdc  ·  backend  ·  vertical-bank
  5. Request from DevOps: ECR repository, database schema, IRSA role, and
     the AWS_ROLE_ARN and ECR_REGISTRY secrets.
  6. Verify the build:

       make infra-up
       make verify

  7. Remove this script:  git rm scripts/init-repo.sh

FIN
